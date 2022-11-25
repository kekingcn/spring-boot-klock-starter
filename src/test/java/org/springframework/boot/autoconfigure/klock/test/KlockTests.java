package org.springframework.boot.autoconfigure.klock.test;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.klock.handler.KlockTimeoutException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = KlockTestApplication.class)
public class KlockTests {

	@Autowired
	TestService testService;

	@Autowired
	TimeoutService timeoutService;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	/**
	 * 同一进程内多线程获取锁测试
	 * @throws Exception
	 */
	@Test
	public void multithreadingTest()throws Exception{
		ExecutorService executorService = Executors.newFixedThreadPool(6);
		IntStream.range(0,10).forEach(i-> executorService.submit(() -> {
			try {
				String result = testService.getValue("sleep");
				System.err.println("线程:[" + Thread.currentThread().getName() + "]拿到结果=》" + result + new Date().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
		executorService.awaitTermination(30, TimeUnit.SECONDS);
	}


	/**
	 *线程休眠50秒
	 * @throws Exception
	 */
	@Test
	public void jvm1()throws Exception{
        String result=testService.getValue("sleep");
		Assert.assertEquals(result,"success");
	}

	/**
	 *不休眠
	 * @throws Exception
	 */
	@Test
	public void jvm2()throws Exception{
		String result=testService.getValue("noSleep");
		Assert.assertEquals(result,"success");
	}
	/**
	 *不休眠
	 * @throws Exception
	 */
	@Test
	public void jvm3()throws Exception{
		String result=testService.getValue("noSleep");
		Assert.assertEquals(result,"success");
	}
	//先后启动jvm1 和 jvm 2两个测试用例，会发现虽然 jvm2没休眠,因为getValue加锁了，
	// 所以只要jvm1拿到锁就基本同时完成

	/**
	 * 测试业务key
	 */
	@Test
	public void businessKeyJvm1()throws Exception{
		String result=testService.getValue("user1",null);
		Assert.assertEquals(result,"success");
	}
	/**
	 * 测试业务key
	 */
	@Test
	public void businessKeyJvm2()throws Exception{
		String result=testService.getValue("user1",1);
		Assert.assertEquals(result,"success");
	}
	/**
	 * 测试业务key
	 */
	@Test
	public void businessKeyJvm3()throws Exception{
		String result=testService.getValue("user1",2);
		Assert.assertEquals(result,"success");
	}
	/**
	 * 测试业务key
	 */
	@Test
	public void businessKeyJvm4()throws Exception{
		String result=testService.getValue(new User(3,null));
		Assert.assertEquals(result,"success");
	}

	/**
	 * 测试watchdog无限延长加锁时间
	 */
	@Test
	public void infiniteLeaseTime() {
		timeoutService.foo1();
	}

	/**
	 * 测试加锁超时快速失败
	 */
	@Test
	public void lockTimeoutFailFast() throws InterruptedException {

		ExecutorService executorService = Executors.newFixedThreadPool(10);

		executorService.submit(() -> timeoutService.foo1());

		TimeUnit.MILLISECONDS.sleep(1000);

		exception.expect(KlockTimeoutException.class);
		timeoutService.foo2();

	}

	/**
	 * 测试加锁超时阻塞等待
	 * 会打印10次acquire lock
	 */
	@Test
	public void lockTimeoutKeepAcquire() throws InterruptedException {

		ExecutorService executorService = Executors.newFixedThreadPool(10);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(10);

		for(int i=0; i<10; i++) {
			executorService.submit(() -> {
				try {
					startLatch.await();
					timeoutService.foo3();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					endLatch.countDown();
				}
			});
		}

		long start = System.currentTimeMillis();
		startLatch.countDown();
		endLatch.await();
		long end = System.currentTimeMillis();
		Assert.assertTrue((end - start) >= 10*2*1000);
	}

	/**
	 * 测试自定义加锁超时处理策略
	 * 会执行1次自定义加锁超时处理策略
	 */
	@Test
	public void lockTimeoutCustom() throws InterruptedException {

		ExecutorService executorService = Executors.newFixedThreadPool(10);
		CountDownLatch latch = new CountDownLatch(2);

		executorService.submit(() -> {
			timeoutService.foo1();
			latch.countDown();
		});

		executorService.submit(() -> {
			timeoutService.foo4("foo", "bar");
			latch.countDown();
		});

		latch.await();
	}

	/**
	 * 测试加锁超时不做处理
	 */
	@Test
	public void lockTimeoutNoOperation() throws InterruptedException {

		ExecutorService executorService = Executors.newFixedThreadPool(10);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch endLatch = new CountDownLatch(10);

		for(int i=0; i<10; i++) {
			executorService.submit(() -> {
				try {
					startLatch.await();
					timeoutService.foo5("foo", "bar");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					endLatch.countDown();
				}
			});

		}

		long start = System.currentTimeMillis();
		startLatch.countDown();
		endLatch.await();
		long end = System.currentTimeMillis();
		Assert.assertTrue((end - start) < 10*2*1000);
	}

	/**
	 * 测试释放锁时已超时，不做处理
	 */
	@Test
	public void releaseTimeoutNoOperation() {

		timeoutService.foo6("foo", "bar");
	}

	/**
	 * 测试释放锁时已超时，快速失败
	 */
	@Test
	public void releaseTimeoutFailFast() {

		exception.expect(KlockTimeoutException.class);
		timeoutService.foo7("foo", "bar");
	}
	/**
	 * 测试释放锁时已超时，自定义策略
	 */
	@Test
	public void releaseTimeoutCustom(){
		exception.expect(IllegalStateException.class);
		timeoutService.foo8("foo", "bar");
	}
}
