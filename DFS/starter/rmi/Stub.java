/**
 * @author Karthikeyan Vasuki Balasubramaniam (kvasukib@cs.ucsd.edu)
 */

package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import rmi.io.RMIRequest;
import rmi.io.RMIResponse;

/**
 * RMI stub factory.
 *
 * <p>
 * RMI stubs hide network communication with the remote server and provide a
 * simple object-like interface to their users. This class provides methods for
 * creating stub objects dynamically, when given pre-defined interfaces.
 *
 * <p>
 * The network address of the remote server is set when a stub is created, and
 * may not be modified afterwards. Two stubs are equal if they implement the
 * same interface and carry the same remote server address - and would therefore
 * connect to the same skeleton. Stubs are serializable.
 */
public abstract class Stub {
	/**
	 * RMI StubInvocationHandler
	 *
	 * <p>
	 * An implementation of the InvocationHandler interface for use with Remote
	 * Method Invocation (RMI). This invocation handler can be used in
	 * conjunction with a dynamic proxy instance as a replacement for a
	 * pregenerated stub class. Applications are not expected to use this class
	 * directly. A remote object exported to use a dynamic proxy with
	 * UnicastRemoteObject or Activatable has an instance of this class as that
	 * proxy's invocation handler.
	 *
	 */
	private static class StubInvocationHandler implements Serializable, InvocationHandler {

		private static final long serialVersionUID = -9213645686207656988L;

		private InetSocketAddress serverSocketAddress;
		private Class<?> c;

		/**
		 * Creates a new StubInvocationHandler constructed with the specified
		 * InetSocketAddress and the class.
		 *
		 * @param c
		 *            A <code>Class</code> object representing the interface
		 *            implemented by the remote object.
		 * @param address
		 *            The network address of the remote skeleton.
		 */
		public StubInvocationHandler(InetSocketAddress address, Class<?> c) {
			this.serverSocketAddress = address;
			this.c = c;
		}

		/**
		 * <p>
		 * Processes a method invocation made on the encapsulating proxy
		 * instance, proxy, and returns the result.
		 *
		 * @param proxy
		 *            The proxy instance that the method was invoked on
		 * @param method
		 *            The Method instance corresponding to the interface method
		 *            invoked on the proxy instance
		 * @param args
		 *            An array of objects containing the values of the arguments
		 *            passed in the method invocation on the proxy instance, or
		 *            null if the method takes no arguments
		 * @return The value to return from the method invocation on the proxy
		 *         instance
		 * @throws Throwable
		 *             The exception to throw from the method invocation on the
		 *             proxy instance
		 *
		 */
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			try {
				if (RemotePattern.isRemoteMethod(method)) {
					return remoteInvoke(proxy, method, args);
				} else {
					return localInvoke(proxy, method, args);
				}
			} catch (Throwable e) {
				throw e;
			}
		}

		/**
		 * <p>
		 * Processes a remote method invocation made on the encapsulating proxy
		 * instance, proxy, and returns the result.
		 *
		 * @param proxy
		 *            The proxy instance that the method was invoked on
		 * @param method
		 *            The Method instance corresponding to the interface method
		 *            invoked on the proxy instance
		 * @param args
		 *            An array of objects containing the values of the arguments
		 *            passed in the method invocation on the proxy instance, or
		 *            null if the method takes no arguments
		 * @return The value to return from the method invocation on the proxy
		 *         instance
		 * @throws Throwable
		 *             The exception to throw from the method invocation on the
		 *             proxy instance
		 *
		 */
		@SuppressWarnings("rawtypes")
		private Object remoteInvoke(Object proxy, Method method, Object[] args) throws Throwable {
			Socket socket = null;
			ObjectOutputStream out;
			ObjectInputStream in;
			RMIRequest request;
			RMIResponse response;

			try {
				socket = new Socket();
				socket.connect(serverSocketAddress);
			} catch (IOException e) {
				//System.err.println("Failed to connect to server skeleton.");
				closeConnection(socket);
				throw (Throwable) (new RMIException(e));
			}

			try {
				out = new ObjectOutputStream(socket.getOutputStream());
				out.flush();
			} catch (IOException e) {
				closeConnection(socket);
				//System.err.println("Failed to connect to get OutputStream from socket.");
				throw (Throwable) (new RMIException(e));
			}
			try {
				in = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				closeConnection(socket);
				//System.err.println("Failed to connect to get InputStream from socket.");
				throw (Throwable) (new RMIException(e));
			}

			String[] argumentTypes = getArgumentTypes(method);
			// System.err.println("Calling Remote Method: " +
			// method.getDeclaringClass().getName() + "." + method.getName()
			// + "(" + args + " : " + argumentTypes.toString() + ")");
			request = new RMIRequest(method.getDeclaringClass().getName(), method.getName(), args, argumentTypes);
			try {
				out.writeObject(request);
			} catch (IOException e) {
				closeConnection(socket);
				System.err.println("Failed to write request to socket.");
				throw (Throwable) (new RMIException(e));
			}

			try {
				response = (RMIResponse) in.readObject();
			} catch (ClassNotFoundException | IOException e) {
				closeConnection(socket);
				System.err.println("Failed to read response from socket.");
				throw (Throwable) (new RMIException(e));
			}

			closeConnection(socket);

			if (response.getException() == null) {
				return response.getReturnValue();
			} else {
				// System.out.println("Remote method execution threw an
				// exception." + response.getException().getClass().getName());
				throw (Throwable) response.getException();
			}

		}

		/**
		 * <p>
		 * This function returns the hash code value of the proxy
		 *
		 * @return The hashCode value as an integer
		 *
		 */
		public int hashCode() {
			return toString().hashCode();
		}

		/**
		 * <p>
		 * Returns true if the argument is an instance of a dynamic proxy class
		 * and this invocation handler is equal to the invocation handler of
		 * that argument, and returns false otherwise.
		 *
		 * @return The boolean value after comparison
		 *
		 */
		public boolean equals(Object proxy) {

			if (proxy == null) {
				return false;
			}

			if (!Proxy.isProxyClass(proxy.getClass())) {
				return false;
			}

			InvocationHandler sih = Proxy.getInvocationHandler(proxy);
			if (!(sih instanceof StubInvocationHandler)) {
				return false;
			}

			if (!serverSocketAddress.equals(((StubInvocationHandler) sih).serverSocketAddress)) {
				return false;
			}

			if (this.c.getName() != ((StubInvocationHandler) sih).c.getName()) {
				return false;
			}

			return true;

		}

		/**
		 * <p>
		 * This function returns a string representation of the proxy.
		 *
		 * @return The string representation of the proxy
		 *
		 */
		public String toString() {
			//System.err.println("PORT : " + serverSocketAddress.getPort() + " HOSTNAME : "
			//		+ serverSocketAddress.getHostName() + " INTERFACE-NAME : " + c.getName());
			return "PORT : " + serverSocketAddress.getPort() + " HOSTNAME : " + serverSocketAddress.getHostName()
					+ " INTERFACE-NAME : " + c.getName();
		}

		/**
		 * <p>
		 * Processes a local method invocation made on the encapsulating proxy
		 * instance, proxy, and returns the result.
		 *
		 * @param proxy
		 *            The proxy instance that the method was invoked on
		 * @param method
		 *            The Method instance corresponding to the interface method
		 *            invoked on the proxy instance
		 * @param args
		 *            An array of objects containing the values of the arguments
		 *            passed in the method invocation on the proxy instance, or
		 *            null if the method takes no arguments
		 * @return The value to return from the method invocation on the proxy
		 *         instance
		 * @throws Throwable
		 *             The exception to throw from the method invocation on the
		 *             proxy instance
		 *
		 */
		private Object localInvoke(Object proxy, Method method, Object[] args) throws Throwable {
			StubInvocationHandler sih = (StubInvocationHandler) Proxy.getInvocationHandler(proxy);

			return method.invoke(sih, args);
		}

		/**
		 * <p>
		 * This function accepts the socket and closes the connection associated
		 * with the socket
		 *
		 * @param socket
		 *            The socket used for communication between the skeleton and
		 *            the stub
		 *
		 */
		private void closeConnection(Socket socket) {
			try {
				socket.close();
			} catch (IOException e) {
				System.err.println("Failed to close socket.");
			}
		}

		/**
		 * <p>
		 * This function takes a method object and returns the array of strings
		 * where each string is the type of an argument of the method
		 *
		 * @param method
		 *            The method object
		 *
		 * @return Array of strings of argument types of method
		 *
		 */
		private String[] getArgumentTypes(Method method) {
			ArrayList<String> list = new ArrayList<>();
			for (Class clazz : method.getParameterTypes()) {
				list.add(clazz.getName());
			}

			String[] argumentTypes = new String[list.size()];

			return (String[]) list.toArray(argumentTypes);
		}
	}

	/**
	 * Creates a stub, given a skeleton with an assigned adress.
	 *
	 * <p>
	 * The stub is assigned the address of the skeleton. The skeleton must
	 * either have been created with a fixed address, or else it must have
	 * already been started.
	 *
	 * <p>
	 * This method should be used when the stub is created together with the
	 * skeleton. The stub may then be transmitted over the network to enable
	 * communication with the skeleton.
	 *
	 * @param c
	 *            A <code>Class</code> object representing the interface
	 *            implemented by the remote object.
	 * @param skeleton
	 *            The skeleton whose network address is to be used.
	 * @param <T>
	 *            Generic class typeparameter
	 * @return The stub created.
	 * @throws IllegalStateException
	 *             If the skeleton has not been assigned an address by the user
	 *             and has not yet been started.
	 * @throws NullPointerException
	 *             If any argument is <code>null</code>.
	 * @throws Error
	 *             If <code>c</code> does not represent a remote interface - an
	 *             interface in which each method is marked as throwing
	 *             <code>RMIException</code>, or if an object implementing this
	 *             interface cannot be dynamically created.
	 */
	public static <T> T create(Class<T> c, Skeleton<T> skeleton) {
		if (c == null || skeleton == null) {
			throw new NullPointerException("Paramaters of create method should be non-null.");
		}

		if (!RemotePattern.isRemoteInterface(c)) {
			throw new Error("c is not a remote interface.");
		}

		InetSocketAddress remoteAddress = skeleton.getBindAddress();
		if (remoteAddress == null) {
			throw new IllegalStateException();
		}

		return doCreate(c, remoteAddress);
	}

	/**
	 * Creates a stub, given a skeleton with an assigned address and a hostname
	 * which overrides the skeleton's hostname.
	 *
	 * <p>
	 * The stub is assigned the port of the skeleton and the given hostname. The
	 * skeleton must either have been started with a fixed port, or else it must
	 * have been started to receive a system-assigned port, for this method to
	 * succeed.
	 *
	 * <p>
	 * This method should be used when the stub is created together with the
	 * skeleton, but firewalls or private networks prevent the system from
	 * automatically assigning a valid externally-routable address to the
	 * skeleton. In this case, the creator of the stub has the option of
	 * obtaining an externally-routable address by other means, and specifying
	 * this hostname to this method.
	 *
	 * @param c
	 *            A <code>Class</code> object representing the interface
	 *            implemented by the remote object.
	 * @param skeleton
	 *            The skeleton whose port is to be used.
	 * @param hostname
	 *            The hostname with which the stub will be created.
	 * @param <T>
	 *            Generic class typeparameter
	 * @return The stub created.
	 * @throws IllegalStateException
	 *             If the skeleton has not been assigned a port.
	 * @throws NullPointerException
	 *             If any argument is <code>null</code>.
	 * @throws Error
	 *             If <code>c</code> does not represent a remote interface - an
	 *             interface in which each method is marked as throwing
	 *             <code>RMIException</code>, or if an object implementing this
	 *             interface cannot be dynamically created.
	 */
	public static <T> T create(Class<T> c, Skeleton<T> skeleton, String hostname) {
		if (c == null || skeleton == null || hostname == null) {
			throw new NullPointerException("Paramater of create should be non-null.");
		}

		if (!RemotePattern.isRemoteInterface(c)) {
			throw new Error("c is not a remote interface.");
		}

		InetSocketAddress address = skeleton.getBindAddress();
		if (address == null) {
			throw new IllegalStateException("skeleton is not assigned a port.");
		}

		InetSocketAddress remoteAddress = new InetSocketAddress(hostname, address.getPort());

		return doCreate(c, remoteAddress);
	}

	/**
	 * Creates a stub, given the address of a remote server.
	 *
	 * <p>
	 * This method should be used primarily when bootstrapping RMI. In this
	 * case, the server is already running on a remote host but there is not
	 * necessarily a direct way to obtain an associated stub.
	 *
	 * @param c
	 *            A <code>Class</code> object representing the interface
	 *            implemented by the remote object.
	 * @param address
	 *            The network address of the remote skeleton.
	 * @param <T>
	 *            Generic class typeparameter
	 * @return The stub created.
	 * @throws NullPointerException
	 *             If any argument is <code>null</code>.
	 * @throws Error
	 *             If <code>c</code> does not represent a remote interface - an
	 *             interface in which each method is marked as throwing
	 *             <code>RMIException</code>, or if an object implementing this
	 *             interface cannot be dynamically created.
	 */
	public static <T> T create(Class<T> c, InetSocketAddress address) {
		if (c == null || address == null) {
			throw new NullPointerException("Paramater of create should be non-null.");
		}

		if (!RemotePattern.isRemoteInterface(c)) {
			throw new Error("c is not a remote interface.");
		}

		return doCreate(c, address);
	}

	/**
	 * <p>
	 * Stub.create calls doCreate function with the class and InetSocketAddress
	 * The function creates a proxy instance with the StubInvocationHandler
	 *
	 * @param c
	 *            A <code>Class</code> object representing the interface
	 *            implemented by the remote object.
	 * @param <T>
	 *            Generic class typeparameter
	 * @param address
	 *            The network address of the remote skeleton.
	 * @return The stub created.
	 *
	 */
	@SuppressWarnings("unchecked")
	private static <T> T doCreate(Class<T> c, InetSocketAddress address) {
		InvocationHandler invocationHandler = new StubInvocationHandler(address, c);
		T instance = (T) Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[] { c, Serializable.class },
				invocationHandler);
		return instance;
	}
}
