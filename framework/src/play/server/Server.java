package play.server;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import play.Logger;
import play.Play;
import play.Play.Mode;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;

public class Server {

	public Server() {
		final Properties p = Play.configuration;
		int httpPort = Integer.parseInt(p.getProperty("http.port", "9000"));
		InetAddress address = null;
		if (System.getProperties().containsKey("http.port")) {
			httpPort = Integer.parseInt(System.getProperty("http.port"));
		}
		try {
			if (p.getProperty("http.address") != null) {
				address = InetAddress.getByName(p.getProperty("http.address"));
			}
			if (System.getProperties().containsKey("http.address")) {
				address = InetAddress.getByName(System.getProperty("http.address"));
			}
		} catch (Exception e) {
			Logger.error(e, "Could not understand http.address");
			System.exit(-1);
		}

		try {
			ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool()));
			bootstrap.setPipelineFactory(new HttpServerPipelineFactory());
			bootstrap.setOption("child.tcpNoDelay", true);
			// bran: some other options we may consider
			// bootstrap.setOption("child.receiveBufferSize", 1048576); // some
			// linux will auto-tune this
//			 bootstrap.setOption("child.keepAlive", true);
			bootstrap.bind(new InetSocketAddress(address, httpPort));

			// bran: create another instance binding to default port + 1 
			bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool()));
			bootstrap.setPipelineFactory(new HttpServerPipelineFactory());
			bootstrap.setOption("child.tcpNoDelay", true);
			// bran: some other options we may consider
			// bootstrap.setOption("child.receiveBufferSize", 1048576); // some
//			 linux will auto-tune this
//			 bootstrap.setOption("child.keepAlive", true);
			bootstrap.bind(new InetSocketAddress(address, httpPort + 1));

			String ports = httpPort + " and " + (httpPort +1);
			
			if (Play.mode == Mode.DEV) {
				if (address == null) {
					Logger.info("Listening for HTTP on port %s (Waiting a first request to start) ...", ports);
				} else {
					Logger.info("Listening for HTTP at %2$s:%1$s (Waiting a first request to start) ...", ports, address);
				}
			} else {
				if (address == null) {
					Logger.info("Listening for HTTP on port %s ...", ports);
				} else {
					Logger.info("Listening for HTTP at %2$s:%1$s  ...", ports, address);
				}
			}
		} catch (ChannelException e) {
			Logger.error("Could not bind on port " + httpPort, e);
			System.exit(-1);
		}

	}

	public static void main(String[] args) throws Exception {
		File root = new File(System.getProperty("application.path"));
		Play.init(root, System.getProperty("play.id", ""));
		if (System.getProperty("precompile") == null) {
			new Server();
		} else {
			Logger.info("Done.");
		}
	}
}
