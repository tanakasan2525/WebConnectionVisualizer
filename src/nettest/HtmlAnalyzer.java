package nettest;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HtmlAnalyzer {
	private String _html;

	HtmlAnalyzer(URI filepath) {
		StringBuilder html = new StringBuilder();
		try {
			BufferedReader br =
					new BufferedReader(new InputStreamReader(new FileInputStream(filepath.toString()),"EUC-JP"));
			String line;
			while ((line = br.readLine()) != null) {
				html.append(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		_html = html.toString();
	}

	HtmlAnalyzer(String html) {
		this._html = html;
	}

	public String[] getMainText() {
		String html = trimComment(getBody());
		String[] needlessTags = {
				"script", "noscript", "form", "iframe", "address", "style"
		};
		html = trimTags(html, needlessTags);

		String[] blocks = separateHtml(html);
		if (blocks == null) return new String[] { trimAllTags(html) };	//	求めているフォーマットでない場合は分析を諦める

		while (blocks.length == 1) {
			String[] temp = separateHtml(blocks[0]);
			if (temp == null) break;
			else blocks = temp;
		}

		ArrayList<String> ret = new ArrayList<String>();
		for (String block : blocks) {
			block = trimWhiteSpace(block);
			block = trimAllTags(block);
			for (String line : block.split("\n")) {
				if (evaluateSentence(line) > 30) {
					ret.add(line);
				}
			}
		}
		return (ret.size() > 0)? ret.toArray(new String[0]): new String[]{getRawText()};
	}

	public String getRawText() {
		String html = trimComment(getBody());
		String[] needlessTags = {
				"script", "noscript", "form", "iframe", "address", "style"
		};
		html = trimTags(html, needlessTags);
		html = trimWhiteSpace(html);
		html = trimAllTags(html);
		System.out.println("get raw text");
		return html;
	}

	/**
	 * HTMLのbodyタグに囲まれた部分を取得します。
	 * @return	成功：文字列、失敗：""
	 */
	public String getBody() {
		final Pattern pBody = Pattern.compile("<body[^>]*>(.*?)</body>", Pattern.CASE_INSENSITIVE);
		final Matcher mBody = pBody.matcher(_html);

		if (mBody.find()) {
			return mBody.group(1);
		}
		return "";
	}

	/**
	 * 文章らしさを評価する(0～100)
	 * @param sentence
	 * @return
	 */
	public int evaluateSentence(String sentence) {
		int eval = 0;
		//		英文も評価するなら「.」が必要だが、3.14などの数字や関係ないピリオドも有り得る。
		int idx = sentence.lastIndexOf("。");
		if (idx != -1) {
			if (idx == sentence.length()) {
				eval = 100;		//	文末に句点なら評価が高い
			} else {
				eval = 60;
			}
		}
		int len = sentence.length();
		if (len > 45) len = 45;
		if (len == 0) return 0;
		return eval / (45 / len);
	}

	/**
	 * すべてのHTMLタグを取り除き、改行に置き換える(タグが連続する場合は、まとめて1つの改行にする)
	 * @param html
	 * @return
	 */
	static final public String trimAllTags(String html) {
		final Pattern p = Pattern.compile("(<[^>]*>)+", Pattern.CASE_INSENSITIVE);
		final Matcher m = p.matcher(html);

		return m.replaceAll("\n");
	}

	/**
	 * <tag>～</tag>のフォーマットの任意のタグを取り除く
	 * @param html
	 * @param tag
	 * @return
	 */
	static final public String trimTag(String html, String tag) {
		final Pattern p = Pattern.compile("<"+tag+".*?>(.*?)</"+tag+">", Pattern.CASE_INSENSITIVE);
		final Matcher m = p.matcher(html);

		return m.replaceAll("");
	}

	/**
	 * <tag>～</tag>のフォーマットの任意のタグ郡を取り除く
	 * @param html
	 * @param tag
	 * @return
	 */
	static final public String trimTags(String html, String[] tags) {
		String ret = html;
		for (String tag : tags)
			ret = trimTag(ret, tag);
		return ret;
	}

	/**
	 * コメント部分を取り除く
	 * @param html
	 * @return
	 */
	static final public String trimComment(String html) {
		final Pattern p = Pattern.compile("<!--[^(-->)]*?-->", Pattern.CASE_INSENSITIVE);
		final Matcher m = p.matcher(html);

		return m.replaceAll("");
	}

	/**
	 * 空白文字(スペース、改行)を取り除く
	 * @param html
	 * @return
	 */
	public String trimWhiteSpace(String html) {
		final Pattern p = Pattern.compile("( |\n|\t|\r)", Pattern.CASE_INSENSITIVE);
		final Matcher m = p.matcher(html);

		return m.replaceAll("");
	}


	/**
	 * HTML文章を意味ごとのクラスタに分ける
	 * 方針：　良いWebページはデファクトスタンダードで＜div id="CSSで設定したID"＞を使って、ブロックごとに分けている。
	 * 		　それをそのまま利用して、クラスタに分ける
	 * 欠点：　個人で独学して作ったようなWebページだと上手くいかない。
	 * @param html
	 * @return
	 */
	public String[] separateHtml(String html) {
		return recursiveSeparateHtml(html, new ArrayList<String>());
	}

	private String[] recursiveSeparateHtml(String html, List<String> lst) {
		//	<div>が入れ子になっている可能性があるので、再帰的に処理する
		final Pattern pBlock = Pattern.compile("<div.*?id\\s*=.*?>(.*)", Pattern.CASE_INSENSITIVE);
		final Matcher mBlock = pBlock.matcher(html);

		final Pattern pDiv = Pattern.compile("(</div>|<div )", Pattern.CASE_INSENSITIVE);

		String text;
		int count = 0;

		if (mBlock.find()) {
			text = mBlock.group(1);
			++count;
			final Matcher mDiv = pDiv.matcher(text);
			while (mDiv.find()) {
				String div = mDiv.group(1);
				if (div.charAt(1) == '/') {
					if (--count == 0) {
						if (mDiv.start() != 0) {
							lst.add(text.substring(0, mDiv.start()));
						}
						return recursiveSeparateHtml(text.substring(mDiv.start(1) + div.length()), lst);
					}
				} else {
					++count;
				}
			}
		} else {
			if (lst.size() > 0)
				return lst.toArray(new String[0]);
		}

		return null;
	}
}
