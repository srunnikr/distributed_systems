/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 * RMI skeleton
 * 
 * <p>
 * A skeleton encapsulates a multithreaded TCP server. The server's clients are
 * intended to be RMI stubs created using the <code>Stub</code> class.
 * 
 * <p>
 * The skeleton class is parametrized by a type variable. This type variable
 * should be instantiated with an interface. The skeleton will accept from the
 * stub requests for calls to the methods of this interface. It will then
 * forward those requests to an object. The object is specified when the
 * skeleton is constructed, and must implement the remote interface. Each method
 * in the interface should be marked as throwing <code>RMIException</code>, in
 * addition to any other exceptions that the user desires.
 * 
 * <p>
 * Exceptions may occur at the top level in the listening and service threads.
 * The skeleton's response to these exceptions can be customized by deriving a
 * class from <code>Skeleton</code> and overriding <code>listen_error</code> or
 * <code>service_error</code>.
 */
public class Skeleton<T> {

	/**
	 * Max waiting connection queue length for server socket
	 */
	public static final int maxQueueLength = 50;

	/**
	 * The server object on which the skeleton executes the remote method calls
	 * on.
	 */
	private T serverObject;
	/**
	 * The remote interface class object represented by the skeleton.
	 */
	private Class<T> serverClass;
	/**
	 * The socket address on which the listener socket binds to accept
	 * connections.
	 */
	private InetSocketAddress bindAddress;
	/**
	 * The listener thread that implements the socket listener that listens on
	 * the given bind address and spawns handler threads.
	 */
	private ListenerThread<T> listener;
	/**
	 * The listener socket on which skeleton accepts connections for remote
	 * calls.
	 */
	private ServerSocket listenerSocket;
	/**
	 * A boolean status of whether the skeleton is running/listening or not.
	 */
	private boolean isActive;

	/**
	 * Creates a <code>Skeleton</code> with no initial server address. The
	 * address will be determined by the system when <code>start</code> is
	 * called. Equivalent to using <code>Skeleton(null)</code>.
	 * 
	 * <p>
	 * This constructor is for skeletons that will not be used for bootstrapping
	 * RMI - those that therefore do not require a well-known port.
	 * 
	 * @param c
	 *            An object representing the class of the interface for which
	 *            the skeleton server is to handle method call requests.
	 * @param server
	 *            An object implementing said interface. Requests for method
	 *            calls are forwarded by the skeleton to this object.
	 * @throws Error
	 *             If <code>c</code> does not represent a remote interface - an
	 *             interface whose methods are all marked as throwing
	 *             <code>RMIException</code>.
	 * @throws NullPointerException
	 *             If either of <code>c</code> or <code>server</code> is
	 *             <code>null</code>.
	 */
	public Skeleton(Class<T> c, T server) {

		if (c == null) {
			throw new NullPointerException("Server object's class cannot be null.");
		}

		if (server == null) {
			throw new NullPointerException("Server object cannot be null.");
		}

		if (!RemotePattern.isRemoteInterface(c)) {
			throw new Error("Not a remote interface: " + "ClassName: " + c.getName());
		}

		this.serverObject = server;
		this.serverClass = c;
		this.bindAddress = null;
		this.listenerSocket = null;
		this.listener = null;
		this.isActive = false;
	}

	/**
	 * Creates a <code>Skeleton</code> with the given initial server address.
	 * 
	 * <p>
	 * This constructor should be used when the port number is significant.
	 * 
	 * @param c
	 *            An object representing the class of the interface for which
	 *            the skeleton server is to handle method call requests.
	 * @param server
	 *            An object implementing said interface. Requests for method
	 *            calls are forwarded by the skeleton to this object.
	 * @param address
	 *            The address at which the skeleton is to run. If
	 *            <code>null</code>, the address will be chosen by the system
	 *            when <code>start</code> is called.
	 * @throws Error
	 *             If <code>c</code> does not represent a remote interface - an
	 *             interface whose methods are all marked as throwing
	 *             <code>RMIException</code>.
	 * @throws NullPointerException
	 *             If either of <code>c</code> or <code>server</code> is
	 *             <code>null</code>.
	 */
	public Skeleton(Class<T> c, T server, InetSocketAddress address) {

		if (c == null) {
			throw new NullPointerException("Server object's class cannot be null.");
		}

		if (server == null) {
			throw new NullPointerException("Server object cannot be null.");
		}

		if (!RemotePattern.isRemoteInterface(c)) {
			throw new Error("Not a remote interface: " + "ClassName: " + c.getName());
		}

		this.serverObject = server;
		this.serverClass = c;
		this.bindAddress = address;
		this.listenerSocket = null;
		this.listener = null;
		this.isActive = false;
	}

	/**
	 * Called when the listening thread exits.
	 * 
	 * <p>
	 * The listening thread may exit due to a top-level exception, or due to a
	 * call to <code>stop</code>.
	 * 
	 * <p>
	 * When this method is called, the calling thread owns the lock on the
	 * <code>Skeleton</code> object. Care must be taken to avoid deadlocks when
	 * calling <code>start</code> or <code>stop</code> from different threads
	 * during this call.
	 * 
	 * <p>
	 * The default implementation does nothing.
	 * 
	 * @param cause
	 *            The exception that stopped the skeleton, or <code>null</code>
	 *            if the skeleton stopped normally.
	 */
	protected void stopped(Throwable cause) {
		// Do nothing
	}

	/**
	 * Called when an exception occurs at the top level in the listening thread.
	 * 
	 * <p>
	 * The intent of this method is to allow the user to report exceptions in
	 * the listening thread to another thread, by a mechanism of the user's
	 * choosing. The user may also ignore the exceptions. The default
	 * implementation simply stops the server. The user should not use this
	 * method to stop the skeleton. The exception will again be provided as the
	 * argument to <code>stopped</code>, which will be called later.
	 * 
	 * @param exception
	 *            The exception that occurred.
	 * @return <code>true</code> if the server is to resume accepting
	 *         connections, <code>false</code> if the server is to shut down.
	 */
	protected boolean listen_error(Exception exception) {
		return false;
	}

	/**
	 * Called when an exception occurs at the top level in a service thread.
	 * 
	 * <p>
	 * The default implementation does nothing.
	 * 
	 * @param exception
	 *            The exception that occurred.
	 */
	protected void service_error(RMIException exception) {
		//exception.printStackTrace();
	}

	/**
	 * Starts the skeleton server.
	 * 
	 * <p>
	 * A thread is created to listen for connection requests, and the method
	 * returns immediately. Additional threads are created when connections are
	 * accepted. The network address used for the server is determined by which
	 * constructor was used to create the <code>Skeleton</code> object.
	 * 
	 * @throws RMIException
	 *             When the listening socket cannot be created or bound, when
	 *             the listening thread cannot be created, or when the server
	 *             has already been started and has not since stopped.
	 */
	public synchronized void start() throws RMIException {

		if (!isActive) {
			try {
				if (bindAddress != null) {
					listenerSocket = new ServerSocket(bindAddress.getPort(), maxQueueLength, bindAddress.getAddress());
				} else {
					listenerSocket = new ServerSocket(0, maxQueueLength);
					bindAddress = new InetSocketAddress(listenerSocket.getInetAddress(), listenerSocket.getLocalPort());
				}
				listener = new ListenerThread<T>(this, serverClass, serverObject, listenerSocket);
				listener.start();
				isActive = true;
			} catch (IOException e) {
				System.err.println("Failed to bind Skeleton listener: " + "ServerClass: " + serverClass.getName() + ", "
						+ "IPAddress: " + bindAddress.getAddress().toString() + ", " + "Port: "
						+ bindAddress.getPort());
				throw new RMIException(e);
			}
		} else {
			throw new RMIException("Failed to start Skeleton listener. It is already active: " + "ServerClass: "
					+ serverClass.getName() + ", " + "IPAddress: " + bindAddress.getAddress().toString() + ", "
					+ "Port: " + bindAddress.getPort());
		}
	}

	/**
	 * Stops the skeleton server, if it is already running.
	 * 
	 * <p>
	 * The listening thread terminates. Threads created to service connections
	 * may continue running until their invocations of the <code>service</code>
	 * method return. The server stops at some later time; the method
	 * <code>stopped</code> is called at that point. The server may then be
	 * restarted.
	 */
	public synchronized void stop() {
		if (isActive) {
			listener.terminate();
		}
	}

	/**
	 * Returns the socket address on which the skeleton binds and listens for
	 * connections of remote method calls.
	 * 
	 * @return socket address to bind on.
	 */
	public InetSocketAddress getBindAddress() {
		return bindAddress;
	}

	/**
	 * The method is called by the listener thread when it terminates. It is
	 * used to clean-up and update inner state, before stopped is called.
	 * 
	 * @param cause
	 *            the cause of termination of listener thread; <code>null</code>
	 *            if the termination was due to the stop request to the
	 *            skeleton.
	 */
	protected void confirmTermination(Throwable cause) {
		if (cause == null) {
			//System.out.println("The Skeleton has stopped: " + "ServerClass: " + serverClass.getName() + ", "
			//		+ "IPAddress: " + bindAddress.getAddress().toString() + ", " + "Port: " + bindAddress.getPort());
		} else {
			//System.err.println("The Skeleton has stopped due to an exception: " + "ServerClass: "
			//		+ serverClass.getName() + ", " + "IPAddress: " + bindAddress.getAddress().toString() + ", "
			//		+ "Port: " + bindAddress.getPort());
		}
		isActive = false;
		listenerSocket = null;
		listener = null;
		stopped(cause);
	}
}
