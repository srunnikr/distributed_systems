package naming;

import common.DfsUtils;
import common.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Sreejith Unnikrishnan on 5/9/16.
 */
public class TreeNode {

    public enum NodeType {FILE, DIRECTORY};

    // Structure
    public NodeType nodeType;
    public String nodeName;
    public TreeNode parent;
    public HashMap<String, TreeNode> children = new HashMap<>();

    // Metadata
    public int numAccesses;
    public int readCounter;
//    public boolean markedForDeletion = false;
    public ArrayList<StorageInfo> storages = new ArrayList<>();

    // Locks
    public LinkedList<DfsLock> currentLocks = new LinkedList<>();
    public LinkedList<DfsLock> pendingLocks = new LinkedList<>();


    public TreeNode(){
        this.nodeType = NodeType.DIRECTORY;
    }

    public TreeNode(TreeNode parent){
        this.parent = parent;
    }

    public TreeNode(TreeNode parent, String current, NodeType type) {
        this.parent = parent;
        nodeName = current;
        nodeType = type;
    }

    public boolean hasChild(String component){
        // TODO: check if contains will compare by value, not by reference
        return children.containsKey(component);
    }
    public TreeNode getChild(String component){
        return children.get(component);
    }

    public void addStorage(StorageInfo storage){
    	storages.add(storage);
//        if (!storages.contains(storage)){
//            storages.add(storage);
//        }
    }

    public TreeNode addChild(TreeNode child) {
        child.parent = this;
        children.put(child.nodeName, child);
        return child;
    }

    public void removeChild(TreeNode node) {
        this.children.remove(node.nodeName);
    }

    public boolean canLockProceed() {
        if (currentLocks.isEmpty()) {
            return true;
        }
        // some locks are in currentLocks
        if (currentLocks.peek().isExclusive) { //
            return false;
        }
        // some read locks in currentLocks
        if (pendingLocks.isEmpty()){
            // some read locks in currentLocks and no locks in pendingLocks
            return true;
        } else {
            // some read locks in currentLocks and write locks in pendingLocks
            return false;
        }
    }

    public void addLock(DfsLock dfsLock) {
        pendingLocks.add(dfsLock);
        checkPendingQueue();
//        System.out.println(nodeName);
//        System.out.print("Pending: ");
//        for(DfsLock lock: pendingLocks) {
//        	System.out.print(lock.lockedPath + ":" + lock.isExclusive + ":" + lock.isInternal + ", ");
//        }
//        System.out.println();
//        
//        System.out.print("Current: ");
//        for(DfsLock lock: pendingLocks) {
//        	System.out.print(lock.lockedPath + ":" + lock.isExclusive + ":" + lock.isInternal + ", ");
//        }
//        System.out.println();
//        System.out.println();
    }

    public void removeLock(String lockId) {
        for (DfsLock dfsLock: currentLocks){
            if (dfsLock.id.equals(lockId)){
                currentLocks.remove(dfsLock);
                //DfsUtils.safePrintln("Removed lock: " + dfsLock.isExclusive+ " " + dfsLock.lockedPath);
                //DfsUtils.safePrintln("Current locks: "+currentLocks.size());
                checkPendingQueue();
                return;
            }
        }
        // TODO: do we need to check locks in pendingLocks? Maybe released lock hasn't been even aquired
    }

    public String getLockIdForRelease(Path path, boolean exclusive) {
        if (currentLocks.size() > 1){
            // currentLocks contain multiple read locks, let's find the one for curent path
            for (DfsLock dfsLock: currentLocks){
                if (dfsLock.lockedPath.equals(path) && dfsLock.isExclusive == exclusive){
                    return dfsLock.id;
                }
            }
            return null;
        } else if (currentLocks.size() > 0 && currentLocks.peek().isExclusive == exclusive &&
                currentLocks.peek().lockedPath.equals(path)) {
            // only one lock, R or W, in the current locks available for release
            return currentLocks.peek().id;
        }
        return null;
    }
    
    public DfsLock getInternalLock(Path path, boolean exclusive) {
    	for (DfsLock dfsLock: currentLocks){
    		if (dfsLock.lockedPath.equals(path) && dfsLock.isExclusive == exclusive && dfsLock.isInternal == true){
                return dfsLock;
            }
        }
    	
    	for (DfsLock dfsLock: pendingLocks){
    		if (dfsLock.lockedPath.equals(path) && dfsLock.isExclusive == exclusive && dfsLock.isInternal == true){
                return dfsLock;
            }
        }
    	
    	return null;
    }


    private void checkPendingQueue() {
        while (true){
            DfsLock dfsLock = pendingLocks.peek();
            if (dfsLock == null){
                break;
            }
            //DfsUtils.safePrintln("Dfs path "+dfsLock.lockedPath+" current path "+getPathToCurrent().toString());
            boolean canMoveR = currentLocks.isEmpty() ? true : !currentLocks.peek().isExclusive;
            if (canMoveR){
                if (dfsLock.lockedPath.equals(getPathToCurrent())){
                    //DfsUtils.safePrintln("First branch : if");
                    // R lock for any node or W lock for current node
                    boolean canMoveRW = currentLocks.isEmpty();
                    if (!dfsLock.isExclusive || canMoveRW){
                        pendingLocks.pollFirst();
                        currentLocks.add(dfsLock);
//                        if(!dfsLock.isExclusive) {
//                        	readCounter++;
//                        }
                        checkNotifySender(dfsLock);
                    } else {
                        break;
                    }
                } else {
                    //DfsUtils.safePrintln("Inside the second branch : else");
                    // need to move lock for different path down to child
                    // TODO: check if the child exist and not deleted
                    DfsLock copyLock = new DfsLock(dfsLock.id, dfsLock.lockedPath, false, dfsLock.isInternal);
                    currentLocks.add(copyLock);
//                    if(!copyLock.isExclusive) {
//                    	readCounter++;
//                    }
                    propagateLock(dfsLock);
                }
            } else {
                break;
            }
        }
    }

    private void propagateLock(DfsLock dfsLock){
        Path currentPath = getPathToCurrent();
        String component = currentPath.getNextComponentOf(dfsLock.lockedPath);
        //DfsUtils.safePrintln("Next component: "+component);
        TreeNode child = getChild(component);
        pendingLocks.pollFirst();
        child.addLock(dfsLock);
    }

    private void checkNotifySender(DfsLock dfsLock) {
        Path pathToCurrent = getPathToCurrent();
        if (dfsLock.lockedPath.equals(pathToCurrent)){
            dfsLock.notifySender();
        }
    }

    public Path getPathToCurrent() {

        StringBuilder path = new StringBuilder();
        TreeNode current = this;
        if (current.parent == null){
            return new Path();
        }
        while (current.parent != null){
            path.insert(0, "/"+current.nodeName);
            current = current.parent;
        }
        return new Path(path.toString());
    }

    public TreeNode getRoot(){
        TreeNode current = this;
        while (current.parent != null){
            current = current.parent;
        }
        return current;
    }

}
