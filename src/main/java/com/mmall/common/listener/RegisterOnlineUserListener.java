package com.mmall.common.listener;

import com.mmall.common.Const;
import com.mmall.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Created by aa on 2017/12/26.
 */
public class RegisterOnlineUserListener implements HttpSessionListener{

    Logger logger = LoggerFactory.getLogger(RegisterOnlineUserListener.class);

    public RegisterOnlineUserListener()
    {
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
    }

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        System.out.println("---------------------------------------------------");
        HttpSession session = httpSessionEvent.getSession();
        ServletContext context = session.getServletContext();

        Object obj = context.getAttribute("online");
        if(obj == null)
        {
            context.setAttribute("online",1);
        }else{
            Object online = context.getAttribute("online");
            context.setAttribute("online",(Integer)online+1);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();

        ServletContext context = session.getServletContext();
        Object online = context.getAttribute("online");
        context.setAttribute("online",(Integer)online - 1);

    }
}
