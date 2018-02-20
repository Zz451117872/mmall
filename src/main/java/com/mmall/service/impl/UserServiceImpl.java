package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.sqlsource.PageSqlSource;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.MD5Util;
import com.mmall.vo.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Created by aa on 2017/6/19.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService{

    @Autowired
    private UserMapper userMapper;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    //查询所有用户
    public ServerResponse<PageInfo<UserVO>> getUserByUsernameOrRole(String username,Integer userRole,Integer pageNum, Integer pageSize)
    {
        try{
            if(username != null && !"".equals(username))
            {
                username = "%"+username.trim()+"%";
            }else {
                username = null;
            }
            try {
                Const.UserStatusEnum.codeof(userRole);
            }catch (Exception e){
                logger.error("UserStatusEnum:",e);
                return  ServerResponse.createByErrorMessage("参数错误");
            }
            if(username != null || userRole != null) {
                PageHelper.startPage(pageNum, pageSize);
                List<User> users = userMapper.getUserByUsernameOrRole(username, userRole);
                if (users != null && !users.isEmpty()) {
                    PageInfo<UserVO> pageInfo = new PageInfo(users);
                    pageInfo.setList(convertUserVOs(users));
                    return ServerResponse.createBySuccess(pageInfo);
                }
                return ServerResponse.createBySuccess(null);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("getUserByUsernameAndRole:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    private List<UserVO> convertUserVOs(List<User> users) {
        try{
            if(users != null && !users.isEmpty())
            {
                List<UserVO> result = Lists.newArrayList();
                for(User user : users)
                {
                    result.add(convertUserVO(user));
                }
                return result;
            }
            return null;
        }catch (Exception e)
        {
            throw e;
        }
    }

    private UserVO convertUserVO(User user) {
        try{
            if(user != null)
            {
                UserVO result = new UserVO();
                result.setId(user.getId());
                result.setUsername(user.getUsername());
                result.setAnswer(user.getAnswer());
                result.setEmail(user.getEmail());
                result.setQuestion(user.getQuestion());
                result.setRoleName(user.getRole() == 1 ? "管理员":"普通用户");
                result.setUpdateTime(DateTimeUtil.dateToStr(user.getUpdateTime()));
                result.setCreateTime(DateTimeUtil.dateToStr(user.getCreateTime()));
                result.setPhone(user.getPhone());
                result.setStatus(user.getStatus());
                result.setStatusDesc(Const.UserStatusEnum.codeof(user.getStatus()).getValue());
                return result;
            }
            return null;
        }catch (Exception e)
        {
            throw e;
        }
    }

    public ServerResponse<User> login(String username, String password) {
        try {
            if(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password) ) {
                int resultCount = userMapper.checkUsername(username);
                if (resultCount == 0) {
                    return ServerResponse.createByErrorMessage("用户名或密码错误");
                }
                String md5password = MD5Util.MD5EncodeUtf8(password);
                User user = userMapper.selectLogin(username, md5password);
                if (user == null) {
                    return ServerResponse.createByErrorMessage("用户名或密码错误");
                }
                user.setPassword(StringUtils.EMPTY);
                return ServerResponse.createBySuccess(user);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("login:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    public ServerResponse<String> addOrUpdateUser(User user) {
        try {
            if(user != null && StringUtils.isNotEmpty(user.getUsername())) {
                if (user.getId() == null) { //新增用户
                    int resultCount = userMapper.checkUsername(user.getUsername());
                    if (resultCount > 0) {
                        return ServerResponse.createByErrorMessage("用户名已存在");
                    }
                    resultCount = userMapper.checkEmail(user.getEmail());
                    if (resultCount > 0) {
                        return ServerResponse.createByErrorMessage("email已存在");
                    }
                    user.setRole(Const.Role.ROLE_CUSTOMER);
                    user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
                    resultCount = userMapper.insertSelective(user);
                    if (resultCount > 0) {
                        return ServerResponse.createBySuccessMessage("注册成功");
                    }
                    return ServerResponse.createByErrorMessage("注册失败");
                } else {
                    User db_user = userMapper.selectByPrimaryKey(user.getId());
                    if (db_user != null) {
                        int resultCount = userMapper.checkUsername(user.getUsername());
                        if (resultCount < 1 || user.getUsername().equals(db_user.getUsername())) {
                            resultCount = userMapper.checkEmail(user.getEmail());
                            if (resultCount < 1 || user.getEmail().equals(db_user.getEmail())) {
                                user.setRole(null);
                                user.setPassword(null);
                                resultCount = userMapper.updateByPrimaryKeySelective(user);
                                if (resultCount > 0) {
                                    return ServerResponse.createBySuccessMessage("更新成功");
                                }
                                return ServerResponse.createByErrorMessage("更新失败");
                            }
                            return ServerResponse.createByErrorMessage("email已存在");
                        }
                        return ServerResponse.createByErrorMessage("用户名已存在");

                    }
                    return ServerResponse.createByErrorMessage("用户不存在");
                }
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("addOrUpdateUser:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    public Boolean checkValid(Integer userId,String name, String value) {
        try{
            User currentUser = null;
            if(userId != null)
            {
                currentUser = userMapper.selectByPrimaryKey(userId);
            }
            User user = userMapper.selectByCondition(name,value);

            if(currentUser == null)
            {
                if(user == null)
                {
                    return true;
                }
                return false;
            }else{
                if(user == null || user.getId().intValue() == currentUser.getId().intValue())
                {
                    return true;
                }
                return false;
            }
        }catch (Exception e)
        {
            logger.error("checkValid:",e);
            return false;
        }
    }

    public ServerResponse<String> selectQuestion(String username) {
        if(checkValid(null,"username",username))
        {
            return ServerResponse.createByErrorMessage("用户不存在 ");
        }
        String question = userMapper.selectQuestingByUsername(username);
        if(StringUtils.isNotBlank(question))
        {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("没有密码提示问题");
    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if(resultCount >0)
        {
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey("token_"+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题回答错误");
    }

    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if(StringUtils.isBlank(forgetToken))
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        //检查该用户名是否存在
        if(checkValid(null,"username",username))
        {
            return ServerResponse.createByErrorMessage("用户不存在 ");
        }
        //在缓存中通过用户名取得token
        String token = TokenCache.getKey("token_"+username);
        if(StringUtils.isBlank(token))
        {
            return ServerResponse.createByErrorMessage("token已过期");
        }
        //若传入token与缓存中token相等则参数有效
        if(StringUtils.equals(forgetToken,token))
        {
            //这里要对密码进行MD5处理
            String pd = MD5Util.MD5EncodeUtf8(passwordNew);
            int resultCount = userMapper.updatePasswordByUsername(username,pd);
            if(resultCount >0)
            {
                return ServerResponse.createBySuccessMessage("密码修改成功 ");
            }
        }else{
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    public ServerResponse<String> updatePassword(String passwordOld, String passwordNew, User user) {
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount == 0)
        {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        resultCount = userMapper.updateByPrimaryKeySelective(user);
        if(resultCount >0)
        {
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
    return ServerResponse.createByErrorMessage("密码更新失败");
    }

    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null)
        {
            return ServerResponse.createByErrorMessage("找不到该用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    public ServerResponse<String> checkAdminRole(User user) {
        if(user !=null && user.getRole().intValue() == Const.Role.ROLE_ADMIN)
        {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    public ServerResponse freezeUser(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user != null)
        {
            if(user.getStatus() == Const.UserStatusEnum.FREEZE_USER.getCode())
            {
                user.setStatus(Const.UserStatusEnum.NORMAL_USER.getCode());
            }else{
                user.setStatus(Const.UserStatusEnum.FREEZE_USER.getCode());
            }
            int result = userMapper.updateByPrimaryKeySelective(user);
            if(result > 0)
            {
                return ServerResponse.createBySuccess();
            }
            return ServerResponse.createByError();
        }
        return ServerResponse.createByErrorMessage("用户不存在");
    }

    public ServerResponse resetPassword(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user != null)
        {
            user.setPassword(MD5Util.MD5EncodeUtf8("1234"));
            int result = userMapper.updateByPrimaryKeySelective(user);
            if(result > 0)
            {
                return ServerResponse.createBySuccess();
            }
            return ServerResponse.createByError();
        }
        return ServerResponse.createByErrorMessage("用户不存在");
    }
}
