package com.mmall.common.listener;

import com.mmall.common.daemon.OrderTimeoutAutoCancleThread;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by aa on 2017/12/26.
 */
public class OrderTimeoutAutoCancleListener implements ServletContextListener {

    @Autowired
    OrderTimeoutAutoCancleThread orderTimeoutAutoCancleThread = null;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        if(orderTimeoutAutoCancleThread == null )
        {
            orderTimeoutAutoCancleThread = new OrderTimeoutAutoCancleThread();
            orderTimeoutAutoCancleThread.setDaemon(true);
            orderTimeoutAutoCancleThread.start();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if(orderTimeoutAutoCancleThread != null )
        {
            orderTimeoutAutoCancleThread.stopThread();
        }
    }
}
