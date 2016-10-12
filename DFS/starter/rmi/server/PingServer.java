/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi.server;

import rmi.RMIException;

/**
 * The concrete class implementing {@code IPingServer} interface.
 */
public class PingServer implements IPingServer {

	/**
	 * {@inheritDoc}
	 */
	public String ping(int idNumber) throws RMIException {
		return ("Pong " + idNumber);
	}

}
