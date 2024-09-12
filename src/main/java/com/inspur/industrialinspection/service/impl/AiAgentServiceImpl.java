package com.inspur.industrialinspection.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.inspur.industrialinspection.service.AiAgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Ai Agent Service
 * @author kliu
 * @date 2022/5/9 17:26
 */
@Service
@Slf4j
public class AiAgentServiceImpl implements AiAgentService {
    /**
     * 公共调用ai service方法
     * @param httpUrl
     * @param para
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/5/25 8:38
     */
    @Override
    public JSONObject invokeHttp(String httpUrl, String para) {
        long startTime = System.currentTimeMillis();
        long endTime;
        String resultStr= HttpUtil.post(httpUrl, para, 600000);
        endTime = System.currentTimeMillis();
        log.info("调用aiagent service url |"+httpUrl+"|入参|"+para+"|出参|"+resultStr+"|耗时"+(endTime-startTime)+"ms");
        JSONObject resultObject = JSONUtil.parseObj(resultStr);
        int statusCode = resultObject.getInt("status_code");
        if(statusCode != 1){
            throw new RuntimeException(resultObject.getStr("error_message", "aiagent未返回错误"));
        }
        return resultObject;
    }
}
