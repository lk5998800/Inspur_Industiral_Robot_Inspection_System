package com.inspur.industrialinspection.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.aliyun.dyvmsapi20170525.Client;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsRequest;
import com.aliyun.dyvmsapi20170525.models.SingleCallByTtsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.inspur.industrialinspection.service.PhoneNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 告警发送短信
 * @throws
 * @author kliu
 * @date 2022/9/15 14:13
 */
@Service
@Slf4j
public class PhoneNoticeServiceImpl implements PhoneNoticeService {

    @Value("${sms.accessKeyId}")
    private String accessKeyId;
    @Value("${sms.accessKeySecret}")
    private String accessKeySecret;

    @Override
    public void phoneNotice(String calledNumber, String ttsCode, String ttsParam) throws Exception {
        // 初始化 Client，采用 AK&SK 鉴权访问的方式，此方式可能会存在泄漏风险，建议使用 STS 方式。鉴权访问方式请参考：https://help.aliyun.com/document_detail/378657.html
        // 获取 AK 链接：https://usercenter.console.aliyun.com
        Client client = getClient();
        SingleCallByTtsRequest singleCallByTtsRequest = new SingleCallByTtsRequest()
                .setCalledNumber(calledNumber)
                .setTtsCode(ttsCode)
                .setPlayTimes(1);
        if (!StringUtils.isEmpty(ttsParam)){
            singleCallByTtsRequest.setTtsParam(ttsParam);
        }
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            SingleCallByTtsResponse singleCallByTtsResponse = client.singleCallByTtsWithOptions(singleCallByTtsRequest, runtime);
            log.info("短信发送成功，calledNumber="+calledNumber+",ttsParam="+ttsParam+",ttsCode="+ttsCode+"，接口返回："+singleCallByTtsResponse.getBody().getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 创建client
     * @return com.aliyun.dyvmsapi20170525.Client
     * @author kliu
     * @date 2022/11/15 14:40
     */
    private Client getClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "dyvmsapi.aliyuncs.com";
        return new Client(config);
    }

}
