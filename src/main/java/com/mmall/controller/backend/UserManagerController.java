package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 * Created by aa on 2017/6/20.
 */
@RestController
@RequestMapping("/manager_user/")
public class UserManagerController {

    @Autowired
    private IUserService iUserService;

    // 通过 用户名 或者 用户角色 获取用户集合
    @RequestMapping(value = "get_all_user.do", method= RequestMethod.POST)
    public ServerResponse<PageInfo<UserVO>> getUserByUsernameOrRole(HttpSession session,
                                                                     @RequestParam(value = "username",required = false) String username,
                                                                     @RequestParam(value = "userRole",required = false) Integer userRole,
                                                                     @RequestParam(value = "pageNum",required = false,defaultValue = "1") Integer pageNum,
                                                                     @RequestParam(value = "pageSize",required = false,defaultValue = "10")Integer pageSize)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorMessage("未登录");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN)
        {
            return ServerResponse.createByErrorMessage("不是管理员，无法登录");
        }
        if(username != null || userRole != null) {
            return iUserService.getUserByUsernameOrRole(username, userRole, pageNum, pageSize);
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }

    // 冻结用户
    @RequestMapping(value = "freeze_user.do", method= RequestMethod.POST)
    public ServerResponse freezeUser(HttpSession session,Integer userId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorMessage("未登录");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN)
        {
            return ServerResponse.createByErrorMessage("不是管理员，无法登录");
        }
        return iUserService.freezeUser(userId);
    }

    //重置密码
    @RequestMapping(value = "reset_password.do", method= RequestMethod.POST)
    public ServerResponse resetPassword(HttpSession session,Integer userId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorMessage("未登录");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN)
        {
            return ServerResponse.createByErrorMessage("不是管理员，无法登录");
        }
        return iUserService.resetPassword(userId);
    }

    @RequestMapping(value = "check_role.do", method= RequestMethod.POST)
    public ServerResponse<String> checkRole(HttpSession session)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createBySuccess("未登录");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN)
        {
            return ServerResponse.createBySuccess("faild");
        }
        return ServerResponse.createBySuccess("success");
    }

    @RequestMapping(value = "count_online.do", method= RequestMethod.POST)
    public ServerResponse  countOnlineUser(HttpSession session)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createBySuccess("未登录");
        }
        if(user.getRole() != Const.Role.ROLE_ADMIN)
        {
            return ServerResponse.createBySuccess("faild");
        }
        ServletContext context = session.getServletContext();
        int online = 0;
        Object obj = context.getAttribute("online");
        if(obj != null)
        {
            online = (int)obj;
        }
        return ServerResponse.createBySuccess(online);
    }
}
