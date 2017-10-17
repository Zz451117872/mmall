package com.alipay.demo.trade;

import com.alipay.api.AlipayResponse;
import com.alipay.api.domain.TradeFundBill;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.MonitorHeartbeatSynResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.*;
import com.alipay.demo.trade.model.hb.*;
import com.alipay.demo.trade.model.result.AlipayF2FPayResult;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.model.result.AlipayF2FQueryResult;
import com.alipay.demo.trade.model.result.AlipayF2FRefundResult;
import com.alipay.demo.trade.service.AlipayMonitorService;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayMonitorServiceImpl;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.service.impl.AlipayTradeWithHBServiceImpl;
import com.alipay.demo.trade.utils.Utils;
import com.alipay.demo.trade.utils.ZxingUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Created by liuyangkly on 15/8/9.
 * 绠�鍗昺ain鍑芥暟锛岀敤浜庢祴璇曞綋闈粯api
 * sdk鍜宒emo鐨勬剰瑙佸拰闂鍙嶉璇疯仈绯伙細liuyang.kly@alipay.com
 */
public class Main {
    private static Log                  log = LogFactory.getLog(Main.class);

    // 鏀粯瀹濆綋闈粯2.0鏈嶅姟
    private static AlipayTradeService   tradeService;

    // 鏀粯瀹濆綋闈粯2.0鏈嶅姟锛堥泦鎴愪簡浜ゆ槗淇濋殰鎺ュ彛閫昏緫锛�
    private static AlipayTradeService   tradeWithHBService;

    // 鏀粯瀹濅氦鏄撲繚闅滄帴鍙ｆ湇鍔★紝渚涙祴璇曟帴鍙pi浣跨敤锛岃鍏堥槄璇籸eadme.txt
    private static AlipayMonitorService monitorService;

    static {

        Configs.init("zfbinfo.properties");
        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */

        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
        // 支付宝当面付2.0服务（集成了交易保障接口逻辑）
        tradeWithHBService = new AlipayTradeWithHBServiceImpl.ClientBuilder().build();
        /** 如果需要在程序中覆盖Configs提供的默认参数, 可以使用ClientBuilder类的setXXX方法修改默认参数 否则使用代码中的默认设置 */
        monitorService = new AlipayMonitorServiceImpl.ClientBuilder()
            .setGatewayUrl("http://mcloudmonitor.com/gateway.do").setCharset("GBK")
            .setFormat("json").build();
    }
    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                    response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }

    public static void main(String[] args) {
        Main main = new Main();

        // 系统商商测试交易保障接口api
        //        main.test_monitor_sys();

        // POS厂商测试交易保障接口api
        //        main.test_monitor_pos();

        // 测试交易保障接口调度
        //        main.test_monitor_schedule_logic();

        // 测试当面付2.0支付（使用未集成交易保障接口的当面付2.0服务）
        //        main.test_trade_pay(tradeService);

        // 测试查询当面付2.0交易
        //        main.test_trade_query();

        // 测试当面付2.0退货
        //        main.test_trade_refund();

        // 测试当面付2.0生成支付二维码
        main.test_trade_precreate();
    }

    // 测试系统商交易保障调度
    public void test_monitor_schedule_logic() {
        // 启动交易保障线程
        DemoHbRunner demoRunner = new DemoHbRunner(monitorService);
        demoRunner.setDelay(5);// 设置启动后延迟5秒开始调度，不设置则默认3秒
        demoRunner.setDuration(10);// 设置间隔10秒进行调度，不设置则默认15 * 60秒
        demoRunner.schedule();
        // 启动当面付，此处每隔5秒调用一次支付接口，并且当随机数为0时交易保障线程退出
        while (Math.random() != 0) {
            test_trade_pay(tradeWithHBService);
            Utils.sleep(5 * 1000);
        }


        demoRunner.shutdown();
    }

    public void test_monitor_sys() {
        List<SysTradeInfo> sysTradeInfoList = new ArrayList<SysTradeInfo>();
        sysTradeInfoList.add(SysTradeInfo.newInstance("00000001", 5.2, HbStatus.S));
        sysTradeInfoList.add(SysTradeInfo.newInstance("00000002", 4.4, HbStatus.F));
        sysTradeInfoList.add(SysTradeInfo.newInstance("00000003", 11.3, HbStatus.P));
        sysTradeInfoList.add(SysTradeInfo.newInstance("00000004", 3.2, HbStatus.X));
        sysTradeInfoList.add(SysTradeInfo.newInstance("00000005", 4.1, HbStatus.X));

        List<ExceptionInfo> exceptionInfoList = new ArrayList<ExceptionInfo>();
        exceptionInfoList.add(ExceptionInfo.HE_SCANER);
        //        exceptionInfoList.add(ExceptionInfo.HE_PRINTER);
        //        exceptionInfoList.add(ExceptionInfo.HE_OTHER);

        Map<String, Object> extendInfo = new HashMap<String, Object>();
        //        extendInfo.put("SHOP_ID", "BJ_ZZ_001");
        //        extendInfo.put("TERMINAL_ID", "1234");

        String appAuthToken = "应用授权令牌";//根据真实值填写

        AlipayHeartbeatSynRequestBuilder builder = new AlipayHeartbeatSynRequestBuilder()
            .setAppAuthToken(appAuthToken).setProduct(Product.FP).setType(Type.CR)
            .setEquipmentId("cr1000001").setEquipmentStatus(EquipStatus.NORMAL)
            .setTime(Utils.toDate(new Date())).setStoreId("store10001").setMac("0a:00:27:00:00:00")
            .setNetworkType("LAN").setProviderId("2088911212323549") // 设置系统商pid
            .setSysTradeInfoList(sysTradeInfoList) // 系统商同步trade_info信息
            //                .setExceptionInfoList(exceptionInfoList)  // 填写异常信息，如果有的话
            .setExtendInfo(extendInfo) // 填写扩展信息，如果有的话
        ;

        MonitorHeartbeatSynResponse response = monitorService.heartbeatSyn(builder);
        dumpResponse(response);
    }

    // POS厂商的调用样例，填写了所有pos厂商需要填写的字段
    public void test_monitor_pos() {
        //POS厂商使用的交易信息格式，字符串类型
        List<PosTradeInfo> posTradeInfoList = new ArrayList<PosTradeInfo>();
        posTradeInfoList.add(PosTradeInfo.newInstance(HbStatus.S, "1324", 7));
        posTradeInfoList.add(PosTradeInfo.newInstance(HbStatus.X, "1326", 15));
        posTradeInfoList.add(PosTradeInfo.newInstance(HbStatus.S, "1401", 8));
        posTradeInfoList.add(PosTradeInfo.newInstance(HbStatus.F, "1405", 3));

        // 填写异常信息，如果有的话
        List<ExceptionInfo> exceptionInfoList = new ArrayList<ExceptionInfo>();
        exceptionInfoList.add(ExceptionInfo.HE_PRINTER);

        // 填写扩展参数，如果有的话
        Map<String, Object> extendInfo = new HashMap<String, Object>();
        //        extendInfo.put("SHOP_ID", "BJ_ZZ_001");
        //        extendInfo.put("TERMINAL_ID", "1234");

        AlipayHeartbeatSynRequestBuilder builder = new AlipayHeartbeatSynRequestBuilder()
            .setProduct(Product.FP)
            .setType(Type.SOFT_POS)
            .setEquipmentId("soft100001")
            .setEquipmentStatus(EquipStatus.NORMAL)
            .setTime("2015-09-28 11:14:49")
            .setManufacturerPid("2088000000000009")
            // 填写机具商的支付宝pid
            .setStoreId("store200001").setEquipmentPosition("31.2433190000,121.5090750000")
            .setBbsPosition("2869719733-065|2896507033-091").setNetworkStatus("gggbbbgggnnn")
            .setNetworkType("3G").setBattery("98").setWifiMac("0a:00:27:00:00:00")
            .setWifiName("test_wifi_name").setIp("192.168.1.188")
            .setPosTradeInfoList(posTradeInfoList) // POS厂商同步trade_info信息
            //                .setExceptionInfoList(exceptionInfoList) // 填写异常信息，如果有的话
            .setExtendInfo(extendInfo) // 填写扩展信息，如果有的话
        ;

        MonitorHeartbeatSynResponse response = monitorService.heartbeatSyn(builder);
        dumpResponse(response);
    }

    //测试当面付2.0支付
    public void test_trade_pay(AlipayTradeService service) {
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成
        String outTradeNo = "tradepay" + System.currentTimeMillis()
                            + (long) (Math.random() * 10000000L);

        //  (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店消费”
        String subject = "xxx鍝佺墝xxx闂ㄥ簵褰撻潰浠樻秷璐�";

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = "0.01";

        // (必填) 付款条码，用户支付宝钱包手机app点击“付款”产生的付款条码
        String authCode = "鐢ㄦ埛鑷繁鐨勬敮浠樺疂浠樻鐮�"; // 条码示例，286648048691290423
        // (可选，根据需要决定是否使用) 订单可打折金额，可以配合商家平台配置折扣活动，如果订单部分商品参与打折，可以将部分商品总价填写至此字段，默认全部商品可打折
        // 如果该值未传入,但传入了【订单总金额】,【不可打折金额】 则该值默认为【订单总金额】- 【不可打折金额】
        //        String discountableAmount = "1.00"; //

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0.0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品3件共20.00元"
        String body = "购买商品3件共20.00元";

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        String providerId = "2088100200300400500";
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId(providerId);

        // 支付超时，线下扫码交易定义为5分钟
        String timeoutExpress = "5m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "xxx面包", 1000, 1);
        // 创建好一个商品后添加至商品明细列表
        goodsDetailList.add(goods1);

        // 继续创建并添加第一条商品信息，用户购买的产品为“黑人牙刷”，单价为5.00元，购买了两件
        GoodsDetail goods2 = GoodsDetail.newInstance("goods_id002", "xxx鐗欏埛", 500, 2);
        goodsDetailList.add(goods2);

        String appAuthToken = "应用授权令牌";//根据真实值填写

        // 创建条码支付请求builder，设置请求参数
        AlipayTradePayRequestBuilder builder = new AlipayTradePayRequestBuilder()
            //            .setAppAuthToken(appAuthToken)
            .setOutTradeNo(outTradeNo).setSubject(subject).setAuthCode(authCode)
            .setTotalAmount(totalAmount).setStoreId(storeId)
            .setUndiscountableAmount(undiscountableAmount).setBody(body).setOperatorId(operatorId)
            .setExtendParams(extendParams).setSellerId(sellerId)
            .setGoodsDetailList(goodsDetailList).setTimeoutExpress(timeoutExpress);

        // 调用tradePay方法获取当面付应答
        AlipayF2FPayResult result = service.tradePay(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝支付成功: )");
                break;

            case FAILED:
                log.error("支付宝支付失败!!!");
                break;

            case UNKNOWN:
                log.error("系统异常，订单状态未知!!!");
                break;

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                break;
        }
    }

    //  // 测试当面付2.0查询订单
    public void test_trade_query() {
        // 商户订单号，通过此商户订单号查询当面付的交易状态
        String outTradeNo = "tradepay14817938139942440181";

        // 创建查询请求builder，设置请求参数
        AlipayTradeQueryRequestBuilder builder = new AlipayTradeQueryRequestBuilder()
            .setOutTradeNo(outTradeNo);

        AlipayF2FQueryResult result = tradeService.queryTradeResult(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("查询返回该订单支付成功: )");

                AlipayTradeQueryResponse response = result.getResponse();
                dumpResponse(response);

                log.info(response.getTradeStatus());
                if (Utils.isListNotEmpty(response.getFundBillList())) {
                    for (TradeFundBill bill : response.getFundBillList()) {
                        log.info(bill.getFundChannel() + ":" + bill.getAmount());
                    }
                }
                break;

            case FAILED:
                log.error("查询返回该订单支付失败或被关闭!!!");
                break;

            case UNKNOWN:
                log.error("系统异常，订单支付状态未知!!!");
                break;

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                break;
        }
    }

    // 测试当面付2.0退款
    public void test_trade_refund() {
        // (蹇呭～) 澶栭儴璁㈠崟鍙凤紝闇�瑕侀��娆句氦鏄撶殑鍟嗘埛澶栭儴璁㈠崟鍙�
        String outTradeNo = "tradepay14817938139942440181";

        // (蹇呭～) 閫�娆鹃噾棰濓紝璇ラ噾棰濆繀椤诲皬浜庣瓑浜庤鍗曠殑鏀粯閲戦锛屽崟浣嶄负鍏�
        String refundAmount = "0.01";

        // (鍙�夛紝闇�瑕佹敮鎸侀噸澶嶉��璐ф椂蹇呭～) 鍟嗘埛閫�娆捐姹傚彿锛岀浉鍚屾敮浠樺疂浜ゆ槗鍙蜂笅鐨勪笉鍚岄��娆捐姹傚彿瀵瑰簲鍚屼竴绗斾氦鏄撶殑涓嶅悓閫�娆剧敵璇凤紝
        // 瀵逛簬鐩稿悓鏀粯瀹濅氦鏄撳彿涓嬪绗旂浉鍚屽晢鎴烽��娆捐姹傚彿鐨勯��娆句氦鏄擄紝鏀粯瀹濆彧浼氳繘琛屼竴娆￠��娆�
        String outRequestNo = "";

        // (蹇呭～) 閫�娆惧師鍥狅紝鍙互璇存槑鐢ㄦ埛閫�娆惧師鍥狅紝鏂逛究涓哄晢瀹跺悗鍙版彁渚涚粺璁�
        String refundReason = "正常退款，用户买多了";

        // (蹇呭～) 鍟嗘埛闂ㄥ簵缂栧彿锛岄��娆炬儏鍐典笅鍙互涓哄晢瀹跺悗鍙版彁渚涢��娆炬潈闄愬垽瀹氬拰缁熻绛変綔鐢紝璇﹁鏀粯瀹濇妧鏈敮鎸�
        String storeId = "test_store_id";

        // 鍒涘缓閫�娆捐姹俠uilder锛岃缃姹傚弬鏁�
        AlipayTradeRefundRequestBuilder builder = new AlipayTradeRefundRequestBuilder()
            .setOutTradeNo(outTradeNo).setRefundAmount(refundAmount).setRefundReason(refundReason)
            .setOutRequestNo(outRequestNo).setStoreId(storeId);

        AlipayF2FRefundResult result = tradeService.tradeRefund(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝退款成功: )");
                break;

            case FAILED:
                log.error("支付宝退款失败!!!");
                break;

            case UNKNOWN:
                log.error("系统异常，订单退款状态未知!!!");
                break;

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                break;
        }
    }

    // 娴嬭瘯褰撻潰浠�2.0鐢熸垚鏀粯浜岀淮鐮�
    public void test_trade_precreate() {
        // (蹇呭～) 鍟嗘埛缃戠珯璁㈠崟绯荤粺涓敮涓�璁㈠崟鍙凤紝64涓瓧绗︿互鍐咃紝鍙兘鍖呭惈瀛楁瘝銆佹暟瀛椼�佷笅鍒掔嚎锛�
        // 闇�淇濊瘉鍟嗘埛绯荤粺绔笉鑳介噸澶嶏紝寤鸿閫氳繃鏁版嵁搴搒equence鐢熸垚锛�
        String outTradeNo = "tradeprecreate" + System.currentTimeMillis()
                            + (long) (Math.random() * 10000000L);

        // (蹇呭～) 璁㈠崟鏍囬锛岀矖鐣ユ弿杩扮敤鎴风殑鏀粯鐩殑銆傚鈥渪xx鍝佺墝xxx闂ㄥ簵褰撻潰浠樻壂鐮佹秷璐光��
        String subject = "xxx品牌xxx门店当面付扫码消费";

        // (蹇呭～) 璁㈠崟鎬婚噾棰濓紝鍗曚綅涓哄厓锛屼笉鑳借秴杩�1浜垮厓
        // 濡傛灉鍚屾椂浼犲叆浜嗐�愭墦鎶橀噾棰濄��,銆愪笉鍙墦鎶橀噾棰濄��,銆愯鍗曟�婚噾棰濄�戜笁鑰�,鍒欏繀椤绘弧瓒冲涓嬫潯浠�:銆愯鍗曟�婚噾棰濄��=銆愭墦鎶橀噾棰濄��+銆愪笉鍙墦鎶橀噾棰濄��
        String totalAmount = "0.01";

        // (鍙��) 璁㈠崟涓嶅彲鎵撴姌閲戦锛屽彲浠ラ厤鍚堝晢瀹跺钩鍙伴厤缃姌鎵ｆ椿鍔紝濡傛灉閰掓按涓嶅弬涓庢墦鎶橈紝鍒欏皢瀵瑰簲閲戦濉啓鑷虫瀛楁
        // 濡傛灉璇ュ�兼湭浼犲叆,浣嗕紶鍏ヤ簡銆愯鍗曟�婚噾棰濄��,銆愭墦鎶橀噾棰濄��,鍒欒鍊奸粯璁や负銆愯鍗曟�婚噾棰濄��-銆愭墦鎶橀噾棰濄��
        String undiscountableAmount = "0";

        // 鍗栧鏀粯瀹濊处鍙稩D锛岀敤浜庢敮鎸佷竴涓绾﹁处鍙蜂笅鏀寔鎵撴鍒颁笉鍚岀殑鏀舵璐﹀彿锛�(鎵撴鍒皊ellerId瀵瑰簲鐨勬敮浠樺疂璐﹀彿)
        // 濡傛灉璇ュ瓧娈典负绌猴紝鍒欓粯璁や负涓庢敮浠樺疂绛剧害鐨勫晢鎴风殑PID锛屼篃灏辨槸appid瀵瑰簲鐨凱ID
        String sellerId = "";

        // 璁㈠崟鎻忚堪锛屽彲浠ュ浜ゆ槗鎴栧晢鍝佽繘琛屼竴涓缁嗗湴鎻忚堪锛屾瘮濡傚～鍐�"璐拱鍟嗗搧2浠跺叡15.00鍏�"
        String body = "璐拱鍟嗗搧3浠跺叡20.00鍏�";

        // 鍟嗘埛鎿嶄綔鍛樼紪鍙凤紝娣诲姞姝ゅ弬鏁板彲浠ヤ负鍟嗘埛鎿嶄綔鍛樺仛閿�鍞粺璁�
        String operatorId = "test_operator_id";

        // (蹇呭～) 鍟嗘埛闂ㄥ簵缂栧彿锛岄�氳繃闂ㄥ簵鍙峰拰鍟嗗鍚庡彴鍙互閰嶇疆绮惧噯鍒伴棬搴楃殑鎶樻墸淇℃伅锛岃璇㈡敮浠樺疂鎶�鏈敮鎸�
        String storeId = "test_store_id";

        // 涓氬姟鎵╁睍鍙傛暟锛岀洰鍓嶅彲娣诲姞鐢辨敮浠樺疂鍒嗛厤鐨勭郴缁熷晢缂栧彿(閫氳繃setSysServiceProviderId鏂规硶)锛岃鎯呰鍜ㄨ鏀粯瀹濇妧鏈敮鎸�
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 鏀粯瓒呮椂锛屽畾涔変负120鍒嗛挓
        String timeoutExpress = "120m";

        // 鍟嗗搧鏄庣粏鍒楄〃锛岄渶濉啓璐拱鍟嗗搧璇︾粏淇℃伅锛�
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        // 鍒涘缓涓�涓晢鍝佷俊鎭紝鍙傛暟鍚箟鍒嗗埆涓哄晢鍝乮d锛堜娇鐢ㄥ浗鏍囷級銆佸悕绉般�佸崟浠凤紙鍗曚綅涓哄垎锛夈�佹暟閲忥紝濡傛灉闇�瑕佹坊鍔犲晢鍝佺被鍒紝璇﹁GoodsDetail
        GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "xxx灏忛潰鍖�", 1000, 1);
        // 鍒涘缓濂戒竴涓晢鍝佸悗娣诲姞鑷冲晢鍝佹槑缁嗗垪琛�
        goodsDetailList.add(goods1);

        // 缁х画鍒涘缓骞舵坊鍔犵涓�鏉″晢鍝佷俊鎭紝鐢ㄦ埛璐拱鐨勪骇鍝佷负鈥滈粦浜虹墮鍒封�濓紝鍗曚环涓�5.00鍏冿紝璐拱浜嗕袱浠�
        GoodsDetail goods2 = GoodsDetail.newInstance("goods_id002", "xxx鐗欏埛", 500, 2);
        goodsDetailList.add(goods2);

        // 鍒涘缓鎵爜鏀粯璇锋眰builder锛岃缃姹傚弬鏁�
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
            .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
            .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
            .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
            .setTimeoutExpress(timeoutExpress)
            //                .setNotifyUrl("http://www.test-notify-url.com")//鏀粯瀹濇湇鍔″櫒涓诲姩閫氱煡鍟嗘埛鏈嶅姟鍣ㄩ噷鎸囧畾鐨勯〉闈ttp璺緞,鏍规嵁闇�瑕佽缃�
            .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("鏀粯瀹濋涓嬪崟鎴愬姛: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                // 闇�瑕佷慨鏀逛负杩愯鏈哄櫒涓婄殑璺緞
                String filePath = String.format("/Users/sudo/Desktop/qr-%s.png",
                    response.getOutTradeNo());
                log.info("filePath:" + filePath);
                //                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                break;

            case FAILED:
                log.error("鏀粯瀹濋涓嬪崟澶辫触!!!");
                break;

            case UNKNOWN:
                log.error("绯荤粺寮傚父锛岄涓嬪崟鐘舵�佹湭鐭�!!!");
                break;

            default:
                log.error("涓嶆敮鎸佺殑浜ゆ槗鐘舵�侊紝浜ゆ槗杩斿洖寮傚父!!!");
                break;
        }
    }
}
