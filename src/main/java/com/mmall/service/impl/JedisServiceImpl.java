package com.mmall.service.impl;

import com.mmall.service.IJedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by aa on 2017/7/7.
 */
@Service("iJedisService")
public class JedisServiceImpl implements IJedisService{


    static int expire = 300;
    @Autowired
    private JedisPool jedisPool;

    private Jedis getJedis()
    {
        return jedisPool.getResource();
    }

    @Override
    public void set(String key, String value) throws Exception {
        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            jedis.expire(key,expire);
            jedis.set(key,value);
        }catch (Exception ex){
            throw new Exception("jedis set error");
        }finally {
            jedis.close();
        }
    }

    @Override
    public String get(String key) throws Exception {
        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            return jedis.get(key);
        }catch (Exception ex){
            throw new Exception("jedis set error");
        }finally {
            jedis.close();
        }
    }

    @Override
    public String hget(String key, String field) throws Exception {
        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            return jedis.hget(key,field);
        }catch (Exception ex){
            throw new Exception("jedis set error");
        }finally {
            jedis.close();
        }
    }

    @Override
    public List<String> hmget(String key, String... fields) throws Exception {
        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            return jedis.hmget(key,fields);
        }catch (Exception ex){
            throw new Exception("jedis set error");
        }finally {
            jedis.close();
        }
    }

    @Override
    public void hset(String key, String field, String value) throws Exception {
        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            jedis.expire(key,expire);
            jedis.hset(key,field,value);
        }catch (Exception ex){
            throw new Exception("jedis set error");
        }finally {
            jedis.close();
        }
    }


    @Override
    public void hmset(String key,Map<String,String> values) throws Exception {
        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            jedis.expire(key,expire);
            jedis.hmset(key,values);
        }catch (Exception ex){
            throw new Exception("jedis set error");
        }finally {
            jedis.close();
        }
    }

    @Override
    public String lpop(String key) throws Exception {
        Jedis jedis = null;
        try{
            jedis = this.getJedis();
           return jedis.lpop(key);
        }catch (Exception ex){
            throw new Exception("jedis set error");
        }finally {
            jedis.close();
        }
    }

    @Override
    public void lpush(String key, String... values) throws Exception {
        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            jedis.expire(key,expire);
            jedis.lpush(key,values);
        }catch (Exception ex){
            throw new Exception("jedis set error");
        }finally {
            jedis.close();
        }
    }

    @Override
    public void sadd(String key, String... values) throws Exception {
        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            jedis.expire(key,expire);
            jedis.sadd(key,values);
        }catch (Exception ex){
            throw new Exception("jedis set error");
        }finally {
            jedis.close();
        }
    }

    @Override
    public String spop(String key) throws Exception {
        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            return jedis.spop(key);
        }catch (Exception ex){
            throw new Exception("jedis set error");
        }finally {
            jedis.close();
        }
    }

    @Override
    public void zadd(String key, Double score, String value) throws Exception {
        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            jedis.expire(key,expire);
            jedis.zadd(key,score,value);
        }catch (Exception ex){
            throw new Exception("jedis set error");
        }finally {
            jedis.close();
        }
    }

    @Override
    public Set<String> zrange(String key, Long start, Long end) throws Exception {
        Jedis jedis = null;
        try{
            jedis = this.getJedis();
            return jedis.zrange(key,start,end);
        }catch (Exception ex){
            throw new Exception("jedis set error");
        }finally {
            jedis.close();
        }
    }
}
