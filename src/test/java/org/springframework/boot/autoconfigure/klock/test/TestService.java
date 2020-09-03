package org.springframework.boot.autoconfigure.klock.test;

import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.boot.autoconfigure.klock.annotation.KlockKey;
import org.springframework.boot.autoconfigure.klock.model.LockTimeoutStrategy;
import org.springframework.stereotype.Service;

/**
 * Created by kl on 2017/12/29.
 */
@Service
public class TestService {

    @Klock(waitTime = 10, leaseTime = 60, keys = {"#param"}, lockTimeoutStrategy = LockTimeoutStrategy.FAIL_FAST)
    public String getValue(String param) throws Exception {
        //  if ("sleep".equals(param)) {//线程休眠或者断点阻塞，达到一直占用锁的测试效果
//        Thread.sleep(1000 );
        //}
        return "success";
    }

    @Klock(keys = {"#userId"})
    public String getValue(String userId, @KlockKey Integer id) throws Exception {
        Thread.sleep(60 * 1000);
        return "success";
    }

    @Klock(keys = {"#user.name", "#user.id"})
    public String getValue(User user) throws Exception {
        Thread.sleep(60 * 1000);
        return "success";
    }

    @Klock
    public String updateValue(@KlockKey User originUser, @KlockKey User targetUser) {
        int randNum = (int) Math.round(Math.random()*10);
        originUser.setId(originUser.getId() - randNum);
        targetUser.setId(targetUser.getId() + randNum);
        System.out.println("thread" + Thread.currentThread().getId() + "-randNum:" + randNum);
        return "success";
    }

}
