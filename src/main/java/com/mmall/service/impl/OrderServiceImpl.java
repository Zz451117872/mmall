package com.mmall.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayResponse;
import com.alipay.api.internal.util.AlipaySignature;
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
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVO;
import com.mmall.vo.OrderProductVO;
import com.mmall.vo.OrderVO;
import com.mmall.vo.ShippingVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.incrementer.SybaseAnywhereMaxValueIncrementer;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
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

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    /*
    创建订单
     */
    @Override
    public ServerResponse createOrder(Integer userId,Integer shippingId)
    {
        //通过userId得到该用户的购物车数据
        List<Cart> cartList = cartMapper.selectCheckedCartByUserid(userId);
        //生成OrderItem集合数据
        ServerResponse response = getCartOrderItem(userId,cartList); //userId 是不是多余的参数？
        if(!response.isSuccess())
        {
            return response;
        }//通过userid ,cartlist合成得到orderItemList
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        //遍历orderItemList得到订单总价
        BigDecimal payment = getOrderTotalPrice(orderItemList);
        //生成订单
        Order order = this.assembleOrder(userId,shippingId,payment);
        if(order == null)
        {
            return ServerResponse.createByErrorMessage("服务器出错");
        }
        if(CollectionUtils.isEmpty(orderItemList))
        {
            return ServerResponse.createByErrorMessage("还没有购买，查看你妹的订单");
        }
        for(OrderItem orderItem : orderItemList)
        {//遍历orderItemList，给orderItem放入新生成的订单号
            orderItem.setOrderNo(order.getOrderNo());
        }
        //批量插入订单条目
        orderItemMapper.batchInsert(orderItemList);
        //减少库存
        this.reduceProductStock(orderItemList);
        //清空购物车
        this.clearCart(cartList);
        //返回给前端的数据，只需要成功失败即可，为什么要把订单详细返回？
 //       OrderVO orderVO = this.assembleOrderVO(order,orderItemList);
        return ServerResponse.createBySuccess();
    }

    /*
    通过订单号取消订单
     */
    @Override
    public ServerResponse cancel(Integer user,Long orderNo)
    {
        Order order = orderMapper.selectByUseridAndOrderno(user,orderNo);
        if(order == null)
        {
            return ServerResponse.createByErrorMessage("订单不存在 ");
        }
        if(order.getStatus() != Const.OrderStatusEnum.ON_PAY.getCode())
        {
            return ServerResponse.createByErrorMessage("已付款，无法取消");
        }
        Order temp = new Order();
        temp.setId(order.getId());
        temp.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int row = orderMapper.updateByPrimaryKeySelective(temp);
        if(row >0)
        {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    /*
    获得购物车详细数据，准备生成订单
     */

    @Override
    public ServerResponse getCartOrderProduct(Integer userId)
    {
        OrderProductVO orderProductVO = new OrderProductVO();
        List<Cart> cartList = cartMapper.selectCheckedCartByUserid(userId);
        ServerResponse response = this.getCartOrderItem(userId,cartList);
        if(!response.isSuccess())
        {
            return response;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        List<OrderItemVO> orderItemVOList = Lists.newArrayList();

        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList)
        {
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
            orderItemVOList.add(this.assembleOrderItemVO(orderItem));
        }

        orderProductVO.setOrderItemVOList(orderItemVOList);
        orderProductVO.setProductTotalPrice(payment);
        orderProductVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVO);
    }

    /*
    通过订单号获取订单详细
     */
    @Override
    public ServerResponse<OrderVO> detail(Integer userId,Long orderNo)
    {
        Order order = orderMapper.selectByUseridAndOrderno(userId,orderNo);
        if(order != null)
        {
            List<OrderItem> orderItemList = orderItemMapper.getByOrdernoAndUserid(orderNo,userId);
            OrderVO orderVO = this.assembleOrderVO(order,orderItemList);
            return ServerResponse.createBySuccess(orderVO);
        }
        return ServerResponse.createByErrorMessage("未找到订单");
    }

    /*
    获得订单列表
     */
    @Override
    public ServerResponse<PageInfo> getOrderList(Integer userId,int pageNum,int pageSize)
    {
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectByUserid(userId);
        List<OrderVO> orderVOList = this.assembleOrderVOlist(userId,orderList);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVOList);
        return ServerResponse.createBySuccess(pageInfo);
    }


    /*
    后台系统：获得订单列表
     */
    public ServerResponse<PageInfo> managerList(int pageNum,int pageSize)
    {
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAll();
        List<OrderVO> orderVOList = this.assembleOrderVOlist(null,orderList);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVOList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /*
    后台系统：获得订单详细
     */
    @Override
    public ServerResponse<OrderVO> managerDetail(Long orderNo)
    {
        Order order = orderMapper.selectByOrderno(orderNo);
        if(order != null)
        {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderno(orderNo);
            OrderVO orderVO = this.assembleOrderVO(order,orderItemList);
            return ServerResponse.createBySuccess(orderVO);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    /*
    后台系统：查询订单
     */
    @Override
    public ServerResponse<PageInfo> managerSearch(Long orderNo,int pageNum,int pageSize)
    {
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderno(orderNo);
        if(order != null)
        {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderno(orderNo);
            OrderVO orderVO = this.assembleOrderVO(order,orderItemList);
            PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
            pageInfo.setList(Lists.newArrayList(orderVO));
            return ServerResponse.createBySuccess(pageInfo);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }


    /*
    后台系统：发货
     */
    @Override
    public ServerResponse<String> managerSendGoods(Long orderNo)
    {
        Order order = orderMapper.selectByOrderno(orderNo);
        if(order != null)
        {
            if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode())
            {
                order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return ServerResponse.createBySuccessMessage("success");
            }
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }







    /*
    将订单集合 转化为 orderVO集合
     */
    private List<OrderVO> assembleOrderVOlist(Integer userId,List<Order> orderList)
    {
        List<OrderVO> orderVOList = Lists.newArrayList();

        for(Order order : orderList)
        {
            List<OrderItem> orderItemList = null;
            if(userId == null)
            {//这里是管理员
                orderItemList = orderItemMapper.getByOrdernoAndUserid(order.getOrderNo(),null);
                //// TODO: 2017/6/24
            }else {
                orderItemList = orderItemMapper.getByOrdernoAndUserid(order.getOrderNo(),userId);
            }
            OrderVO orderVO = this.assembleOrderVO(order,orderItemList);
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }

















    /*
    通过order 与 orderItem 合成 orderVO ，因为前台需要orderVO这样的合成数据。
     */
    private OrderVO assembleOrderVO(Order order, List<OrderItem> orderItemList)
    {
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderNo(order.getOrderNo());
        orderVO.setPayment(order.getPayment());
        orderVO.setPayType(order.getPaymentType());
        orderVO.setPayTypeDesc(Const.PaymentTypeEnum.codeof(order.getPaymentType()).getValue());
        orderVO.setPostage(order.getPostage());
        orderVO.setStatus(order.getStatus());
        orderVO.setStatusDesc(Const.OrderStatusEnum.codeof(order.getStatus()).getValue());
        orderVO.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping != null)
        {
            orderVO.setReceiveName(shipping.getReceiverName());
            orderVO.setShippingVO(this.assembleShippingVO(shipping));
        }
        orderVO.setPayTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVO.setCloseTim(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVO.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVO.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVO.setCreateTim(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        List<OrderItemVO> orderItemVOs = Lists.newArrayList();
        for(OrderItem orderItem : orderItemList)
        {
            OrderItemVO orderItemVO = this.assembleOrderItemVO(orderItem);
            orderItemVOs.add(orderItemVO);
        }
        orderVO.setOrderItemVOList(orderItemVOs);
        return orderVO;
    }


    private OrderItemVO assembleOrderItemVO(OrderItem orderItem)
    {
        OrderItemVO orderItemVO = new OrderItemVO();
        orderItemVO.setOrderNo(orderItem.getOrderNo());
        orderItemVO.setProductId(orderItem.getProductId());
        orderItemVO.setProductName(orderItem.getProductName());
        orderItemVO.setProductImage(orderItem.getProductImage());
        orderItemVO.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVO.setQuantity(orderItem.getQuantity());
        orderItemVO.setTotalPrice(orderItem.getTotalPrice());
        orderItemVO.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVO;
    }

    private ShippingVO assembleShippingVO(Shipping shipping)
    {
        ShippingVO shippingVO = new ShippingVO();
        shippingVO.setReceiverAddress(shipping.getReceiverAddress());
        shippingVO.setReceiverCity(shipping.getReceiverCity());
        shippingVO.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVO.setReceiverMobile(shipping.getReceiverMobile());
        shippingVO.setReceiverName(shipping.getReceiverName());
        shippingVO.setReceiverProvince(shipping.getReceiverProvince());
        shippingVO.setReceiverZip(shipping.getReceiverZip());
        return shippingVO;
    }

    /*
    清除购物车
     */
    private void clearCart(List<Cart> carts)
    {
        for (Cart cart : carts)
        {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    /*
    减少对应产品的库存
     */
    private void reduceProductStock(List<OrderItem> orderItemList)
    {
        for(OrderItem orderItem : orderItemList)
        {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    /*
    生成订单，主要是生成订单号。
     */
    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment )
    {
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setStatus(Const.OrderStatusEnum.ON_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        int resultCount = orderMapper.insert(order);
        if(resultCount >0)
        {
            return order;
        }
        return null;
    }

    /*
    生成订单号
     */
    private long generateOrderNo()
    {
        long currentTime = System.currentTimeMillis();
        return currentTime+currentTime%10;
    }

    /*
    计算订单条目的总价
     */
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList)
    {
        BigDecimal payTotal = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList)
        {
            payTotal = BigDecimalUtil.add(payTotal.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payTotal;
    }

    /*
    将购物车条目 转化为订单条目
     */
    private ServerResponse<List<OrderItem>> getCartOrderItem(Integer userId,List<Cart> cartList)
    {
        List<OrderItem> orderItemList = Lists.newArrayList();
        if(CollectionUtils.isEmpty(cartList))
        {
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        for(Cart cart : cartList)
        {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if(product != null)
            {
                if(Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus())
                {
                    return ServerResponse.createByErrorMessage("产品不是在线售卖状态");
                }
                if(cart.getQuantity() > product.getStock())
                {
                    return ServerResponse.createByErrorMessage("产品库存不够");
                }
                orderItem.setUserId(userId); //这里为什么不直接用cart里面的userid，
                orderItem.setProductId(product.getId());//为什么不直接用cart里面的productid
                orderItem.setProductName(product.getName());
                orderItem.setProductImage(product.getMainImage());
                orderItem.setCurrentUnitPrice(product.getPrice());
                orderItem.setQuantity(cart.getQuantity());
                orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cart.getQuantity()));
                orderItemList.add(orderItem);
            }
        }
        return ServerResponse.createBySuccess(orderItemList);
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
        resultMap.put("orderNo",String.valueOf(order.getOrderNo()));

        //支付宝主逻辑
        //必填区
        String outTradeNo = String.valueOf(order.getOrderNo());
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
        String timeoutExpress = "120m";

        //购买商品明细区
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        List<OrderItem> orderItemList = orderItemMapper.getByOrdernoAndUserid(orderNo,userId);
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
                File targerFile = new File(path,qrFileName);        //这个文件是上传至ftp服务器的二维 码文件
                try {
                    FTPUtil.uploadFile(Lists.<File>newArrayList(targerFile));   //上传
                } catch (IOException e) {
                    logger.error("上传二维码异常",e);
                    e.printStackTrace();
                }
                logger.info("qrPath:" + qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targerFile.getName();
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


    //支付宝回调逻辑
    @Override
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
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    @Override
    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo)
    {
        Order order = orderMapper.selectByUseridAndOrderno(userId,orderNo);
        if(order == null)
        {
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode())
        {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
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
