# spring-boot-klock-starter
基于redis的分布式锁spring-boot starter组件，使得项目拥有分布式锁能力变得异常简单，支持spring boot，和spirng mvc等spring相关项目


# 快速开始

> spring boot项目接入


1.添加lock starter组件依赖，目前还没上传到公共仓库，需要自己下源码build
```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-klock-starter</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```

2.application.properties配置redis链接：spring.klock.address=127.0.0.1:6379


3.在需要加分布式锁的方法上，添加注解@Klock，如：
```
@Service
public class TestService {

    @Klock(waitTime = Long.MAX_VALUE)
    public String getValue(String param) throws Exception {
        if ("sleep".equals(param)) {//线程休眠或者断点阻塞，达到一直占用锁的测试效果
            Thread.sleep(1000 * 50);
        }
        return "success";
    }
}

```

4. 支持锁指定的业务key，如同一个方法ID入参相同的加锁，其他的放行。业务key的获取支持Spel，具体使用方式如下
![输入图片说明](https://gitee.com/uploads/images/2018/0125/100452_e5d61dc8_492218.png "屏幕截图.png")



> spring mvc项目接入

其他步骤和spring boot步骤一样，只需要spring-xx.xml配置中添加KlockAutoConfiguration类扫描即可，如：
```
<context:component-scan base-package="org.springframework.boot.autoconfigure.klock.KlockAutoConfiguration"/>
```

# 使用参数说明

> 配置参数说明

```
    spring.klock.address  : redis链接地址
    spring.klock.password ：redis密码
    spring.klock.database ：redis数据索引
    spring.klock.waitTime ：获取锁最长阻塞时间（默认：60，单位：秒）
    spring.klock.leaseTime：已获取锁后自动释放时间（默认：60，单位：秒）
    spring.klock.cluster-server.node-addresses ：redis集群配置 如 127.0.0.1:7000,127.0.0.1:7001，127.0.0.1:7002
    spring.klock.address 和 spring.klock.cluster-server.node-addresses 选其一即可
```
> @Klock注解参数说明
```
@Klock可以标注四个参数，作用分别如下

name：lock的name，对应redis的key值。默认为：类名+方法名

lockType：锁的类型，目前支持（可重入锁，公平锁，读写锁）。默认为：公平锁

waitTime：获取锁最长等待时间。默认为：60s。同时也可通过spring.klock.waitTime统一配置

leaseTime：获得锁后，自动释放锁的时间。默认为：60s。同时也可通过spring.klock.leaseTime统一配置
```

# 关于测试
工程test模块下，为分布式锁的测试模块。可以快速体验分布式锁的效果。

# 使用登记
如果这个项目解决了你的实际问题，可在[https://gitee.com/kekingcn/spring-boot-klock-starter/issues/IH4NE](http://https://gitee.com/kekingcn/spring-boot-klock-starter/issues/IH4NE)登记下，如果节省了你的研发时间，也愿意支持下的话，可点击下方【捐助】请作者喝杯咖啡，也是非常感谢
