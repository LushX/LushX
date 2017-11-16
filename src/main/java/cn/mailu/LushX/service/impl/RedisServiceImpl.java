package cn.mailu.LushX.service.impl;

import cn.mailu.LushX.constant.LoginTypeEnum;
import cn.mailu.LushX.entity.Video;
import cn.mailu.LushX.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @Author: NULL
 * @Description: redis服务类
 * @Date: Create in 2017/11/10 21:40
 */

@Service
public class RedisServiceImpl implements RedisService{

    @Autowired
    private RedisTemplate redisTemplate;

    public Object getValueByKey(String key){
        return  redisTemplate.opsForValue().get(key);
    }

    public void saveByKey(String key,Object object){
        redisTemplate.opsForValue().set(key,object);
    }

    public List getListByKey(String key,long start ,long end){
        return redisTemplate.opsForList().range(key,start,end);
    }

    public void saveListByKey(String key,List list){
        redisTemplate.opsForList().leftPushAll(key,list);
    }
}
