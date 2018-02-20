package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVO;
import com.mmall.vo.OrderVO;
import com.mmall.vo.ShippingVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;


/**
 * Created by aa on 2017/6/24.
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    UserMapper userMapper;

    Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    //创建订单
    @Transactional
    public ServerResponse<Long> createOrder(Integer userId,Integer shippingId,String cartIds)
    {
            if (userId != null && shippingId != null && cartIds != null) {
                List<Integer> cartIdList = Lists.newArrayList();
                String[] cartIdArr = cartIds.split(",");

                for (int i = 0; i < cartIdArr.length; i++) {
                    cartIdList.add(Integer.parseInt(cartIdArr[i]));
                }

                //查询要提交订单的 购物条目
                List<Cart> cartList = cartMapper.getByUserAndCartIds(userId, cartIdList);
                if(cartList == null || cartList.isEmpty())
                {
                    return ServerResponse.createByErrorMessage("没有购买商品");
                }
                List<OrderItem> orderItemList = null;           //将购物条目 生成 订单条目
                ServerResponse<List<OrderItem>> response = generalOrderItemList(userId, cartList);
                if (response.isSuccess()) {
                    orderItemList = response.getData();
                } else {                                          //若生成订单条目不成功，则表示部分条件不满足
                    return ServerResponse.createByErrorMessage(response.getMsg());
                }

                if(orderItemList != null && !orderItemList.isEmpty()){
                    //计算提交购物条目 的总价
                    BigDecimal payment = getOrderTotalPrice(orderItemList);
                    Order order = this.assembleOrder(userId, shippingId, payment);//生成订单
                    if (order == null) {
                        return ServerResponse.createByErrorMessage("订单生成错误");
                    }
                    for (OrderItem orderItem : orderItemList) {                   //给生成的订单条目 设置订单号
                        orderItem.setOrderNo(order.getId());
                    }
                    //批量插入订单条目
                    orderItemMapper.batchInsert(orderItemList);
                    //减少库存
                    this.reduceProductStock(orderItemList);
                    //清空购物车
                    this.clearCart(cartList);

                    return ServerResponse.createBySuccess(order.getId());
                }
                return ServerResponse.createByErrorMessage("app错误");
            }
            return ServerResponse.createByErrorMessage("参数错误");
    }

    //清除购物车
    @Transactional
    private void clearCart(List<Cart> carts)
    {
       for (Cart cart : carts)
       {
           cartMapper.deleteByPrimaryKey(cart.getId());
       }
    }

    //减少对应产品的库存
    @Transactional
    private void reduceProductStock(List<OrderItem> orderItemList)
    {
        if(orderItemList != null && !orderItemList.isEmpty()){
            for (OrderItem orderItem : orderItemList) {
                Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
               if(product != null)
               {
                   product.setStock(product.getStock() - orderItem.getQuantity());
                   productMapper.updateByPrimaryKeySelective(product);
               }
            }
        }
    }

    //生成订单，主要是生成订单号。
    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment )
    {
       try{
           if(userId != null && shippingId != null && payment != null) {
               Order order = new Order();
               Long orderNo = generateOrderNo();
               if (orderNo != null) {
                   order.setId(orderNo);
                   order.setStatus(Const.OrderStatusEnum.ON_PAY.getCode());
                   order.setPostage(0);
                   order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
                   order.setUserId(userId);
                   order.setShippingId(shippingId);
                   order.setPayment(payment);
                   int resultCount = orderMapper.insert(order);
                   if (resultCount > 0) {
                       return order;
                   }
                   return null;
               }
               return null;
           }
           return null;
       }catch (Exception e)
       {
           throw e;
       }
    }

    //生成订单号
    private long generateOrderNo()
    {
        long currentTime = System.currentTimeMillis();
        return currentTime+currentTime%10;
    }

    //计算订单条目的总价
    @Transactional
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList)
    {
        if(orderItemList != null && !orderItemList.isEmpty()) {
            BigDecimal payTotal = new BigDecimal("0");
            for (OrderItem orderItem : orderItemList) {
                payTotal = BigDecimalUtil.add(payTotal.doubleValue(), orderItem.getTotalPrice().doubleValue());
            }
            return payTotal;
        }
        return null;
    }

    //将提交的购物条目 生成 订单条目
    private ServerResponse<List<OrderItem>> generalOrderItemList(Integer userId,List<Cart> cartList)
    {
        try {
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (CollectionUtils.isEmpty(cartList)) {
                return ServerResponse.createByErrorMessage("购物车中没有商品");
            }
            for (Cart cart : cartList) {
                OrderItem orderItem = new OrderItem();
                Product product = productMapper.selectByPrimaryKey(cart.getProductId());
                if (product != null) {
                    if (Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()) {
                        return ServerResponse.createByErrorMessage("产品不是在线售卖状态");
                    }
                    if (cart.getQuantity() > product.getStock()) {
                        return ServerResponse.createByErrorMessage("产品库存不够");
                    }
                    orderItem.setUserId(userId);
                    orderItem.setProductId(product.getId());
                    orderItem.setProductName(product.getName());
                    orderItem.setProductImage(product.getMainImage());
                    orderItem.setCurrentUnitPrice(product.getPrice());
                    orderItem.setQuantity(cart.getQuantity());
                    orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cart.getQuantity()));
                    orderItemList.add(orderItem);
                } else {
                    return ServerResponse.createByErrorMessage("所购产品不存在");
                }
            }
            return ServerResponse.createBySuccess(orderItemList);
        }catch (Exception e)
        {
            throw e;
        }
    }

    //通过订单号取消订单
    @Transactional
    public ServerResponse cancel(Boolean isManager,Integer user,Long orderNo)
    {
            Order order = null;
            if(isManager){
                if( orderNo != null) {
                        order = orderMapper.selectByPrimaryKey(orderNo);
                    }else {
                        return ServerResponse.createByErrorMessage("参数错误");
                    }
                }else {
                    if(user != null && orderNo != null) {
                        order = orderMapper.selectByUseridAndOrderno(user, orderNo);
                    }else {
                        return ServerResponse.createByErrorMessage("参数错误");
                    }
                }
                if (order == null) {
                    return ServerResponse.createByErrorMessage("订单不存在 ");
                }
                if (order.getStatus() != Const.OrderStatusEnum.ON_PAY.getCode()) {
                    return ServerResponse.createByErrorMessage("已付款，无法取消");
                }
                order.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
                int row = orderMapper.updateByPrimaryKeySelective(order);
                if (row > 0) {
                    recoverProductStock(order.getId());
                    logger.info("取消订单成功！！！orderNo:"+order.getId());
                    return ServerResponse.createBySuccess();
                }
                return ServerResponse.createByErrorMessage("数据操作错误");
    }

    @Transactional
    private void recoverProductStock(Long orderNo)
    {
        List<OrderItem> orderItemList = orderItemMapper.getByOrderno(orderNo);
        if(orderItemList != null && !orderItemList.isEmpty())
        {
            for(OrderItem orderItem : orderItemList)
            {
                Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
                if(product != null)
                {
                    product.setStock(product.getStock()+orderItem.getQuantity());
                    int result = productMapper.updateByPrimaryKeySelective(product);
                    if(result > 0)
                    {
                        logger.info("成功恢复产品库存："+orderItem.getQuantity()+" 产品名称："+product.getName()+" 所属订单: "+orderNo);
                    }else{
                        logger.info("恢复产品库存失败 产品名称：",product.getName()+" 所属订单: "+orderNo);
                    }
                }else{
                    logger.info("恢复产品库存失败,产品不存在 ID:",orderItem.getProductId());
                }
            }
        }
    }

    public ServerResponse verifyAccepted(Integer id, Long orderNo)
    {
        try{
            if(id != null && orderNo != null)
            {
                Order order = orderMapper.selectByUseridAndOrderno(id,orderNo);
                if(order != null)
                {
                    if(order.getStatus() == Const.OrderStatusEnum.SHIPPED.getCode())
                    {
                        order.setStatus(Const.OrderStatusEnum.SUCCESS.getCode());
                        order.setEndTime(new Date());
                        int result = orderMapper.updateByPrimaryKeySelective(order);
                        if(result > 0)
                        {
                            return ServerResponse.createBySuccess();
                        }
                        return ServerResponse.createByErrorMessage("内部错误");
                    }
                    return ServerResponse.createByErrorMessage("订单状态错误");
                }
                return ServerResponse.createByErrorMessage("订单不存在");
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    //通过订单号获取订单详细
    public ServerResponse<OrderVO> getOrderDetail(Integer userId,Long orderNo)
    {
        try {
            if(orderNo != null) {
                Order order = null;
                if (userId != null) {                           //用户查询订单
                    order = orderMapper.selectByUseridAndOrderno(userId, orderNo);
                } else {                      //管理员查询订单
                    order = orderMapper.selectByOrderno(orderNo);
                }
                if (order != null) {
                    List<OrderItem> orderItemList = orderItemMapper.getByOrderOrUser(orderNo, userId);
                    OrderVO orderVO = convertOrderVO(order, orderItemList);
                    return ServerResponse.createBySuccess(orderVO);
                }
                return ServerResponse.createByErrorMessage("未找到订单");
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("getOrderDetail:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    //获得 我的订单列表
    public ServerResponse<PageInfo> myOrderList(Integer userId,Integer status,Integer pageNum,Integer pageSize)
    {
        try {
            if(userId != null  && pageNum != null && pageSize != null) {
                PageHelper.startPage(pageNum, pageSize);
                List<Order> orderList = orderMapper.selectByUseridOrStatus(userId, status);
                if(orderList != null && !orderList.isEmpty()) {
                    List<OrderVO> orderVOList = convertOrderVOs(userId, orderList);
                    PageInfo pageInfo = new PageInfo(orderList);
                    pageInfo.setList(orderVOList);
                    return ServerResponse.createBySuccess(pageInfo);
                }
                return ServerResponse.createBySuccess(null);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("myOrderList:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    public ServerResponse<PageInfo> getOrderByMultiCondition(Integer userId, String username, Integer orderStatus, Integer minutes, Integer pageNum, Integer pageSize,String orderby,Boolean convert)
    {
        try{
            if(userId == null)
            {
                if(username != null && !"".equals(username))
                {
                    User user = userMapper.getByUsername(username);
                    if(user != null)
                    {
                        userId = user.getId();
                    }
                }
            }
            String createTime = null;
           if(minutes != null && minutes != 0)
           {
               Date date = new Date();
               createTime = DateTimeUtil.dateToStr(new Date(date.getTime()-minutes*60*1000));
           }

            if(userId == null && orderStatus == null && minutes == null)
            {
                return ServerResponse.createByErrorMessage("参数错误");
            }
            PageHelper.startPage(pageNum,pageSize);
            if(StringUtils.isNotEmpty(orderby) && Const.MmallOrderbySet.orderbySet.contains(orderby))
            {
                String[] orderbyArr = orderby.split(":");
                if(orderbyArr.length == 2)
                {
                    PageHelper.orderBy(orderbyArr[0]+" "+orderbyArr[1]);
                }
            }

            List<Order> orderList = orderMapper.getOrderByMultiCondition(userId,orderStatus,createTime);

                if (orderList != null && !orderList.isEmpty()) {
                    PageInfo pageInfo = new PageInfo(orderList);
                    if(convert) {
                        pageInfo.setList(convertOrderVOs(null, orderList));
                    }else{
                        pageInfo.setList(orderList);
                    }
                    return ServerResponse.createBySuccess(pageInfo);
                }

            return ServerResponse.createBySuccess(null);
        }catch (Exception e)
        {
            logger.error("getOrderByMultiCondition:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    @Override
    public ServerResponse sendGoods(Long orderNo)
    {
        try {
            if(orderNo != null) {
                Order order = orderMapper.selectByOrderno(orderNo);
                if (order != null) {
                    if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()) {
                        order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                        order.setSendTime(new Date());
                        int result = orderMapper.updateByPrimaryKeySelective(order);
                        if(result > 0) {
                            return ServerResponse.createBySuccess();
                        }
                        return ServerResponse.createByErrorMessage("数据操作错误");
                    }
                    return ServerResponse.createByErrorMessage("订单状态错误");
                }
                return ServerResponse.createByErrorMessage("订单不存在");
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("sendGoods:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    private List<OrderVO> convertOrderVOs(Integer userId,List<Order> orderList)
    {
        try {
            List<OrderVO> orderVOList = Lists.newArrayList();
            for (Order order : orderList) {
                List<OrderItem> orderItemList = null;
                if (userId == null) {//这里是管理员
                    orderItemList = orderItemMapper.getByOrderOrUser(order.getId(), null);
                } else {
                    orderItemList = orderItemMapper.getByOrderOrUser(order.getId(), userId);
                }
                OrderVO orderVO = convertOrderVO(order, orderItemList);
                orderVOList.add(orderVO);
            }
            return orderVOList;
        }catch (Exception e)
        {
            throw e;
        }
    }

    private OrderVO convertOrderVO(Order order, List<OrderItem> orderItemList)
    {
        try {
            OrderVO orderVO = new OrderVO();
            orderVO.setUserId(order.getUserId());
            User user = userMapper.selectByPrimaryKey(order.getUserId());
            if(user != null)
            {
                orderVO.setUsername(user.getUsername());
            }else{
                return null;
            }
            orderVO.setId(order.getId());
            orderVO.setPayment(order.getPayment().doubleValue());
            orderVO.setPayType(order.getPaymentType());
            orderVO.setPayTypeDesc(Const.PaymentTypeEnum.codeof(order.getPaymentType()).getValue());
            orderVO.setPostage(order.getPostage());
            orderVO.setStatus(order.getStatus());
            orderVO.setStatusDesc(Const.OrderStatusEnum.codeof(order.getStatus()).getValue());
            Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
            if (shipping != null) {
                orderVO.setShippingVO(convertShippingVO(shipping));
            }
            String payTime = DateTimeUtil.dateToStr(order.getPaymentTime());
            payTime = payTime == null || payTime == "" ? "未支付":payTime;
            orderVO.setPayTime(payTime);
            String closeTime = DateTimeUtil.dateToStr(order.getCloseTime());
            closeTime = closeTime == null || closeTime == "" ? "未关闭":closeTime;
            orderVO.setCloseTim(closeTime);
            String endTime = DateTimeUtil.dateToStr(order.getEndTime());
            endTime = endTime == null || endTime == "" ? "未收货":endTime;
            orderVO.setEndTime(endTime);
            String sendTime = DateTimeUtil.dateToStr(order.getSendTime());
            sendTime = sendTime == null || sendTime == "" ? "未发货":sendTime;
            orderVO.setSendTime(sendTime);
            orderVO.setCreateTim(DateTimeUtil.dateToStr(order.getCreateTime()));
            List<OrderItemVO> orderItemVOs = Lists.newArrayList();
            for (OrderItem orderItem : orderItemList) {
                OrderItemVO orderItemVO = convertOrderItemVO(orderItem);
                orderItemVOs.add(orderItemVO);
            }
            orderVO.setOrderItemVOList(orderItemVOs);
            return orderVO;
        }catch (Exception e)
        {
            throw e;
        }
    }

    private OrderItemVO convertOrderItemVO(OrderItem orderItem)
    {
        try {
            OrderItemVO orderItemVO = new OrderItemVO();
            orderItemVO.setId(orderItem.getId());
            orderItemVO.setOrderNo(orderItem.getOrderNo());
            orderItemVO.setProductId(orderItem.getProductId());
            orderItemVO.setProductName(orderItem.getProductName());
            orderItemVO.setProductImage(orderItem.getProductImage());
            orderItemVO.setCurrentUnitPrice(orderItem.getCurrentUnitPrice().doubleValue());
            orderItemVO.setQuantity(orderItem.getQuantity());
            orderItemVO.setTotalPrice(orderItem.getTotalPrice().doubleValue());
            orderItemVO.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
            return orderItemVO;
        }catch (Exception e)
        {
            throw e;
        }
    }

    private ShippingVO convertShippingVO(Shipping shipping)
    {
        try {
            ShippingVO shippingVO = new ShippingVO();

            shippingVO.setReceiverAddress(shipping.getReceiverAddress());
            shippingVO.setReceiverCity(shipping.getReceiverCity());
            shippingVO.setReceiverDistrict(shipping.getReceiverDistrict());
            shippingVO.setReceiverPhone(shipping.getReceiverPhone());
            shippingVO.setReceiverMobile(shipping.getReceiverMobile());
            shippingVO.setReceiverName(shipping.getReceiverName());
            shippingVO.setReceiverProvince(shipping.getReceiverProvince());
            shippingVO.setReceiverZip(shipping.getReceiverZip());
            return shippingVO;
        }catch (Exception e)
        {
            throw e;
        }
    }

    @Override
    public ServerResponse pay(Long orderNo, Integer userId, String path)
    {
        HashMap<String,String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectByUseridAndOrderno(userId,orderNo);
        if(order == null)
        {
            return ServerResponse.createByErrorMessage("没有该订单");
        }
        if(order.getStatus() != Const.OrderStatusEnum.ON_PAY.getCode())
        {
            return ServerResponse.createByErrorMessage("订单状态错误");
        }
        resultMap.put("orderNo",String.valueOf(order.getId()));

        //支付宝主逻辑
        //必填区
        String outTradeNo = String.valueOf(order.getId());
        String subject = "小杰商城"+outTradeNo;
        String totalAmount = order.getPayment().toString();
        String body = "订单："+outTradeNo+totalAmount+"元";

        //不需要动
        String undiscountableAmount = "0";
        String sellerId = "";
        String operatorId = "test_operator_id";
        String storeId = "test_store_id";
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");
        String timeoutExpress = "10m";

        //购买商品明细区
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        List<OrderItem> orderItemList = orderItemMapper.getByOrderOrUser(orderNo,userId);
        for(OrderItem item : orderItemList)
        {
            GoodsDetail goods1 = GoodsDetail.newInstance(
                        item.getProductId().toString()
                        , item.getProductName()
                        , BigDecimalUtil.mul(item.getCurrentUnitPrice().doubleValue(),new Double(100)).longValue()
                        , item.getQuantity());
            goodsDetailList.add(goods1);
        }
        //扫码支付
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//回调地址
                .setGoodsDetailList(goodsDetailList);

        Configs.init("zfbinfo.properties");
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");
                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                //如果在项目路径下没有存放二维码的目录，则创建。
                File folder = new File(path);
                if(!folder.exists())
                {
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径
                String qrPath = String.format(path+"/qr-%s.png", response.getOutTradeNo()); //二维码文件的全路径名
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());    //二维码的文件名
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);        //大致意思应该是把二维码数据写入文件
//                File targerFile = new File(path,qrFileName);        //这个文件是上传至ftp服务器的二维 码文件
//                try {
//                    FTPUtil.uploadFile(Lists.<File>newArrayList(targerFile));   //上传
//                } catch (IOException e) {
//                    logger.error("上传二维码异常",e);
//                    e.printStackTrace();
//                }
//                logger.info("qrPath:" + qrPath);
//                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targerFile.getName();
                String qrUrl = Const.picturePath+qrFileName;
                resultMap.put("qrUrl",qrUrl);
                return ServerResponse.createBySuccess(resultMap);
            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败");
            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知");
            default:
                logger.error("你妹!!!");
                return ServerResponse.createByErrorMessage("你妹");
        }
    }

    public ServerResponse isPayed(Integer userId, Long orderNo)
    {
        try {
            if(userId != null && orderNo != null) {
                Order order = orderMapper.selectByOrderno(orderNo);
                if (order != null) {
                    if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
                        return ServerResponse.createBySuccess();
                    }
                    return ServerResponse.createByError();
                }
                return ServerResponse.createByErrorMessage("订单不存在");
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("isPayed:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }
    //支付宝回调逻辑
    @Override
    @Transactional
    public ServerResponse aliCallback(Map<String,String> parmas)
    {
        Long orderNo = Long.parseLong(parmas.get("out_trade_no"));
        String tradeNo = parmas.get("trade_no");
        String tradeStatus = parmas.get("trade_status");

        Order order =orderMapper.selectByOrderno(orderNo);
        if(order == null)
        {   // 订单不存在
            return ServerResponse.createByErrorMessage("不是我的订单，免疫");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode())
        {   //订单状态 是已付款，已发货，交易成功，已关闭
            return ServerResponse.createBySuccessMessage("支付宝重复调用");
        }
        if(Const.AlipayCallable.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus))
        {   //如果交易状态是 成功，则更新订单状态
            order.setPaymentTime(DateTimeUtil.strToDate(parmas.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        // 保存交易信息
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getId());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }
}
