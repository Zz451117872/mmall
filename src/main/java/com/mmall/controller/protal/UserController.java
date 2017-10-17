package com.mmall.controller.protal;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IJedisService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by aa on 2017/6/19.
 */
@Controller
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IJedisService iJedisService;
    @ Autowired
    private Gson gson;

    @RequestMapping(value = "is_login.do", method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse isLogin(String username)
    {
        try {
           String user =  iJedisService.hget(username,"user");
            if(user != null)
            {
                return ServerResponse.createBySuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ServerResponse.createByError();
    }

    //前端登录
    @RequestMapping(value = "login.do", method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session,HttpServletResponse resp)
    {
        ServerResponse<User> response = iUserService.login(username,password);
        if(response.isSuccess())
        {
            User user = response.getData();
            session.setAttribute(Const.CURRENT_USER,user);
            try {
                iJedisService.hset(user.getUsername(),"user",gson.toJson(user));
            } catch (Exception e) {
                System.out.println("数据入缓存出错");
                e.printStackTrace();
            }
        }
        return response;
    }

    //登出
  @RequestMapping(value = "logout.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        try {
            iJedisService.hset(user.getUsername(),"user",null);
        } catch (Exception e) {
            System.out.println("数据入缓存出错");
            e.printStackTrace();
        }
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    //前端注册
    @RequestMapping(value = "register.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user)
    {
        return iUserService.register(user);
    }

    //表单验证
    @RequestMapping(value = "check_valid.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type)
    {
        return iUserService.checkValid(str,type);
    }

    //获取用户信息
    @RequestMapping(value = "get_user_info.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null)
        {
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("无法获取用户信息");
    }

    //密码忘记提示问题
    @RequestMapping(value = "forget_get_question.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username)
    {
        return iUserService.selectQuestion(username);
    }

    //密码忘记提示问题验证
    @RequestMapping(value = "forget_check_answer.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer)
    {
        return iUserService.checkAnswer(username,question,answer);
    }

    //密码忘记修改密码
    @RequestMapping(value = "forget_reset_password.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String token)
    {
        return iUserService.forgetResetPassword(username,passwordNew,token);
    }

    //修改密码
    @RequestMapping(value = "reset_password.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorMessage("用户未登陆");
        }

        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    //更新用户信息
    @RequestMapping(value = "update_information.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session,User user)
    {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null)
        {
            return ServerResponse.createByErrorMessage("用户未登陆");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if(response.isSuccess())
        {
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    @RequestMapping(value = "get_information.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpSession session)
    {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"该用记没有登录");
        }
        return iUserService.getInformation(currentUser.getId());
    }
}
