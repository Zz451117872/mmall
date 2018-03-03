package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    //通过用户名 查询 密码忘记问题
    String selectQuestingByUsername(String username);

    //通过 用户名 和 用户密码 查询用户
    User getByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    //通过 用户名 和 密码忘记问题 和 密码忘记问题答案 查询计数
    int checkAnswer(@Param("username")String username,@Param("question")String question,@Param("answer")String answer);

    //通过 用户名 更新 新密码
    int updatePasswordByUsername(@Param("username")String username,@Param("passwordNew")String passwordNew);

    //通过用户关键字 或者 用户角色 查询用户集合
    List<User> getUserByUsernameOrRole(@Param("username")String username,@Param("userRole") Integer userRole);

    //通过 字段属性（具有唯一性的字段：username , email , phone ） 查询用户
    User selectByCondition(@Param("name")String name, @Param("value")String value);
}