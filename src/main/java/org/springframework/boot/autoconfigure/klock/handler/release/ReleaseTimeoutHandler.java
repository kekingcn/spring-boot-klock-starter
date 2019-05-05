package org.springframework.boot.autoconfigure.klock.handler.release;

import org.springframework.boot.autoconfigure.klock.model.LockInfo;

/**
 * 获取锁超时的处理逻辑接口
 *
 * @author wanglaomo
 * @since 2019/4/15
 **/
public interface ReleaseTimeoutHandler {

    void handle(LockInfo lockInfo);
}
