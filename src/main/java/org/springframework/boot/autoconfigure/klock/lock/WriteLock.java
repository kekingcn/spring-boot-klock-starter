package org.springframework.boot.autoconfigure.klock.lock;

import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;

import java.util.concurrent.TimeUnit;

/**
 * Created by kl on 2017/12/29.
 */
public class WriteLock implements Lock {

    private RReadWriteLock rLock;

    private LockInfo lockInfo;

    private RedissonClient redissonClient;

    public WriteLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean acquire() {
        try {
            rLock=redissonClient.getReadWriteLock(lockInfo.getName());
            return rLock.writeLock().tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public void release() {
        if(rLock.writeLock().isHeldByCurrentThread()){
            rLock.writeLock().unlockAsync();
        }
    }

    public LockInfo getLockInfo() {
        return lockInfo;
    }

    public Lock setLockInfo(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
        return this;
    }
}
