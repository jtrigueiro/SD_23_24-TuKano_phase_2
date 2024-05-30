package tukano.servers.rest;

import java.net.URI;
import java.util.logging.Logger;
import java.net.InetAddress;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import javax.net.ssl.SSLContext;

import tukano.utils.Args;
import tukano.utils.Discovery;

public class RestBlobsServer {

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	public static final int PORT = 8080;
	public static final String SERVICE = "blobs";
	private static final String SERVER_URI_FMT = "https://%s:%s/rest";
	private static Logger Log = Logger.getLogger(RestBlobsServer.class.getName());

	public static void main(String[] args) {
		try {
			// int index = args[0]

			ResourceConfig config = new ResourceConfig();

			String host = InetAddress.getLocalHost().getHostName();
			String serverURI = String.format(SERVER_URI_FMT, host, PORT);

			String[] newArgs = new String[args.length + 2];
			System.arraycopy(args, 0, newArgs, 0, args.length);

			newArgs[args.length] = "-serverURI";
			newArgs[args.length + 1] = serverURI;
			Args.use(newArgs);

			config.register(RestBlobsResource.class);

			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config, SSLContext.getDefault());

			Discovery discovery = Discovery.getInstance();
			discovery.announce(SERVICE, serverURI.toString());
			Log.info(String.format("%s Rest Server ready @ %s\n", SERVICE, serverURI));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
