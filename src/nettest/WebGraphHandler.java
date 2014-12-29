package nettest;

import java.awt.EventQueue;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class WebGraphHandler extends ServletContextHandler {
	private GraphFrame _graphFrame;

	WebGraphHandler() {
		_graphFrame = new GraphFrame();
	}

	/**
	 * GETメソッドがリクエストされた時に呼び出されます。
	 * @param target
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	private void doGet(String target, Request baseRequest,
			final HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		if (request.getPathInfo().lastIndexOf("/") == request.getPathInfo().length()-1) {

			//	下のRunnableはdoGet()の後で実行されるため、
			//	finalな変数に値を入れていないと、requestの内容は消えてしまう
			final StringBuffer url = request.getRequestURL();

			if ("http://download.mozilla.org/".equals(url)) return;	// for firefox

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					_graphFrame.setWebGraph(url.toString());
				}
			});
		}
		//	for debug
		/*System.out.println(request.getRequestURL());
		System.out.println(request.getRequestURI());
		System.out.println(request.getPathInfo());
		System.out.println();*/
	}

	/**
	 * POSTメソッドがリクエストされた時に呼び出されます。
	 * @param target
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	private void doPost(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {


	}

	@Override
	public void doHandle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
		super.doHandle(target, baseRequest, request, response);
		}catch (Exception e) {}
		//	メソッドの種類による処理分岐
		String methodName = request.getMethod();
		if ("GET".equals(methodName)) {
			doGet(target, baseRequest, request, response);
		} else if("POST".equals(methodName)) {
			doPost(target, baseRequest, request, response);
		} else {
			System.out.println(methodName);		//	debug
		}

		/*==================================================================
		HttpServletRequestクラスの使えそうなメソッドのメモ

		getMethod():		GET、HEAD、POSTなどのメソッド名の取得
		getPathInfo():		リクエスト対象のファイルのパス(サーバのルートディレクトリからの相対パス)
		getHeaderNames():	リクエストに含まれるヘッダ名の一覧の取得
		getHeader(String):	ヘッダの中身の取得
		getRequestURL():	リクエスト先ファイルのURL(http://www.yahoo.co.jp/やhttp://www.yahoo.co.jp/favicon.icoなど)

		↓分かりやすいHTTPリクエスト/レスポンスの解説サイト
		http://www.tohoho-web.com/ex/http.htm
		/==================================================================*/
	}

	/**
	 * 拡張子を取得する
	 * @param url
	 * @return
	 */
	String getExtension(String url) {
		if (url == null) {
			return null;
		}
		int idx = url.lastIndexOf(".");
		if (idx != -1) {
			return url.substring(idx + 1);
		}
		return url;
	}
}
