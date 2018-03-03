package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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

    //通过 用户名关键字 或者 用户角色 查询用户集合
    public ServerResponse<PageInfo<UserVO>> getUserByUsernameOrRole(String username,Integer userRole,Integer pageNum, Integer pageSize)
    {
        try{
            if(username != null && !"".equals(username))
            {
                username = "%"+username.trim()+"%";
            }else {
                username = null;
            }
            if( userRole != Const.Role.ROLE_ADMIN && userRole != Const.Role.ROLE_CUSTOMER)
                return ServerResponse.createByErrorMessage("参数错误");

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

    //登陆
    public ServerResponse<User> login(String username, String password) {
        try {
            if(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password) ) {
                User db_user = userMapper.selectByCondition("username",username);
                if (db_user == null) {
                    return ServerResponse.createByErrorMessage("用户名或密码错误1");
                }
                String md5password = MD5Util.MD5EncodeUtf8(password);
                User user = userMapper.getByUsernameAndPassword(username, md5password);
                if (user == null) {
                    return ServerResponse.createByErrorMessage("用户名或密码错误2");
                }
                //user.setPassword(StringUtils.EMPTY);
                return ServerResponse.createBySuccess(user);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("login:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    //添加或者修改用户
    public ServerResponse<String> addOrUpdateUser(User user) {
        try {
            if(user != null && StringUtils.isNotEmpty(user.getUsername())) {
                if (user.getId() == null) { //新增用户
                    User db_user = userMapper.selectByCondition("username",user.getUsername());
                    if (db_user != null) {
                        return ServerResponse.createByErrorMessage("用户名已存在");
                    }
                    db_user = userMapper.selectByCondition("email",user.getEmail());
                    if (db_user != null) {
                        return ServerResponse.createByErrorMessage("email已存在");
                    }
                    user.setRole(Const.Role.ROLE_CUSTOMER);
                    user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
                    int resultCount = userMapper.insertSelective(user);
                    if (resultCount > 0) {
                        return ServerResponse.createBySuccessMessage("注册成功");
                    }
                    return ServerResponse.createByErrorMessage("注册失败");
                } else {
                    User db_user = userMapper.selectByPrimaryKey(user.getId());
                    if (db_user != null) {
                        User check_user = userMapper.selectByCondition("username",user.getUsername());
                        if (check_user == null || user.getUsername().equals(db_user.getUsername())) {
                            check_user = userMapper.selectByCondition("email",user.getEmail());
                            if (check_user ==null || user.getEmail().equals(db_user.getEmail())) {
                                user.setRole(null);     //意思是这两个属性不能修改
                                user.setPassword(null);
                                int resultCount = userMapper.updateByPrimaryKeySelective(user);
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

    /*
    检查指定用户的指定属性 是否可用（用户名，邮箱，电话号码）
    userId ： 为空 则表示 用户是未登陆状态
    name ： 哪个属性
    value ： 属性值
     */
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

    //根据用户名查询用户预留的密码提示问题（目前只可以预留一个问题）
    public ServerResponse<String> selectQuestion(String username) {
        try{
            if( ! StringUtils.isNotEmpty( username ) )
            {
                return ServerResponse.createByErrorMessage("参数错误");
            }
            if( checkValid(null,"username",username) )
            {
                return ServerResponse.createByErrorMessage("用户不存在 ");
            }
            String question = userMapper.selectQuestingByUsername(username);
            if(StringUtils.isNotBlank(question))
            {
                return ServerResponse.createBySuccess(question);
            }
            return ServerResponse.createByErrorMessage("没有密码提示问题");
        }catch (Exception e){
            logger.error("selectQuestion" , e );
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    //检查用户提交的问题答案
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
       try{
           if( StringUtils.isNotEmpty( username ) &&  StringUtils.isNotEmpty( question ) &&  StringUtils.isNotEmpty( answer ) )
           {
               int resultCount = userMapper.checkAnswer(username,question,answer);
               if(resultCount >0)
               {        //随机生成一个token 返回给前台，需要在有效期前使用
                   String forgetToken = UUID.randomUUID().toString();
                   TokenCache.setKey("token_"+username,forgetToken);
                   return ServerResponse.createBySuccess(forgetToken);
               }
               return ServerResponse.createByErrorMessage("问题回答错误");
           }
           return ServerResponse.createByErrorMessage("参数错误");
       }catch (Exception e){
           logger.error("checkAnswer " , e );
           return ServerResponse.createByErrorMessage("未知错误");
       }
    }

    //忘记密码后重置密码
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        try{
            if(StringUtils.isNotEmpty( username ) && StringUtils.isNotEmpty( passwordNew ) && StringUtils.isNotEmpty( forgetToken ))
            {
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
                    return ServerResponse.createByErrorMessage("修改密码失败");
                }else{
                    return ServerResponse.createByErrorMessage("无效的token");
                }
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("forgetResetPassword " , e );
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    //修改密码
    public ServerResponse<String> updatePassword(String passwordOld, String passwordNew, User user) {
        try{
            if( StringUtils.isNotEmpty( passwordOld ) && StringUtils.isNotEmpty( passwordNew)){
                if( ! user.getPassword().equals( MD5Util.MD5EncodeUtf8(passwordOld) ))
                {
                    return ServerResponse.createByErrorMessage("密码错误");
                }
                user.setPassword( MD5Util.MD5EncodeUtf8( passwordNew ) );
                int resultCount = userMapper.updateByPrimaryKeySelective(user);
                if(resultCount >0)
                {
                    return ServerResponse.createBySuccessMessage("密码更新成功");
                }
                return ServerResponse.createByErrorMessage("密码更新失败");
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e )
        {
            logger.error(" " , e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    //获取个人信息
    public ServerResponse<User> getInformation(Integer userId) {
        try{
            if( userId != null )
            {
                User user = userMapper.selectByPrimaryKey(userId);
                if(user == null)
                {
                    return ServerResponse.createByErrorMessage("用户不存在");
                }
                user.setPassword(StringUtils.EMPTY);
                return ServerResponse.createBySuccess(user);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("getInformation" , e);
            return ServerResponse.createByErrorMessage("未知错误 ");
        }
    }

    public ServerResponse<String> checkAdminRole(User user) {
        if(user !=null && user.getRole().intValue() == Const.Role.ROLE_ADMIN)
        {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    // 冻结用户 或者 解冻用户
    public ServerResponse freezeUser(Integer userId) {
       try{
          if( userId != null )
          {
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
          return ServerResponse.createByErrorMessage("参数错误");
       }catch (Exception e)
       {
           logger.error("freezeUser" , e);
           return ServerResponse.createByErrorMessage("未知错误");
       }
    }

    //管理员重置密码
    public ServerResponse resetPassword(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user != null)
        {
            user.setPassword(MD5Util.MD5EncodeUtf8( Const.INITIAL_PASSWORD ));
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
