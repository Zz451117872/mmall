package com.mmall.common.daemon;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.listener.RegisterOnlineUserListener;
import com.mmall.pojo.Order;
import com.mmall.service.IOrderService;
import com.mmall.service.impl.OrderServiceImpl;
import com.mmall.util.DateTimeUtil;
import com.mmall.vo.OrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;

import java.util.Date;
import java.util.List;

/**
 * Created by aa on 2017/12/26.
 * 订单支付超时自动取消
 */
public class OrderTimeoutAutoCancleThread extends Thread {

    public OrderTimeoutAutoCancleThread()
    {
        ApplicationContext act = ContextLoader.getCurrentWebApplicationContext();
        this.iOrderService = (IOrderService)act.getBean("iOrderService");
        logger.info("OrderTimeoutAutoCancleThread 启动成功");
    }

    private IOrderService iOrderService = null;

    private Boolean isRunning = true;

    Logger logger = LoggerFactory.getLogger(OrderTimeoutAutoCancleThread.class);



    @Override
    public void run() {
        while (isRunning)
        {
            logger.info("OrderTimeoutAutoCancleThread is running !!! "+new Date());
            ServerResponse<PageInfo> response =  iOrderService.getOrderByMultiCondition(null,null, Const.OrderStatusEnum.ON_PAY.getCode(),null,1,20,"create_time:asc",false);
            if(response != null && response.getData() != null)
            {
                List<Order> orders = response.getData().getList();
                if(orders != null && !orders.isEmpty())
                {
                    checkTimeoutProcess(orders);
                }

            }else{
                logger.info("------------------没有待支付订单--------------");
                System.out.println("------------------没有待支付订单--------------");
            }
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkTimeoutProcess(List<Order> orders)
    {
        for(Order order : orders)
        {
            if(judgeTimeout(order))
            {
                logger.info("订单支付超时，取消订单："+order.getId());
                iOrderService.cancel(true,null,order.getId());

            }
        }
    }

    private boolean judgeTimeout(Order order)
    {
        Date currentTime = new Date();
        if(DateTimeUtil.dateDiff(currentTime,order.getCreateTime()) >= Const.orderPayTimeoutLimit)
        {
            logger.info("订单支付超时！下单时间："+order.getCreateTime()+" 当前时间："+currentTime);
            return true;
        }
        return false;
    }

    public void stopThread()
    {
        this.setRunning(false);
        logger.info("OrderTimeoutAutoCancleThread 停止");
    }

    public Boolean getRunning() {
        return isRunning;
    }

    public void setRunning(Boolean running) {
        isRunning = running;
    }
}
