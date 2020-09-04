package org.springframework.boot.autoconfigure.klock.test;

import org.springframework.boot.autoconfigure.klock.annotation.Klock;
import org.springframework.boot.autoconfigure.klock.annotation.KlockKey;
import org.springframework.boot.autoconfigure.klock.model.LockTimeoutStrategy;
import org.springframework.boot.autoconfigure.klock.model.LockType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    static List<User> users = new ArrayList<>();

    static {
        User originUser = new User(1, "xx", 1);
        User targetUser = new User(9, "tt", 9);
        users.add(originUser);
        users.add(targetUser);
    }


    @Klock(leaseTime = 100, waitTime = 1, lockTimeoutStrategy = LockTimeoutStrategy.FAIL_FAST, lockType = LockType.Multi)
    public String updateValue(@KlockKey Integer originUserId, @KlockKey Integer targetUserId) {
        int randNum = (int) Math.round(Math.random() * 10);
        System.out.println("thread" + Thread.currentThread().getId() + "---randNum:" + randNum);
        users.forEach(user -> {
            System.out.println(user.getSalary());
            if (Objects.equals(user.getId(), originUserId)) {
                user.setSalary(user.getSalary() - randNum);
            }
            if (Objects.equals(user.getId(), targetUserId)) {
                user.setSalary(user.getSalary() + randNum);
            }
            System.out.println("thread" + Thread.currentThread().getId() + "---" + user);
        });
        try {
//            Thread.sleep(60000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

}
