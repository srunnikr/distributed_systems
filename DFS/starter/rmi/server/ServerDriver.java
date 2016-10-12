/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import rmi.RMIException;
import rmi.Skeleton;

/**
 * The driver class exporting the remote services of the {@code PingServer}
 * object.
 */
public class ServerDriver {

	/**
	 * The thread for graceful termination of services exposed by the
	 * {@code PingServer} instance.
	 */
	private static class ShutdownThread extends Thread {

		/**
		 * List of skeletons to terminate.
		 */
		ArrayList<Skeleton> skeletons;

		/**
		 * Create a new instance of {@code ShutdownThread}.
		 * 
		 * @param skeletons list of skeletons to terminate
		 */
		public ShutdownThread(ArrayList<Skeleton> skeletons) {
			this.skeletons = skeletons;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public void run() {
			System.out.println("Stopping PingServer skeleton...");
			for (Skeleton s : skeletons) {
				s.stop();
			}
			System.out.println("Done. Bye!");
		}
	}

	public static void main(String[] args) {

		int port = Integer.parseInt(args[0]);
		IPingServer server = PingServerFactory.makePingServer();
		Skeleton<IPingServer> skeleton = new Skeleton<IPingServer>(IPingServer.class, server,
				new InetSocketAddress(port));

		ArrayList<Skeleton> skeletons = new ArrayList<>();
		skeletons.add(skeleton);

		Runtime.getRuntime().addShutdownHook(new ShutdownThread(skeletons));

		try {
			skeleton.start();
			System.out.println("PingServer skeleton is listening on port " + port + "...");
		} catch (RMIException e) {
			e.printStackTrace();
		}

	}

}
