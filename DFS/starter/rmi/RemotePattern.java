/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

package rmi;

import java.lang.reflect.Method;

/**
 * The static class representing the patterns that must be followed by remote
 * constructs.
 */
public final class RemotePattern {

	/**
	 * The method to check is the given interface class object is or implements
	 * a remote interface.
	 * 
	 * @param clazz
	 *            the interface class object
	 * @return {@code true} if the interface is a remote interface;
	 *         {@code false} otherwise
	 */
	public static boolean isRemoteInterface(Class clazz) {

		if (!clazz.isInterface()) {
			return false;
		}

		boolean allRemoteMethods = true;
		boolean hasDeclaredMethods = false;
		for (Method method : clazz.getDeclaredMethods()) {
			hasDeclaredMethods = true;
			boolean isRemoteMethod = isRemoteMethod(method);
			if (!isRemoteMethod) {
				allRemoteMethods = false;
				break;
			}
		}

		// It might be an empty interface but it might have ancestor interfaces
		// that are remote.
		boolean hasAncestors = false;
		if (!allRemoteMethods || !hasDeclaredMethods) {
			Class[] implementedInterfaces = clazz.getInterfaces();
			for (Class iface : implementedInterfaces) {
				hasAncestors = true;
				if (isRemoteInterface(iface)) {
					return true;
				}
			}

			// It has ancestors but none of them is a remote interface or else
			// control wouldn't reach here. So, it is also not a remote
			// interface.
			if (hasAncestors) {
				return false;
			}
		}

		return allRemoteMethods;
	}

	/**
	 * Checks if the given method is a remote method.
	 * 
	 * @param method
	 *            method object
	 * @return {@code true} if the given method is a remote method;
	 *         {@code false} otherwise.
	 */
	public static boolean isRemoteMethod(Method method) {
		for (Class exceptionClass : method.getExceptionTypes()) {
			if (exceptionClass.equals(RMIException.class)) {
				return true;
			}
		}

		return false;
	}
}
