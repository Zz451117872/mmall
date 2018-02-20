package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.vo.UserVO;

import java.util.List;

/**
 * Created by aa on 2017/6/19.
 */
public interface IUserService {

    ServerResponse<User> login(String username, String password);
    Boolean checkValid(Integer userId,String name,String value);
    ServerResponse<String> selectQuestion(String username);
    ServerResponse<String> checkAnswer(String username,String question,String answer);
    ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken);
    ServerResponse<String> updatePassword(String passwordOld,String passwordNew,User user);
    ServerResponse<User> getInformation(Integer userId);
    ServerResponse<String> checkAdminRole(User user);

    ServerResponse<PageInfo<UserVO>> getUserByUsernameOrRole(String username,Integer userRole,Integer pageNum , Integer pageSize);

    ServerResponse<String> addOrUpdateUser(User user);

    ServerResponse freezeUser(Integer userId);

    ServerResponse resetPassword(Integer userId);

}
