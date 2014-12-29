package nettest;

import java.awt.Color;

public class WebNode {
	private Website _web;
	private String _name;
	private Color _bgcolor;
	private String _title;

	WebNode(String url, String name) throws Exception {
		_web = new Website(url);
		init(_web, name);
	}

	WebNode(Website web, String name) {
		init(web, name);
	}

	private void init(Website web, String name) {
		_web = web;
		_name = name;
		_bgcolor = Color.RED;
		_title = web.getTitle();
	}


	public Website getWebsite() {
		return _web;
	}

	public String getUrl() {
		return _web.getUrl();
	}

	public String getName() {
		return _name;
	}

	public String getTitle() {
		return _title;
	}

	public String getMainTest() {
		String[] text = _web.getMainText();
		StringBuilder maintext = new StringBuilder(1024);
		if (text == null) return "";
		for (String item : text) maintext.append(item);
		return maintext.toString();
	}

	/**
	 * 背景色を設定します
	 * @param c	背景色
	 */
	public void setBackGround(Color c) {
		_bgcolor = c;
	}

	public Color getBackGround() {
		return _bgcolor;
	}


}
