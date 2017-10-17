package com.mmall.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by aa on 2017/7/7.
 */
public interface IJedisService {
   void set(String key,String value) throws Exception;
    String get(String key) throws Exception;
    String hget(String key,String field) throws Exception;
    List<String> hmget(String key,String... fields) throws Exception;
    void hmset(String key,Map<String,String> values) throws Exception;
    void hset(String key,String field,String value) throws Exception;
    String lpop(String key) throws Exception;
    void lpush(String key,String... value) throws Exception;
    void sadd(String key,String... value) throws Exception;
    String spop(String key) throws Exception;
    void zadd(String key,Double score ,String value)throws Exception;
    Set zrange(String key, Long start, Long end)throws Exception;

}
