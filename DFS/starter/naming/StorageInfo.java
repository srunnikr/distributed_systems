package naming;

import common.Path;
import storage.Command;
import storage.Storage;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sreejith Unnikrishnan on 5/9/16.
 */
public class StorageInfo {
    public Command commandStub;
    public Storage clientStub;
    public ArrayList<TreeNode> paths;

    public StorageInfo(Command command){
        commandStub = command;
        paths = new ArrayList<>();
    }

    public StorageInfo(Storage client, Command command){
        clientStub = client;
        commandStub = command;
        paths = new ArrayList<TreeNode>();
    }
    public void addFile(TreeNode file){
        paths.add(file);
    }

//    public void addPaths(List<Path> paths){
//        throw new NotImplementedException();
//    }
//
//    public void addPath(Path path){
//        throw new NotImplementedException();
//    }
//
//    public void removePath(Path path){
//        throw new NotImplementedException();
//    }
//
//    public void deleteStub(){
//        throw new NotImplementedException();
//    }
}
