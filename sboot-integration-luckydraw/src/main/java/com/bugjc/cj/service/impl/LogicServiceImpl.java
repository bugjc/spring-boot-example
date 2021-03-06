package com.bugjc.cj.service.impl;

import cn.hutool.core.lang.Singleton;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bugjc.cj.component.DrawRedisResult;
import com.bugjc.cj.service.LogicService;
import com.bugjc.cj.core.util.IdWorker;
import com.bugjc.cj.core.util.LuckyDrawQueueUtil;
import com.bugjc.cj.core.util.MyCache;
import com.bugjc.cj.core.dto.Result;
import com.bugjc.cj.core.dto.ResultGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Auther: qingyang
 * @Date: 2018/6/6 09:24
 * @Description:
 */
@Slf4j
@Service
public class LogicServiceImpl implements LogicService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private DrawRedisResult drawRedisResult;

    @Override
    public Result luckyDraw(String userId) {
        log.info("用户["+userId+"]加入抽奖队列");
        String key = "LuckyDraw:"+userId;
        if (MyCache.userRequestLimit(key)){
            log.info("请求频次限制，用户编号："+userId);
        }

        //构建任务参数
        String queryId = IdWorker.getNextId();
        JSONObject result = new JSONObject(){{
            put("userId",userId);
            put("queryId",queryId);
        }};

        //加入队列
//        LuckyDrawQueueUtil luckyDrawQueueUtil = Singleton.get(LuckyDrawQueueUtil.class);
//        luckyDrawQueueUtil.getLuckyDrawQueue().offer(result);

        //快速返回受理结果
        return ResultGenerator.genSuccessResult(result);
    }

    @Override
    public Result queryLuckDraw(String queryId) {
        log.info("查询抽奖结果...");
        String json = drawRedisResult.getQueryResult(queryId);
        if (json == null){
            return new Result().setCode(1);
        }
        return JSON.parseObject(json,Result.class);
    }
}
