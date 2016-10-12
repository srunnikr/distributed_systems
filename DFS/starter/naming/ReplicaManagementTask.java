package naming;

import common.*;

import java.util.*;
import storage.*;

/**
 * @author Dhruv Sharma (dhsharma@cs.ucsd.edu)
 */

public class ReplicaManagementTask implements Runnable {

	private Path file;
	private TreeNode node;
	private NamingServer namingServer;

	/**
	 * The current storage node on which read or write operation happens.
	 */
	private StorageInfo currentStorageInfo;

	/**
	 * It is True if this task must do a file replication to new storage nodes;
	 * False if it is an invalidation task to delete nodes from all other
	 * storage nodes.
	 */
	private boolean isReplicationTask;

	public ReplicaManagementTask(NamingServer namingServer, Path file, TreeNode node,
			boolean isReplicationTask) {
		this.file = file;
		this.node = node;
		this.namingServer = namingServer;
		this.currentStorageInfo = node.storages.get(0);
		this.isReplicationTask = isReplicationTask;
	}

	public void run() {

		// TODO: perform replication or invalidation based on isReplicationTask;
		// (try to) acquire locks accordingly for replication and invalidation
		// on other servers
		
		// Lock was already requested
		boolean isExclusiveLock = !isReplicationTask;
		DfsLock lock = node.getInternalLock(file, isExclusiveLock);
		
		try {
//			System.err.println("waiting to replicate : " + lock.lockedPath + ":" + isExclusiveLock);
            lock.waitLock();
//            System.err.println("Beginning replication : " + lock.lockedPath + ":" + isExclusiveLock);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
		
		// Do file replication on new storage nodes
		if (isReplicationTask) {
			StorageInfo src = currentStorageInfo;
			StorageInfo dest = null;

			HashSet<StorageInfo> availableStorages = namingServer.availableStorages;
			Iterator<StorageInfo> it = availableStorages.iterator();
			while(it.hasNext()) {
				StorageInfo considerInfo = it.next();
				Command consider = considerInfo.commandStub;
				if (!consider.equals(src.commandStub) && !considerInfo.paths.contains(node)) {
					dest = considerInfo;
					break;
				}
			}
			
			if (dest == null) {
				System.err.println("[ERROR] No available storage servers to replicate file. Need Patience!");
			} else {
				try {
					dest.commandStub.copy(file, src.clientStub);
					dest.addFile(node);
		            node.storages.add(dest);
				} catch(Exception e) {
					System.err.println("[ERROR] Replication on new storage nodes failed!");
					e.printStackTrace();
				}
			}
		// Do invalidate operation on all other storage nodes
		} else {
			StorageInfo src = currentStorageInfo;

            HashSet<StorageInfo> availableStorages = namingServer.availableStorages;
            Iterator<StorageInfo> it = availableStorages.iterator();
            while(it.hasNext()) {
                StorageInfo considerInfo = it.next();
                Command consider = considerInfo.commandStub;
                if (!consider.equals(src.commandStub) && considerInfo.paths.contains(node)) {
					try {	
							consider.delete(file);
							node.storages.remove(considerInfo);
							considerInfo.paths.remove(node);
							
					} catch (Exception e) {
						System.err.println("[ERROR] Failed during invalidation of replicas");
						e.printStackTrace();
					}
				}
            }
            
		}
		
		TreeNode current = namingServer.filesystem;
        current.removeLock(lock.id);
        for (String component: file){
            current = current.getChild(component);
            current.removeLock(lock.id);
        }
	}

}
