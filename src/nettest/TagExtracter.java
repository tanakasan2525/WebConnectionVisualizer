package nettest;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class TagExtracter {

	public static String[][] extractKeyword(String appId, String sentence) {
		try {
			final String apiUrl = "http://jlp.yahooapis.jp/KeyphraseService/V1/extract";
			
			System.out.println(sentence);

			String parameters = getParametersString(appId, sentence);
			String xmlContent = getContent(new URL(apiUrl), parameters);
			Document doc = getDocument(xmlContent);
			ResultSet rs = getResultSet(doc);

			String[][] ret = new String[rs.result.length][2];
			for (int i=0; i<rs.result.length; i++) {
				ret[i][0] = rs.result[i].Keyphrase;
				ret[i][1] = rs.result[i].Score;
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getParametersString(String appId, String sentence) throws UnsupportedEncodingException {
		String parameters =
			"appid=" + appId +
			"&" +
			"sentence=" + URLEncoder.encode(sentence, "UTF-8");
		return parameters;
	}

	private static int getLength(XPath xpath, Document doc, String expression) throws XPathExpressionException {
		NodeList nodelist = (NodeList)xpath.evaluate(expression, doc, XPathConstants.NODESET);
		if (nodelist != null) {
			return nodelist.getLength();
		}
		return 0;
	}

	private static String getString(XPath xpath, Document doc, String expression) throws XPathExpressionException {
		return xpath.evaluate(expression, doc);
	}

	private static Document getDocument(String xmlContent) throws IOException, SAXException, ParserConfigurationException {
		InputSource is = new InputSource(new StringReader(xmlContent));
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
	}

	// URLからコンテンツ(HTML/XMLページの文字列)を取得
	private static String getContent(URL url, String parameters) throws IOException, ParseException {
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestProperty("User-agent","Mozilla/5.0");
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.connect();

		OutputStream os = con.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write(parameters);
		bw.flush();
		bw.close();

		// ex. String ct = "text/xml; charset=\"utf-8\"";
		String ct = con.getContentType();
		String charset = "UTF-8"; // Content Type が無ければ UTF-8
		if (ct != null) {
			String cs = null;
			//String cs = new ContentType(ct).getParameter("charset");
			if(cs != null){
				charset = cs;
			}
		}

		InputStream is = con.getInputStream();
		InputStreamReader isr = new InputStreamReader(is, charset);
		BufferedReader br = new BufferedReader(isr);
		StringBuffer buf = new StringBuffer();
		String s;
		while ((s = br.readLine()) != null) {
			buf.append(s);
			buf.append("\r\n"); // 改行コードKIMEUCHI
		}
		br.close();
		con.disconnect();

		return buf.toString();
	}

	private static ResultSet getResultSet(Document doc) throws Exception {
		XPath xpath = XPathFactory.newInstance().newXPath();

		int length = getLength(xpath, doc, "ResultSet/Result");
		Result[] r = new Result[length];
		for(int i=0; i<length; i++){
			r[i] = new Result();
			r[i].Keyphrase = getString(xpath, doc, "ResultSet/Result[" + (i+1) + "]/Keyphrase");
			r[i].Score     = getString(xpath, doc, "ResultSet/Result[" + (i+1) + "]/Score");
		}

		ResultSet rs = new ResultSet();
		rs.result = r;
		return rs;
	}

	public static class ResultSet{
		public Result[] result;
	}

	public static class Result{
		public String Keyphrase; // キーフレーズ
		public String Score; // キーフレーズの重要度
	}

}
