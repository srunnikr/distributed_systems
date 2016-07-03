package unit.rmi;

import rmi.RMIException;
import test.*;
import unit.rmi.ClientProcess;
import unit.rmi.ServerProcess;
import unit.rmi.SimpleServer;

import java.net.InetSocketAddress;

/** Sample unit test for the class <code>SampleClassUnderTest</code>.

    <p>
    Unit tests, unlike conformance tests, are located in the same package as the
    classes they are testing. This means they are not restricted to testing only
    the public interfaces of the classes in each package - they can also access
    the package-private classes and methods in their package.

    <p>
    Unit tests are isolated from the code they are testing. They are kept under
    the <code>unit/</code> directory.

    <p>
    Delete this class and <code>SampleClassUnderTest</code> before submitting
    your code.
 */
public class SampleUnitTest extends Test
{
    /** Test notice. */
    public static final String  notice = "running sample unit test";

    /** Performs the sample test.

        @throws TestFailed If the test fails.
     */
    @Override
    protected void perform() throws TestFailed
    {
        InetSocketAddress address = new InetSocketAddress("localhost", 5000);
        new Thread(new ServerProcess(address)).start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ClientProcess client = new ClientProcess();
        try {
            String fullName = client.run(address, "there");
            if (!fullName.equals("Hello there")){
                throw new TestFailed("Got string "+fullName);
            }
        } catch (RMIException e) {
            throw new TestFailed("RMIException on client");
        }

        try {
            client.run2(address);
        } catch (RMIException e) {
            throw new TestFailed("RMIException on client run2");
        }
    }
}
