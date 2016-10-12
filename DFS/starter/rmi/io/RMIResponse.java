/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi.io;

import java.io.Serializable;

/**
 * The {@code Serializable} class representing the remote method call response
 * including the return or exception from the skeleton to the stub.
 */
public class RMIResponse implements Serializable {

	private static final long serialVersionUID = -8899349477943341489L;

	/**
	 * The return value of the remote method call.
	 */
	private Object returnValue;
	/**
	 * The exception thrown by the remote method call.
	 */
	private Exception exception;

	/**
	 * The constructor to create an instance of {@code RMIResponse}.
	 */
	public RMIResponse(Object returnValue) {
		this.returnValue = returnValue;
		this.exception = null;
	}

	/**
	 * The constructor to create an instance of {@code RMIResponse}.
	 */
	public RMIResponse(Exception exception) {
		this.returnValue = null;
		this.exception = exception;
	}

	/**
	 * Returns the return value object of the remote method.
	 * 
	 * @return return value
	 */
	public Object getReturnValue() {
		return returnValue;
	}

	/**
	 * Returns the exception thrown by the remote method.
	 * 
	 * @return exception
	 */
	public Exception getException() {
		return exception;
	}

}
