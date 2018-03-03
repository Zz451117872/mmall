package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by aa on 2017/6/20.
 */
public class TokenCache {
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);
    private static LoadingCache<String,String> localCache = CacheBuilder.newBuilder().
            initialCapacity(1000).
            maximumSize(10000).expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                @Override           //意思是当找不到对应的键时返回什么结果
                public String load(String key) throws Exception {
                    return "null";
                }
            });

    public static void setKey(String key,String value)
    {
        localCache.put(key,value);
    }

    public static String getKey(String key)
    {
        String value = null;
        try{
            value = localCache.get(key);
            if("null".equals(value))
            {
                return null;
            }
            return value;
        }catch (Exception e){
            logger.error("localCache get error",e);
        }
        return null;
    }
}
