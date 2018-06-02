package com.redrock.wechatchat;

import com.redrock.wechatchat.dao.UserDao;
import com.redrock.wechatchat.netty.been.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WechatChatApplicationTests {

	@Autowired
	UserDao userDao;
	@Test
	public void contextLoads() {
//		userDao.addUser("mashiro");
//		userDao.addUser("shiina");
//		userDao.addUser("test");
//		userDao.addUser("emm");
	}
//	@Test
//	public void addFriendTest(){
//		userDao.addFriend("mashiro","shiina",0);
//		userDao.addFriend("mashiro","test",0);
//		userDao.addFriend("mashiro","emm",0);
//		userDao.uploadFriendSuccess("mashiro","shiina");
//		userDao.uploadFriendSuccess("mashiro","test");
//		userDao.addFriend("test","mashiro",1);
//		userDao.addFriend("shiina","mashiro",1);
//	}

//	@Test
//	public void get(){
//		List<User> userList =  userDao.getFriend("mashiro");
//		for (User user:userList) {
//			System.out.println(user.getNickname());
//		}
//	}

}
