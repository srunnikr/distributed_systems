package common;

import common.Path;

/**
 * Created by Sreejith Unnikrishnan on 5/11/16.
 */
public class DfsUtils {
    private static boolean isDebugEnabled = true;

    public static void safePrintln(String s){
        if (isDebugEnabled){
            synchronized (System.out){
                System.out.println("DEBUG: " + s);
            }
        }
    }

    public static void safePrintln(String s, Object[] arr) {
        if (isDebugEnabled){
            synchronized (System.out){
                System.out.println("DEBUG: " + s);
                for (Object obj: arr){
                    System.out.println(obj.toString());
                }
            }
        }
    }
}
