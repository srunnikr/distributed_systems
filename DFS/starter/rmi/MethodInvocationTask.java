/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import rmi.io.RMIRequest;
import rmi.io.RMIResponse;

/**
 * The runnable object executed in the thread pool that services a client
 * connection to execute the remote method call on the {@link Skeleton} object.
 *
 * @param <T>
 *            the generic remote interface this listener thread represents.
 */
public class MethodInvocationTask<T> implements Runnable {

	/**
	 * The skeleton object on which the remote method call is executed.
	 */
	private Skeleton<T> container;
	/**
	 * The object that the skeleton and this {@link Runnable} task represents.
	 */
	private T serverObject;
	/**
	 * The remote interface class object that the skeleton and this
	 * {@link Runnable} task represents.
	 */
	private Class<T> serverClass;
	/**
	 * The client connection that the task accepts remote method calls and
	 * returns the response on.
	 */
	private Socket clientConnection;

	/**
         * The constructor to create a new {@link MethodInvocationTask} instance.
         *
         * @param container
         *              The skeleton object
         * @param serverClass
         *              The class object of server
         * @param serverObject
         *              The generic server object
         * @param clientConnection
         *              The listener socket
         */
	public MethodInvocationTask(Skeleton<T> container, T serverObject, Class<T> serverClass, Socket clientConnection) {
		this.container = container;
		this.serverObject = serverObject;
		this.serverClass = serverClass;
		this.clientConnection = clientConnection;
	}

	/**
	 * The method returns a member {@link Method} instance of the given remote
	 * interface that matches with the given method signature provided in the
	 * remote call requests.
	 * 
	 * @param clazz
	 *            The remote interface class object that this
	 *            {@link MethodInvocationTask} represents.
	 * @param methodName
	 *            The remote method name that has been requested in the remote
	 *            call.
	 * @param argumentTypes
	 *            The expected argument types of the remote method to be called.
	 * @return {@link Method} instance; {@code null} if not such method is
	 *         found.
	 */
	private Method getMatchingMethod(Class clazz, String methodName, String[] argumentTypes) {
		for (Method method : clazz.getDeclaredMethods()) {
			Class[] paramTypes = method.getParameterTypes();
			if (methodName.equals(method.getName()) && (argumentTypes.length == paramTypes.length)) {
				boolean found = true;
				for (int i = 0; i < argumentTypes.length; i++) {
					if (!paramTypes[i].getName().equals(argumentTypes[i])) {
						found = false;
					}
				}

				if (found) {
					return method;
				}
			}
		}

		for (Class iface : clazz.getInterfaces()) {
			Method method = getMatchingMethod(iface, methodName, argumentTypes);
			if (method != null) {
				return method;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		ObjectOutputStream outStream;
		ObjectInputStream inStream;

		try {
			outStream = new ObjectOutputStream(clientConnection.getOutputStream());
			outStream.flush();
		} catch (IOException e) {
			//System.err.println("Failed to get OutputStream from client connection: " + "ServerClass: "
			//		+ serverClass.getName() + ", " + "IPAddress: " + container.getBindAddress().getAddress().toString()
			//		+ ", " + "Port: " + container.getBindAddress().getPort());

			container.service_error(new RMIException(e));

			closeConnection();
			return; // Nothing can be done so simply exit.
		} catch (NullPointerException e) {
			container.service_error(new RMIException(e));

			closeConnection();
			return; // Nothing can be done so simply exit.
		}
		try {
			inStream = new ObjectInputStream(clientConnection.getInputStream());
		} catch (IOException e) {
			//System.err.println("Failed to get IntputStream from client connection: " + "ServerClass: "
			//		+ serverClass.getName() + ", " + "IPAddress: " + container.getBindAddress().getAddress().toString()
			//		+ ", " + "Port: " + container.getBindAddress().getPort());

			container.service_error(new RMIException(e));

			closeConnection();
			return; // Nothing can be done so simply exit.
		} catch (NullPointerException e) {
			container.service_error(new RMIException(e));

			closeConnection();
			return; // Nothing can be done so simply exit.
		}

		RMIRequest request;
		try {
			request = (RMIRequest) inStream.readObject();
		} catch (ClassNotFoundException | IOException e) {
			System.err.println("Failed to get read request from client connection: " + "ServerClass: "
					+ serverClass.getName() + ", " + "IPAddress: " + container.getBindAddress().getAddress().toString()
					+ ", " + "Port: " + container.getBindAddress().getPort());
			container.service_error(new RMIException(e));

			closeConnection();
			return; // Nothing can be done so simply exit.
		}

		RMIResponse response;
		String className = request.getClassName();
		String methodName = request.getMethodName();
		Object[] arguments = request.getArguments();
		String[] argumentTypes = request.getArgumentTypes();

		if (isAncestorOrEqual(serverClass, className)) {

			Method matchingMethod = getMatchingMethod(serverClass, methodName, argumentTypes);
			if (matchingMethod != null) {
				try {
					Object returnValue = matchingMethod.invoke(serverObject, arguments);
					response = new RMIResponse(returnValue);
				} catch (InvocationTargetException e) {
					response = new RMIResponse((Exception) e.getTargetException());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					System.err.println(
							"Failed to invoke the designated method: " + "ServerClass: " + serverClass.getName() + ", "
									+ "IPAddress: " + container.getBindAddress().getAddress().toString() + ", "
									+ "Port: " + container.getBindAddress().getPort() + ", " + "ClientClass: "
									+ className + ", " + "Method: " + methodName + ", " + "Arguments: " + arguments);
					RMIException exception = new RMIException(e);
					container.service_error(exception);
					response = new RMIResponse(exception);
				}
			} else {
				System.err.println("Failed to find a matching method: " + "ServerClass: " + serverClass.getName() + ", "
						+ "IPAddress: " + container.getBindAddress().getAddress().toString() + ", " + "Port: "
						+ container.getBindAddress().getPort() + ", " + "ClientClass: " + className + ", " + "Method: "
						+ methodName + ", " + "Arguments: " + arguments);
				RMIException exception = new RMIException(new NoSuchMethodException("No such remote method."));
				container.service_error(exception);
				response = new RMIResponse(exception);
			}
		} else {
			System.err.println("Failed to find a matching class: " + "ServerClass: " + serverClass.getName() + ", "
					+ "IPAddress: " + container.getBindAddress().getAddress().toString() + ", " + "Port: "
					+ container.getBindAddress().getPort() + ", " + "ClientClass: " + className + ", " + "Method: "
					+ methodName + ", " + "Arguments: " + arguments);
			RMIException exception = new RMIException(new ClassNotFoundException("No such remote class."));
			container.service_error(exception);
			response = new RMIResponse(exception);
		}

		try {
			outStream.writeObject(response);
		} catch (IOException e) {
			System.err.println("Failed to write response to client connection: " + "ServerClass: "
					+ serverClass.getName() + ", " + "IPAddress: " + container.getBindAddress().getAddress().toString()
					+ ", " + "Port: " + container.getBindAddress().getPort() + ", " + "ClientClass: " + className + ", "
					+ "Method: " + methodName + ", " + "Arguments: " + arguments);
			container.service_error(new RMIException(e));
		}

		closeConnection();
	}

	/**
	 * The methods checks if the expected {@code ancestorClass} represents the
	 * interface either equal or inherited by the {@code currentClass} interface
	 * class object.
	 * 
	 * @param currentClass
	 *            the class object whose ancestry needs to searched in.
	 * @param ancestorClassName
	 *            the class to be expected in the ancestry of the
	 *            {@code currentClass}
	 * @return {@code true} if the expected interface is in the given classes
	 *         hierarchy; {@code false} otherwise.
	 */
	private boolean isAncestorOrEqual(Class currentClass, String ancestorClassName) {
		if (currentClass.getName().equals(ancestorClassName)) {
			return true;
		} else {
			for (Class iface : serverClass.getInterfaces()) {
				if (isAncestorOrEqual(iface, ancestorClassName)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * The method closes the client connection for the remote method call.
	 */
	private void closeConnection() {
		try {
			clientConnection.close();
		} catch (IOException e) {
			System.err.println("Failed to close client connection: " + "ServerClass: " + serverClass.getName() + ", "
					+ "IPAddress: " + container.getBindAddress().getAddress().toString() + ", " + "Port: "
					+ container.getBindAddress().getPort());

			container.service_error(new RMIException(e));
		} catch (NullPointerException e) {
			container.service_error(new RMIException(e));
		}
	}

}
