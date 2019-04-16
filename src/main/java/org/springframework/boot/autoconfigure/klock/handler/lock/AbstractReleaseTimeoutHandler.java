package org.springframework.boot.autoconfigure.klock.handler.lock;

import org.springframework.boot.autoconfigure.klock.handler.release.ReleaseTimeoutHandler;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;

/**
 * @Author wanglaomo
 * @Date 2019/4/16
 **/
public abstract class AbstractReleaseTimeoutHandler implements ReleaseTimeoutHandler {

    public static class None extends AbstractReleaseTimeoutHandler {

        @Override
        public void handle(LockInfo lockInfo) {

        }
    }
}
