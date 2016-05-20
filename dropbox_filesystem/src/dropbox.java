import Client.ClientStub;
import Utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Sreejith Unnikrishnan on 5/19/16.
 */

/* Client facing part of the system. Takes input from client and pass it onto Client stub
    Similarly, displays results and errors of the operation requested by the user.
*/
public class dropbox {

    private static void printInitialCommandHelper() {
        System.out.println("");
        System.out.println("Commands available");
        System.out.println("1. upload <file location>");
        System.out.println("2. downlaod <filename>");
        System.out.println("");
    }

    private static void printClientConsole() {
        System.out.println("Dropbox > ");
    }

    private static boolean validateCommand(String[] operation) {
        if (operation.length < 2 || operation.length > 2 ) {
            Utils.clientLog("ERROR wrong command arguments");
            printInitialCommandHelper();
            return false;
        }
        if (!operation[0].equals("upload") && !operation[0].equals("download")) {
            Utils.clientLog("ERROR unknown command");
            printInitialCommandHelper();
            return false;
        }
        return true;
    }

    public static void main(String args[]) throws IOException {
        File downloadDir;
        ClientStub clientStub;
        String command;
        BufferedReader console;

        // Check the download directory passed on is valid
        if (args.length < 1) {
            Utils.clientLog("Wrong arguments > usage ./dropbox <download directory>");
            System.exit(1);
        }

        downloadDir = new File(args[1]);
        if (!downloadDir.exists() || !downloadDir.isDirectory()) {
            Utils.clientLog("Given directory path does not exist or is not a directory");
            System.exit(1);
        }

        console = new BufferedReader(new InputStreamReader(System.in));

        clientStub = new ClientStub(downloadDir);
        clientStub.scanDownloadDir();

        printInitialCommandHelper();
        printClientConsole();
        command = console.readLine();

        while(!command.equals("quit")) {
            String[] operation = command.split(" ");
            if (!validateCommand(operation)) {
                continue;
            }
            if (clientStub.command(operation)) {
                Utils.clientLog("Command completed");
            } else {
                Utils.clientLog("Command execution failed");
            }
            printClientConsole();
            command = console.readLine();
        }

        Utils.clientLog("Thank you!");
    }

}
