package rmi;

import java.lang.reflect.Method;

/**
 * Created by stanislavmushits on 11/04/16.
 */
public class RmiUtils {
    private static boolean isDebugEnabled = false;

    public static void safePrintln(String s){
        if (isDebugEnabled){
            synchronized (System.out){
                System.out.println("DEBUG: " + s);
            }
        }
    }

    public static void check_null(Object o) {
        if (o == null){
            throw new NullPointerException();
        }
    }

    public static <T> void check_remote_interface(Class<T> c) throws Error {
        if (!c.isInterface()) {
            throw new Error("Class passed to Skeleton does not represent Remote Interface");
        }
        check_remote_methods(c);
        for (Class<?> i: c.getInterfaces()){
            check_remote_methods(i);
        }

    }

    public static void check_remote_methods(Class<?> i){
        for (Method m: i.getDeclaredMethods()){
            boolean hasRMIException = false;
            for (Class<?> e: m.getExceptionTypes()){
                if (e == RMIException.class){
                    hasRMIException = true;
                    break;
                }
            }
            if (!hasRMIException){
                throw new Error("Class passed to Skeleton does not represent Remote Interface");
            }
        }
    }
}
