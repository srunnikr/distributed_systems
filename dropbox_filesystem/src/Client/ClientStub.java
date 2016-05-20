package Client;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Sreejith Unnikrishnan on 5/19/16.
 */
public class ClientStub {

    private File downloadDir;
    private HashMap<String, String> localFileBlocks;

    public ClientStub(File downloadDir) {
        this.downloadDir = downloadDir;
        localFileBlocks = new HashMap<>();
    }

    public void scanDownloadDir() {
        /*
        * 1. Validate the path
        * 2. Get all the files in directory
        * 3. Scan and hash the file and update the localFileBlocks
        * */
    }

    public boolean command(String[] operation) {
        String op = operation[0];
        String file = operation[1];

        if (op.equals("upload")) {
            return uploadFile(file);
        } else if (op.equals("download")) {
            return downloadFile(file);
        }

        return false;
    }

    private boolean downloadFile(String file) {
        return false;
    }

    private boolean uploadFile(String file) {
        return false;
    }
}
