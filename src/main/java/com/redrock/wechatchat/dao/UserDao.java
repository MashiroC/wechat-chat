package com.redrock.wechatchat.dao;

import com.redrock.wechatchat.been.Message;
import com.redrock.wechatchat.been.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface UserDao {

    @Insert("INSERT INTO relation(user_id_1,user_id_2,success) VALUE ((SELECT id FROM user WHERE openid = #{fromUser}),(SELECT id FROM user WHERE openid = #{toUser}),#{success})")
    void addFriend(@Param("fromUser") String fromUser, @Param("toUser") String toUser, @Param("success") Integer success);

    @Update("Update relation SET success = 1 WHERE user_id_1 = (SELECT id FROM user WHERE openid = #{fromUser}) AND user_id_2 = (SELECT id FROM user WHERE openid = #{toUser})")
    void uploadFriendSuccess(@Param("fromUser") String fromUser, @Param("toUser") String toUser);

    @Select("SELECT * FROM user WHERE id IN (SELECT user_id_2 from relation WHERE user_id_1 = (SELECT id FROM user WHERE openid = #{fromUser}) AND success = 1)")
    List<User> getFriend(@Param("fromUser") String fromUser);

    //select user.nickname,message.text from user,message where (message.touser_id = (SELECT id FROM user WHERE openid = 'oCtt60ZbjteIEBiL2FaXweolfbKc') and message.fromuser_id = user.id);


    @Select("SELECT * FROM user WHERE openid = #{openid}")
    User getUserByOpenid(String openid);

    @Select("SELECT * FROM user WHERE nickname = #{nickname}")
    User getUserByNickname(@Param("nickname") String nickname);

    @Insert("INSERT INTO user(nickname,openid,head_url) VALUE(#{nickname},#{openid},#{headUrl})")
    void addUser(User user);

    @Update("UPDATE user SET nickname=#{nickname},head_url=#{headUrl} WHERE openid = #{openid}")
    void updateUser(User user);

    @Select("SELECT * FROM user WHERE id IN (SELECT user_id_1 from relation WHERE user_id_2 = (SELECT id FROM user WHERE openid = #{fromUser}) AND success = 0)")
    List<User> getUnAddFriend(String fromUser);

    @Select("SELECT * FROM user WHERE openid = #{toUser} OR nickname = #{toUser}")
    User getUserByOpenidOrNickname(String toUser);

    @Select("select a.nickname as from_user,b.nickname as to_user,message.text from user a,user b,message where message.fromuser_id=(select id from user where openid = #{openid}) AND message.fromuser_id = a.id AND message.touser_id=b.id union all select a.nickname as from_user,b.nickname as to_user,message.text from user a,user b,message where message.touser_id=(select id from user where openid = #{openid}) AND message.fromuser_id = a.id AND message.touser_id=b.id")
    List<Message> getAllMessage(String openid);

    @Insert("INSERT INTO message(fromuser_id,touser_id,text) VALUE((SELECT id FROM user WHERE nickname = #{fromUser}),(SELECT id FROM user WHERE nickname = #{toUser}),#{text})")
    void saveMessage(Message message);

}
