# spring-boot-klock-starter
基于redis的分布式锁spring-boot starter组件，使得项目拥有分布式锁能力变得异常简单，支持spring boot，和spirng mvc等spring相关项目


# 快速开始

> spring boot项目接入


1.添加lock starter组件依赖，~~目前还没上传到公共仓库，需要自己下源码build~~ ，已上传到maven中央仓库
```
<dependency>
    <groupId>cn.keking</groupId>
    <artifactId>spring-boot-klock-starter</artifactId>
    <version>1.4-RELEASE</version>
</dependency>

```

2.application.properties配置redis链接：spring.klock.address=127.0.0.1:6379


3.在需要加分布式锁的方法上，添加注解@Klock，如：
```java
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
```xml
<context:component-scan base-package="org.springframework.boot.autoconfigure.klock.KlockAutoConfiguration"/>
```

# 使用参数说明

> 配置参数说明

```properties
spring.klock.address  : redis链接地址
spring.klock.password : redis密码
spring.klock.database : redis数据索引
spring.klock.waitTime : 获取锁最长阻塞时间（默认：60，单位：秒）
spring.klock.leaseTime: 已获取锁后自动释放时间（默认：60，单位：秒）
spring.klock.cluster-server.node-addresses : redis集群配置 如 127.0.0.1:7000,127.0.0.1:7001，127.0.0.1:7002
spring.klock.address 和 spring.klock.cluster-server.node-addresses 选其一即可
```
> @Klock注解参数说明
```
@Klock可以标注四个参数，作用分别如下

name：lock的name，对应redis的key值。默认为：类名+方法名

lockType：锁的类型，目前支持（可重入锁，公平锁，读写锁）。默认为：公平锁

waitTime：获取锁最长等待时间。默认为：60s。同时也可通过spring.klock.waitTime统一配置

leaseTime：获得锁后，自动释放锁的时间。默认为：60s。同时也可通过spring.klock.leaseTime统一配置

lockTimeoutStrategy: 加锁超时的处理策略，可配置为不做处理、快速失败、阻塞等待的处理策略，默认策略为不做处理

customLockTimeoutStrategy: 自定义加锁超时的处理策略，需指定自定义处理的方法的方法名，并保持入参一致。

releaseTimeoutStrategy: 释放锁时，持有的锁已超时的处理策略，可配置为不做处理、快速失败的处理策略，默认策略为不做处理

customReleaseTimeoutStrategy: 自定义释放锁时，需指定自定义处理的方法的方法名，并保持入参一致。
```
# 锁超时说明
因为基于redis实现分布式锁，如果使用不当，会在以下场景下遇到锁超时的问题：
![锁超时处理逻辑](https://wx1.sinaimg.cn/large/7dfa0a7bly1g24obim6cnj20u80jzgnf.jpg "锁超时处理逻辑.jpg")

加锁超时处理策略(**LockTimeoutStrategy**)：
- **NO_OPERATION** 不做处理，继续执行业务逻辑
- **FAIL_FAST** 快速失败，会抛出KlockTimeoutException
- **KEEP_ACQUIRE** 阻塞等待，一直阻塞，直到获得锁，但在太多的尝试后，会停止获取锁并报错，此时很有可能是发生了死锁。
- **自定义(customLockTimeoutStrategy)** 需指定自定义处理的方法的方法名，并保持入参一致，指定自定义处理方法后，会覆盖上述三种策略，且会拦截业务逻辑的运行。

释放锁时超时处理策略(**ReleaseTimeoutStrategy**)：
- **NO_OPERATION** 不做处理，继续执行业务逻辑
- **FAIL_FAST** 快速失败，会抛出KlockTimeoutException
- **自定义(customReleaseTimeoutStrategy)** 需指定自定义处理的方法的方法名，并保持入参一致，指定自定义处理方法后，会覆盖上述两种策略, 执行自定义处理方法时，业务逻辑已经执行完毕，会在方法返回前和throw异常前执行。

**希望使用者清楚的意识到，如果没有对加锁超时进行有效的设置，那么设置释放锁时超时处理策略是没有意义的。**

*在测试模块中已集成锁超时策略的使用用例*
# 关于测试
工程test模块下，为分布式锁的测试模块。可以快速体验分布式锁的效果。

# 使用登记
如果这个项目解决了你的实际问题，可在[https://gitee.com/kekingcn/spring-boot-klock-starter/issues/IH4NE](http://https://gitee.com/kekingcn/spring-boot-klock-starter/issues/IH4NE)登记下，如果节省了你的研发时间，也愿意支持下的话，可点击下方【捐助】请作者喝杯咖啡，也是非常感谢
