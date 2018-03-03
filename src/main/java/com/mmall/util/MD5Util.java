package com.mmall.util;


import java.security.MessageDigest;

/*
 *MD5加密工具
 */
public class MD5Util {

    //字节数组 转化为 16进制字符串
    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
            resultSb.append(byteToHexString(b[i]));

        return resultSb.toString();
    }
    //单个字节 转化为 16进制字符串，一个字节8位，可以用2位16进制表示
    private static String byteToHexString(byte b) {
        int n = b;  //将字节转化为 整形，小于256
        if (n < 0)
            n += 256;
        int d1 = n / 16;    //高位
        int d2 = n % 16;    //低位
        return hexDigits[d1] + hexDigits[d2];
    }

    //使用 特定的字符集 对 输入串 加密
    private static String MD5Encode(String origin, String charsetname) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charsetname == null || "".equals(charsetname))
                resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
            else
                resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetname)));
        } catch (Exception exception) {
        }
        return resultString.toUpperCase();
    }

    //使用utf-8对 输入串加密
    public static String MD5EncodeUtf8(String origin) {
        origin = origin + PropertiesUtil.getProperty("password.salt", "");
        return MD5Encode(origin, "utf-8");
    }


    private static final String hexDigits[] = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

}
