package org.springframework.boot.autoconfigure.klock.test;

import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.boot.autoconfigure.klock.annotation.KlockKey;
import org.springframework.stereotype.Service;

/**
 * Created by kl on 2017/12/29.
 */
@Service
public class TestService {

    @Klock(waitTime = Long.MAX_VALUE,leaseTime = 1)
    public String getValue(String param) throws Exception {
        if ("sleep".equals(param)) {//线程休眠或者断点阻塞，达到一直占用锁的测试效果
            Thread.sleep(1000*3);
        }
        return "success";
    }

    @Klock(keys = {"#userId"})
    public String getValue(String userId,@KlockKey int id)throws Exception{
        Thread.sleep(60*1000);
        return "success";
    }

    @Klock(keys = {"#user.name","#user.id"})
    public String getValue(User user)throws Exception{
        Thread.sleep(60*1000);
        return "success";
    }

}
