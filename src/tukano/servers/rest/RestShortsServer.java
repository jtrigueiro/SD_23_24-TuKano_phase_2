package tukano.servers.rest;

import java.net.URI;
import java.util.logging.Logger;
import java.net.InetAddress;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import javax.net.ssl.SSLContext;

import tukano.utils.Args;
import tukano.utils.Discovery;

public class RestShortsServer {

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	public static final int PORT = 8080;
	public static final String SERVICE = "shorts";
	private static final String SERVER_URI_FMT = "https://%s:%s/rest";
	private static Logger Log = Logger.getLogger(RestShortsServer.class.getName());

	public static void main(String[] args) {
		Args.use(args);
		
		try {

			ResourceConfig config = new ResourceConfig();
			config.register(RestShortsResource.class);

			String ip = InetAddress.getLocalHost().getHostName();
			String serverURI = String.format(SERVER_URI_FMT, ip, PORT);

			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config, SSLContext.getDefault());

			Discovery discovery = Discovery.getInstance();
			discovery.announce(SERVICE, serverURI.toString());
			Log.info(String.format("%s Rest Server ready @ %s\n", SERVICE, serverURI));


		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
