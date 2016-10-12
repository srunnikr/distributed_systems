/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi.server;

/**
 * The factory class to generate instances of {@code PingServer} class.
 */
public class PingServerFactory {

	/**
	 * Returns a new {@code PingServer} instance.
	 * 
	 * @return {@code PingServer} instance
	 */
	public static PingServer makePingServer() {
		return new PingServer();
	}

}
