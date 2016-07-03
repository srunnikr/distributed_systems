package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * Created by Sreejith Unnikrishnan on 4/9/16.
 */
public class SkeletonWorker<T> implements Runnable {

    protected Socket socket;
    protected Skeleton<T> skeleton;

    ObjectInputStream ois = null;
    ObjectOutputStream oos;

    public SkeletonWorker(Socket socket, Skeleton<T> skeleton){
        this.socket = socket;
        this.skeleton = skeleton;
    }

    @Override
    public void run() {
        skeleton.workerStarted();



        try {
            RmiUtils.safePrintln("SkeletonWorker started running");
            ois = new ObjectInputStream(socket.getInputStream());
            RmiMessage message = (RmiMessage) ois.readObject();

            RmiUtils.safePrintln("Passed method: " + message.methodName);
            RmiUtils.safePrintln("Will call: " + skeleton.remote_interface.toString());

            Object result = execute(message);

            sendMessage(new RmiReturnMessage(result));


        } catch (InvocationTargetException e) {
            sendMessage(new RmiReturnMessage(e.getTargetException(), RmiReturnMessage.RETURN_TYPE.EXCEPTION));
        } catch (NoSuchMethodException e){
            sendMessage(new RmiReturnMessage(new RMIException(e), RmiReturnMessage.RETURN_TYPE.EXCEPTION));
        } catch (IllegalAccessException e){
            sendMessage(new RmiReturnMessage(new RMIException(e), RmiReturnMessage.RETURN_TYPE.EXCEPTION));
        }catch (Exception e) {
            RmiUtils.safePrintln("********** exception "+e.toString());
            if (skeleton.isRunning())
                skeleton.service_error(new RMIException(e));
        } finally {
            skeleton.workerStopped();
        }
    }

    public void sendMessage(RmiReturnMessage message) {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(message);
            oos.flush();
            oos.close();

            if (ois != null) {
                ois.close();
            }
        } catch (IOException e) {
            if (skeleton.isRunning())
                skeleton.listen_error(e);
        }
    }

    public RmiMessage receiveMessage(){
        RmiMessage message = null;
        try {
            ObjectInputStream ois = null;
            ois = new ObjectInputStream(socket.getInputStream());
            message = (RmiMessage) ois.readObject();

            RmiUtils.safePrintln("Passed method: " + message.methodName);
            RmiUtils.safePrintln("Will call: " + skeleton.remote_interface.toString());
            ois.close();
        } catch (IOException e) {
            if (skeleton.isRunning())
                skeleton.listen_error(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            if (skeleton.isRunning())
                skeleton.service_error(new RMIException(e));
        }
        return message;
    }

    private Object execute(RmiMessage message) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method targetMethod = skeleton.remote_interface.getMethod(message.methodName, message.types);
        return targetMethod.invoke(skeleton.remote_impl, message.args);
    }

}
