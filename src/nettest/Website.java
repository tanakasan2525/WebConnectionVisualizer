package nettest;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.image4j.codec.ico.ICODecoder;

/**
 * WebSiteの情報をカプセル化するクラス
 * @author a5812070
 *
 */
public class Website {

	private String _url;
	private String _html;
	private HtmlAnalyzer htmlAnalyzer;

	Website(String url) throws Exception {
		inputWebsite(url);
	}

	long start = System.currentTimeMillis();
	void timer(String message, String url) {
		System.out.println("-----------" + message + "  : " + url +"----------------");
		System.out.println(System.currentTimeMillis() - start);
		System.out.println("---------------------------");
		start = System.currentTimeMillis();
	}

	/**
	 * 【非公開】HTMLを取得する
	 * @param url	取得元URL
	 * @return
	 */
	private String downloadHtml(String url) {
		try {
			start = System.currentTimeMillis();
			HtmlDownloader hd = new HtmlDownloader(url);
			timer("connect", url);
			hd.download(3000);
			timer("read", url);

			_html = hd.getHtml();
			String charsetname = getCharset();
			if (charsetname != null) {
				Charset c = Charset.forName(charsetname);
				if(!c.equals(hd.utf8)) {
					hd.load(c);
					hd.download(3000);
				}
			}
			return hd.getHtml();
		} catch (IOException ioex) {			//	getInputStream()でレスポンスコード403が返される時がある
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * タイムアウトが設定できるHtmlのダウンローダ
	 * タイムアウトの値は固定に変更されました。要リファクタリング
	 * @author a5812070
	 *
	 */
	class HtmlDownloader extends Thread {
		Charset utf8 = Charset.forName("utf-8");
		BufferedReader br;
		StringBuilder html;
		HttpURLConnection conn;
		InputStream is;
		URL url;
		HtmlDownloader(String url) throws Exception {
			this.url = new URL(url);
			load(utf8);
		}
		void load(Charset c) throws Exception {
			conn = (HttpURLConnection)this.url.openConnection();
			conn.setRequestProperty(
					"User-Agent","Mozilla/5.0 (Macintosh; U; Intel Mac OS X; ja-JP-mac; rv:1.8.1.6) Gecko/20070725 Firefox/2.0.0.6"
					);
			conn.setConnectTimeout(2000);
			conn.setReadTimeout(2000);
			is = conn.getInputStream();
			InputStreamReader isr = new InputStreamReader(is,c);
			br = new BufferedReader(isr);
		}
		public void run() {
			try {
				//	推奨されていないが、available()でサイズを取得し、先にバッファを確保して高速化を図る
				//	facebookのページ取得時間が、1294ms→1155msになった
				html = new StringBuilder(is.available());
				String line;
				while ((line = br.readLine()) != null) {
					html.append(line);
				}
				/*while (br.read(cbuf, 0, 10000) != -1) {	//	改行が入り、正規表現を複数行モードにしなければならない
					html.append(cbuf);
				}*/

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		void download(int timeout) throws Exception {
			run();
			//start();
			//join(timeout);
		}
		String getHtml() { return html.toString(); }
	}

	/**
	 * Websiteを読み込む
	 * @param url	対象のWebsiteのURL
	 */
	public void inputWebsite(String url) throws Exception {
		if((_html = downloadHtml(url)) == null) {
			throw new Exception("can't download htmlfile.");
		} else {
			htmlAnalyzer = new HtmlAnalyzer(_html);
		}
		_url = url;
	}

	/**
	 * HTMLの内容を標準出力に表示する
	 */
	public void printHtml() {
		System.out.println(_html);
	}

	public String getHtml() { return _html; }

	/**
	 * タイトルを取得する
	 * @return
	 */
	public String getTitle() {
		final Pattern titlePattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE);
		final Matcher matcher = titlePattern.matcher(_html);

		if (matcher.find()) return matcher.group(1);
		return _url;
	}

	/**
	 * HTMLに記述されたテキストエンコードの種類を取得する
	 * @return
	 */
	public String getCharset() {
		final Pattern csPattern = Pattern.compile("<meta.*?charset=\"?([\\w\\-]+)[\">\\s]", Pattern.CASE_INSENSITIVE);
		final Matcher matcher = csPattern.matcher(_html);

		if (matcher.find()) return matcher.group(1);
		return null;
	}

	/**
	 * 主な文章を取得する
	 * @return
	 */
	public String[] getMainText() {
		return htmlAnalyzer.getMainText();
	}

	/**
	 *	WebsiteがリンクしているサイトのURLをすべて取得する
	 * @return	URLのString配列
	 */
	public String[] getLinks() {
		/*final Pattern urlPattern = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+"
                , Pattern.CASE_INSENSITIVE);*/
		final Pattern urlPattern = Pattern.compile("href\\s*=\\s*\"?((http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+)"
                , Pattern.CASE_INSENSITIVE);
		final Matcher matcher = urlPattern.matcher(_html);
		List<String> list = new ArrayList<String>();

		while (matcher.find()) {
			if (!_url.equals(matcher.group(1)))
				list.add(matcher.group(1));
		}
		return (String[]) list.toArray(new String[0]);
	}

	/**
	 * WebsiteがリンクしているサイトのURLをすべて取得する(重複したドメインは１つのみ、自身のドメインは含まない)
	 * @return
	 */
	public ArrayList<String> getLinksWithoutSameDomain() {
		List<String> domains = new ArrayList<String>();
		ArrayList<String> list = new ArrayList<String>();
		domains.add(getDomain());
		for (String s : getLinks()) {
			String domain = getDomain(s);
			if (!domains.contains(domain)) {
				domains.add(domain);
				list.add(s);
			}
		}
		return list;
	}

	/**
	 * URLのドメイン部分を取得する
	 * @return	成功時はドメイン名、失敗時はnull
	 */
	public String getDomain() {
		return getDomain(_url);
	}

	public String getDomain(String url) {
		try {
			URL url2 = new URL(url);
			return url2.getHost();
		}catch(Exception e) {
			return null;
		}
	}

	/**
	 * アイコンを取得する
	 * @return	アイコンの画像
	 */
	public Image getIcon() {
		String urlIcon = getIconUrl();

		if (urlIcon == null)
			return null;
		try {
			return ICODecoder.read(new URL(urlIcon).openConnection().getInputStream()).get(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	//	Patternを生成するのは重いのでここで静的変数で宣言する

	//	<link>タグ属性を取得
	static final Pattern pLink = Pattern.compile("<link (.*?)>", Pattern.CASE_INSENSITIVE);

	//	rel属性の値を取得
	static final Pattern pRel = Pattern.compile("rel\\s*=\\s*\"((shortcut )?icon)\"", Pattern.CASE_INSENSITIVE);

	//	href属性の値を取得
	static final Pattern pHref = Pattern.compile("href\\s*=\\s*\"(.*?)\"", Pattern.CASE_INSENSITIVE);

	/**
	 * アイコンのURLを取得する
	 * @return	アイコンのURL
	 */
	public String getIconUrl() {
		String urlIcon = null;

		final Matcher mLink = pLink.matcher(_html);

		while (mLink.find()) {
			Matcher mRel = pRel.matcher(mLink.group(1));
			if (mRel.find()) {
				if ("icon".equals(mRel.group(1)) || "shortcut icon".equals(mRel.group(1))) {
					Matcher mHref = pHref.matcher(mLink.group(1));
					if (mHref.find()) {
						try {
							String name = mHref.group(1);
							String test1 = name.substring(0, 6);
							if ("//".equals(name.substring(0, 2)))	//	//から始まる場合はhttp:をつければ、アイコンのパス(http;//play.google.comなど)
								urlIcon = "http:" + name;
							else if ("http:/".equals(test1) || "https:".equals(test1))	//	プロトコルが入ってる場合は、そのまま
								urlIcon = name;
							else	//	ファイル名or/ファイル名なら、プロトコル名とドメイン名をつけると、アイコンのパス
								urlIcon = new URL("http", getDomain(), "/".equals(name.substring(0,1))? "" : "/" + name).toString();
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					}
				}
			}
		}

		if (urlIcon == null)
			return "http://" + getDomain() + "/favicon.ico";	//	HTMLに設定されてない場合、ファイルが存在しない可能性もある
		try {
			return urlIcon;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * URLを取得する
	 * @return
	 */
	public String getUrl() {
		return _url;
	}
}
