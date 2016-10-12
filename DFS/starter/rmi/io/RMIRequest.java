/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi.io;

import java.io.Serializable;

/**
 * The {@code Serializable} class representing the remote method call sent from
 * stub to the skeleton.
 */
public class RMIRequest implements Serializable {

	private static final long serialVersionUID = -2462856798587765532L;

	/**
	 * The remote interface class name on which the remote method is called.
	 */
	private String className;
	/**
	 * The remote method name that has been called.
	 */
	private String methodName;
	/**
	 * The arguments objects passed to the remote method call.
	 */
	private Object[] arguments;
	/**
	 * The argument type names for the arguments being passed to the remote
	 * method.
	 */
	private String[] argumentTypes;

	/**
	 * The constructor to create an instance of {@code RMIRequest}.
	 * 
	 * @param className
	 * @param methodName
	 * @param arguments
	 * @param argumentTypes
	 */
	public RMIRequest(String className, String methodName, Object[] arguments, String[] argumentTypes) {
		this.className = className;
		this.methodName = methodName;
		this.arguments = ((arguments == null) ? new Object[] {} : arguments);
		this.argumentTypes = ((argumentTypes == null) ? new String[] {} : argumentTypes);
	}

	/**
	 * Returns the class name.
	 * 
	 * @return class name
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Returns the method name.
	 * 
	 * @return method name
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Returns the arguments of the remote method.
	 * 
	 * @return {@code Array} of {@code Object} arguments
	 */
	public Object[] getArguments() {
		return arguments;
	}

	/**
	 * Returns the argument type names of the remote method.
	 * 
	 * @return {@code Array} of argument type names
	 */
	public String[] getArgumentTypes() {
		return argumentTypes;
	}

}
