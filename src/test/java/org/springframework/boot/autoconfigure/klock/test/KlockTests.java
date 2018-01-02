package org.springframework.boot.autoconfigure.klock.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = KlockTestApplication.class)
public class KlockTests {

	@Autowired
	TestService testService;

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
	//先后启动jvm1 和 jvm 2两个测试用例，会发现虽然 jvm2没休眠,因为getValue加锁了，
	// 所以只要jvm1拿到锁就基本同时完成
}
