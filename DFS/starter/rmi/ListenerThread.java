/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The listener thread that hosts the server socket and accepts and services
 * multiple connections in {@link MethodInvocationTask} worker threads for each
 * remote method call made on other skeleton object.
 *
 * @param <T>
 *            the generic remote interface this listener thread represents.
 */
public class ListenerThread<T> extends Thread {

	/**
	 * The skeleton object that this listener thread instance represents.
	 */
	private Skeleton<T> container;
	/**
	 * The server object on which remote method calls are executed.
	 */
	private T serverObject;
	/**
	 * The remote interface class object represented by this listener thread.
	 */
	private Class<T> serverClass;
	/**
	 * The server socket on which the listener listens for connections.
	 */
	private ServerSocket listenerSocket;
	/**
	 * The boolean status representing if the listener thread is
	 * running/listening or not.
	 */
	private boolean isActive;
	/**
	 * The cause due to which the listener thread terminated. {@code null} if
	 * termination was requested and not due to an exception.
	 */
	private Throwable cause;
	/**
	 * The thread pool running the worker threads that service individual remote
	 * method calls.
	 */
	private ExecutorService threadPool = Executors.newCachedThreadPool();

	/**
	 * The constructor for the listener thread.
	 * 
	 * @param container
	 *		The skeleton object
	 * @param serverClass
	 *		The class object of server
	 * @param serverObject
	 *		The generic server object
	 * @param listenerSocket
	 *		The listener socket
	 */
	public ListenerThread(Skeleton<T> container, Class<T> serverClass, T serverObject, ServerSocket listenerSocket) {
		this.container = container;
		this.serverObject = serverObject;
		this.serverClass = serverClass;
		this.listenerSocket = listenerSocket;
		this.isActive = false;
		this.cause = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {

		isActive = true;
		while (isActive) {
			Socket clientConnection = null;
			try {
				clientConnection = listenerSocket.accept();
			} catch (SocketException e) {
				if (!listenerSocket.isClosed()) {
					System.err.println("Failed to accept client connection: " + "ServerClass: " + serverClass.getName()
							+ ", " + "IPAddress: "
							+ ((InetSocketAddress) listenerSocket.getLocalSocketAddress()).getHostString() + ", "
							+ "Port: " + ((InetSocketAddress) listenerSocket.getLocalSocketAddress()).getPort());
					isActive = container.listen_error(e);
					if (!isActive) {
						cause = (Throwable) e;
						closeConnection();
						break;
					}
				} else {
					// Do nothing; it is an expected behaviour.
				}
			} catch (IOException e) {
				System.err.println("Failed to accept client connection: " + "ServerClass: " + serverClass.getName()
						+ ", " + "IPAddress: "
						+ ((InetSocketAddress) listenerSocket.getLocalSocketAddress()).getHostString() + ", " + "Port: "
						+ ((InetSocketAddress) listenerSocket.getLocalSocketAddress()).getPort());
				isActive = container.listen_error(e);
				if (!isActive) {
					cause = (Throwable) e;
					closeConnection();
					break;
				}
			}

			MethodInvocationTask<T> handler = new MethodInvocationTask<T>(container, serverObject, serverClass,
					clientConnection);
			threadPool.execute(handler);
		}

		//System.out.println("Shutting down thread pool...");
		threadPool.shutdown();
		if (!threadPool.isTerminated()) {
			//System.err.println("Force terminating thread pool...");
			threadPool.shutdownNow();
		}

		//System.out.println("Thread pool terminated.");

		closeConnection();
		container.confirmTermination(cause);
	}

	/**
	 * The method to initiate termination of the listener thread.
	 */
	public void terminate() {
		if (this.isActive) {
			this.isActive = false;
			closeConnection();
		} else {
			closeConnection();
			container.confirmTermination(null);
		}
	}

	/**
	 * The method to close the server socket to prevent it from accepting
	 * anymore connections.
	 */
	private void closeConnection() {
		try {
			listenerSocket.close();
		} catch (IOException e) {
			System.err.println("Failed to close listener socket. Ignoring the exception: " + "ServerClass: "
					+ serverClass.getName() + ", " + "IPAddress: "
					+ ((InetSocketAddress) listenerSocket.getLocalSocketAddress()).getHostString() + ", " + "Port: "
					+ ((InetSocketAddress) listenerSocket.getLocalSocketAddress()).getPort());
			e.printStackTrace();
		}
	}

}
