package nettest;


public class TestMain {

	public static void main(String[] args) {

		try {
			ProxyServer server = new ProxyServer(new WebGraphHandler());

			server.start();

			//Browser.open("http://www.aoyama.ac.jp/");
			Browser.open("http://www.agulin.aoyama.ac.jp/");

			server.waitServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
