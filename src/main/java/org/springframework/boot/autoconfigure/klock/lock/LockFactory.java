package org.springframework.boot.autoconfigure.klock.lock;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kl on 2017/12/29.
 * Content :
 */
public class LockFactory {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedissonClient redissonClient;

    public Lock getLock(LockInfo... lockInfos) {
        if (lockInfos.length == 1) {
            LockInfo lockInfo = lockInfos[0];
            switch (lockInfo.getType()) {
                case Reentrant:
                    return new ReentrantLock(redissonClient, lockInfo);
                case Fair:
                    return new FairLock(redissonClient, lockInfo);
                case Read:
                    return new ReadLock(redissonClient, lockInfo);
                case Write:
                    return new WriteLock(redissonClient, lockInfo);
                default:
                    return new ReentrantLock(redissonClient, lockInfo);
            }
        } else {
            List<LockInfo> targetLockInfos = new ArrayList<>();
            for (int i = 0; i < lockInfos.length; i++) {
                targetLockInfos.add(lockInfos[i]);
            }
            return new MultiLock(redissonClient, targetLockInfos);
        }
    }

}
