package naming;

import common.Path;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Sreejith Unnikrishnan on 5/9/16.
 */
public class DfsLock {
    public String id;
    public Path lockedPath;
    public boolean isExclusive;
    public boolean isInternal;
    public CountDownLatch notification;

    public DfsLock(String id, Path path, boolean exclusive, boolean internal) {
        this.id = id;
        lockedPath = path;
        isExclusive = exclusive;
        isInternal = internal;
        notification = new CountDownLatch(1);
    }

    public DfsLock(DfsLock dfsLock) {
        id = dfsLock.id;
        lockedPath = dfsLock.lockedPath;
        isExclusive = dfsLock.isExclusive;
        isInternal = dfsLock.isInternal;
        notification = new CountDownLatch(1);
    }

    public void notifySender(){
        notification.countDown();
    }

    public void waitLock() throws InterruptedException {
        notification.await();
    }
}
