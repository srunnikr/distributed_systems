/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi.server;

import rmi.RMIException;

/**
 * The remote interface for the {@code PingServer}.
 */
public interface IPingServer {

	/**
	 * The ping call to the {@code IPingServer} instance.
	 * 
	 * @param idNumber
	 *            ping id number
	 * @return ping response
	 * @throws RMIException
	 *             exception throws by the remote ping method call
	 */
	public String ping(int idNumber) throws RMIException;

}
