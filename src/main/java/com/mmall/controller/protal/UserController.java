package com.mmall.controller.protal;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IJedisService;
import com.mmall.service.IUserService;
import com.mmall.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

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


    @RequestMapping(value = "login.do", method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> login(@RequestParam(value = "username",required = true) String username,
                                        @RequestParam(value = "password",required = true) String password, HttpSession session ,
                                        @RequestParam(value = "isManager",required = false,defaultValue = "false") Boolean isManager, HttpServletResponse resp)
    {
        ServerResponse<User> response = iUserService.login(username,password);
        if(response.isSuccess())
        {
            User user = response.getData();
            if(isManager && user.getRole() != Const.Role.ROLE_ADMIN)
            {
                return ServerResponse.createByErrorMessage("不是管理员，无法登录");
            }
            session.setAttribute(Const.CURRENT_USER,user);
            return ServerResponse.createBySuccess(user.getUsername());
        }
        return ServerResponse.createByErrorMessage(response.getMsg());
    }

    //登出
    @RequestMapping(value = "logout.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        session.removeAttribute(Const.CURRENT_USER);
        session.invalidate();
        return ServerResponse.createBySuccess();
    }

    //前端注册
    @RequestMapping(value = "add_or_update_user.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> addUser(@Valid User user, BindingResult bindingResult)
    {
        if(bindingResult.hasErrors())
        {
            return ServerResponse.createByErrorMessage(bindingResult.getFieldError().getDefaultMessage());
        }
        return iUserService.addOrUpdateUser(user);
    }

    //表单验证
    @RequestMapping(value = "check_valid.do" ,method = RequestMethod.POST)
    @ResponseBody
    public Boolean checkValid(HttpSession session,String name,String value)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user != null)
        {
            return iUserService.checkValid(user.getId(),name,value);
        }else {
            return iUserService.checkValid(null, name, value);
        }
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

        return iUserService.updatePassword(passwordOld,passwordNew,user);
    }

    //更新用户信息
    @RequestMapping(value = "update_information.do" ,method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> update_information(HttpSession session,User user)
    {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null)
        {
            return ServerResponse.createByErrorMessage("用户未登陆");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        return iUserService.addOrUpdateUser(user);
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
