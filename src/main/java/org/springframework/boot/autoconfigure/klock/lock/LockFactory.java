package org.springframework.boot.autoconfigure.klock.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.boot.autoconfigure.klock.core.LockInfoProvider;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;
import org.springframework.boot.autoconfigure.klock.model.LockType;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kl on 2017/12/29.
 * Content :
 */
public class LockFactory  {
    Logger logger= LoggerFactory.getLogger(getClass());

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private LockInfoProvider lockInfoProvider;

    private static final Map<LockType,Lock> lockMap= new HashMap<>();

    @PostConstruct
    public void init(){
        lockMap.put(LockType.Reentrant,new ReentrantLock(redissonClient));
        lockMap.put(LockType.Fair,new FairLock(redissonClient));
        lockMap.put(LockType.Read,new ReadLock(redissonClient));
        lockMap.put(LockType.Write,new WriteLock(redissonClient));
        logger.info("Klock Initialization Successful");
    }

    public Lock getLock(ProceedingJoinPoint joinPoint, Klock klock){
        LockInfo lockInfo = lockInfoProvider.get(joinPoint,klock);
        return lockMap.get(lockInfo.getType()).setLockInfo(lockInfo);
    }

}
