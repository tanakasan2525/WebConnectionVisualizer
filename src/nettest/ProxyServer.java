package nettest;

import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;

public class ProxyServer {
	private Server _server;

	ProxyServer(ContextHandler handler) throws Exception {
		_server = new Server();
		ServerConnector http = new ServerConnector(_server);
		http.setPort(9999);
		_server.setConnectors(new Connector[] { http });

		//	servlet
		ServletHandler servletHandler = new ServletHandler();
		servletHandler.addServletWithMapping(ProxyServlet.class, "/*");

		handler.setHandler(servletHandler);
		_server.setHandler(handler);
	}

	public void start() throws Exception
	{
		_server.start();
	}

	public void waitServer() throws Exception {
		_server.join();
	}

}

