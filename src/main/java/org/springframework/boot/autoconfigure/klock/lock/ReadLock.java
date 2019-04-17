package org.springframework.boot.autoconfigure.klock.lock;

import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by kl on 2017/12/29.
 */
public class ReadLock implements Lock {

    private  RReadWriteLock rLock;

    private final LockInfo lockInfo;

    private RedissonClient redissonClient;

    public ReadLock(RedissonClient redissonClient,LockInfo info) {
        this.redissonClient = redissonClient;
        this.lockInfo = info;
    }

    @Override
    public boolean acquire() {
        try {
            rLock=redissonClient.getReadWriteLock(lockInfo.getName());
            return rLock.readLock().tryLock(lockInfo.getWaitTime(), lockInfo.getLeaseTime(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean release() {
        if(rLock.readLock().isHeldByCurrentThread()){
            try {
                return rLock.readLock().forceUnlockAsync().get();
            } catch (InterruptedException e) {
                return false;
            } catch (ExecutionException e) {
                return false;
            }
        }

        return false;
    }
}
