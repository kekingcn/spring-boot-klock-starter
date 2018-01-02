package org.springframework.boot.autoconfigure.klock.lock;

import org.springframework.boot.autoconfigure.klock.model.LockInfo;

/**
 * Created by kl on 2017/12/29.
 */
public interface Lock {
    Lock setLockInfo(LockInfo lockInfo);

    boolean acquire();

    void release();
}
