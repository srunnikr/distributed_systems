package rmi;

/** RMI exceptions. */
public class RMIException extends Exception
{
	private static final long serialVersionUID = -5373364859058750880L;

		/** Creates an <code>RMIException</code> with the given message string.
		*
		*  @param message
		*						RMI Exception message
		*/
    public RMIException(String message)
    {
        super(message);
    }

    /** Creates an <code>RMIException</code> with a message string and the given
      *  cause.
			* @param message
			*						RMI Exception message
			* @param cause
			*						Cause of exception
			*/
    public RMIException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /** Creates an <code>RMIException</code> from the given cause.
		*
		*  @param cause
		*						Cause if exception
		*
		*/
    public RMIException(Throwable cause)
    {
        super(cause);
    }
}
