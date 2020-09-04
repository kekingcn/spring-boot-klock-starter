package org.springframework.boot.autoconfigure.klock.lock;

/**
 * Created by kl on 2017/12/29.
 */
public class Lock {


    String name = "lock";

    public boolean acquire() {
        return true;
    }

    public boolean release() {
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

