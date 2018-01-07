package org.springframework.boot.autoconfigure.klock.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;

import java.util.concurrent.TimeUnit;

/**
 * Created by kl on 2017/12/29.
 */
public class ReentrantLock implements Lock {

    private RLock rLock;

    private LockInfo lockInfo;

    private RedissonClient redissonClient;

    public ReentrantLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    @Override
    public boolean acquire() {
        try {
            rLock=redissonClient.getLock(lockInfo.getName());
            return rLock.tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public void release() {
        if(rLock.isHeldByCurrentThread()){
            rLock.unlockAsync();
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
