/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi.client;

import java.net.InetSocketAddress;

import rmi.RMIException;
import rmi.Stub;
import rmi.server.IPingServer;

/**
 * The driver class to ping the remote {@code PingServer} instance.
 */
public class ClientDriver {

	public static void main(String[] args) {

		String serverIPAddress = args[0];
		int port = Integer.parseInt(args[1]);
		IPingServer server = Stub.create(IPingServer.class, new InetSocketAddress(serverIPAddress, port));

		int correctCount = 0;
		for (int i = 1; i <= 4; i++) {
			boolean result;
			String expectedResponseString = "Pong " + i;
			String responseString = null;
			try {
				responseString = server.ping(i);
				if (expectedResponseString.equals(responseString)) {
					result = true;
				} else {
					result = false;
				}
			} catch (RMIException e) {
				result = false;
			}

			if (result) {
				correctCount++;
			}
		}

		System.out.println(correctCount + " Tests Completed, " + (4 - correctCount) + " Tests Failed.");
	}

}
