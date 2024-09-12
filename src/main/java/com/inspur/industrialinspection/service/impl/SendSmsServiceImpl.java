package com.inspur.industrialinspection.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.inspur.industrialinspection.service.SendSmsService;
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
public class SendSmsServiceImpl implements SendSmsService {

    @Value("${sms.accessKeyId}")
    private String accessKeyId;
    @Value("${sms.accessKeySecret}")
    private String accessKeySecret;

    @Override
    public void sendSms(String phoneNumbers, String templateCode, String templateParam) throws Exception {
        Client client = getClient();
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName("新一代信息产业技术研院")
                .setPhoneNumbers(phoneNumbers)
                .setTemplateCode(templateCode);
        if (!StringUtils.isEmpty(templateParam)){
            sendSmsRequest.setTemplateParam(templateParam);
        }
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            SendSmsResponse sendSmsResponse = client.sendSmsWithOptions(sendSmsRequest, runtime);
            log.info("短信发送成功，phoneNumbers="+phoneNumbers+",templateParam="+templateParam+",templateCode="+templateCode+"，接口返回："+sendSmsResponse.getBody().getMessage());
        } catch (Exception e) {
            // 如有需要，请打印 error
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private Client getClient() throws Exception {
        Config config = new Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new Client(config);
    }
}
