package com.alipay.demo.trade;

import com.alipay.demo.trade.model.builder.AlipayHeartbeatSynRequestBuilder;
import com.alipay.demo.trade.model.hb.*;
import com.alipay.demo.trade.service.AlipayMonitorService;
import com.alipay.demo.trade.service.impl.hb.AbsHbRunner;
import com.alipay.demo.trade.service.impl.hb.HbQueue;
import com.alipay.demo.trade.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by liuyangkly on 15/10/23.
 鎵ц璋冨害锛屼富瑕佷换鍔＄敱涓や釜绾跨▼瀹屾垚锛屼氦鏄撶嚎绋嬶紙璋冪敤褰撻潰浠�2.0鎺ュ彛锛夊拰浜ゆ槗淇濋殰绾跨▼锛堣疆璇級锛屽叿浣撻渶瑕佸仛鐨勪簨鎯�
 1.褰撻潰浠樼▼搴忔瘡鎵ц瀹屼竴绗斾氦鏄撳悗灏嗕氦鏄撶粨鏋滀繚瀛樺湪涓存椂闃熷垪
 2.杞绾跨▼璇诲彇涓存椂闃熷垪锛岃幏鍙栧熀纭�閲囬泦淇℃伅鍜屾渶澶�30鏉rade_info淇℃伅锛岃皟鐢ㄦ敮浠樺疂monitor.heartbeat.syn鎺ュ彛
 绀轰緥浠ｇ爜浠呭皝瑁呬簡濡備綍璋冪敤璇ユ帴鍙pi锛岄噰闆嗘暟鎹紝姣斿閲囬泦缃戠粶淇℃伅銆佷氦鏄撹�楁椂銆佸紓甯镐俊鎭瓑锛岄渶瑕佺郴缁熷晢寮�鍙戣�呰嚜琛屽畬鎴愩��
 */
public class DemoHbRunner extends AbsHbRunner {

    public DemoHbRunner(AlipayMonitorService monitorService) {
        super(monitorService);
    }

    @Override
    public String getAppAuthToken() {
        // 瀵逛簬绯荤粺鍟嗭紝濡傛灉鏄负浜嗗晢鎴峰紑鍙戠洃鎺т繚闅滄帴鍙ｏ紝鍒欓渶瑕佷紶姝ゅ�硷紝鍚﹀垯濡傛灉涓虹郴缁熷晢鑷繁鍋氫氦鏄撲繚闅滄帴鍙ｅ紑鍙戯紝鍒欏彲涓嶄紶銆�
        return null;
    }

    @Override
    public AlipayHeartbeatSynRequestBuilder getBuilder() {
        // 绯荤粺鍟嗕娇鐢ㄧ殑浜ゆ槗淇℃伅鏍煎紡锛宩son瀛楃涓茬被鍨嬶紝浠庝氦鏄撻槦鍒椾腑鑾峰彇
        List<SysTradeInfo> sysTradeInfoList = HbQueue.poll();

        // 寮傚父淇℃伅鐨勯噰闆嗭紝绯荤粺鍟嗚嚜琛屽畬鎴�
        List<ExceptionInfo> exceptionInfoList = new ArrayList<ExceptionInfo>();
        //        exceptionInfoList.add(ExceptionInfo.HE_SCANER);
        //        exceptionInfoList.add(ExceptionInfo.HE_PRINTER);
        //        exceptionInfoList.add(ExceptionInfo.HE_OTHER);

        AlipayHeartbeatSynRequestBuilder builder = new AlipayHeartbeatSynRequestBuilder()
            .setProduct(Product.FP).setType(Type.CR).setEquipmentId("cr1000001")
            .setEquipmentStatus(EquipStatus.NORMAL).setTime(Utils.toDate(new Date()))
            .setStoreId("store10001").setMac("0a:00:27:00:00:00").setNetworkType("LAN")
            .setProviderId("2088911212323549") // 璁剧疆绯荤粺鍟唒id
            .setSysTradeInfoList(sysTradeInfoList) // 绯荤粺鍟嗗悓姝rade_info淇℃伅
            .setExceptionInfoList(exceptionInfoList) // 濉啓寮傚父淇℃伅锛屽鏋滄湁鐨勮瘽
        ;
        return builder;
    }
}
