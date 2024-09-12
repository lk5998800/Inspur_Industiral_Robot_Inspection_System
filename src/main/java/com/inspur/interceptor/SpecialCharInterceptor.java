package com.inspur.interceptor;

import com.alibaba.druid.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: kliu
 * @description: 特殊字符
 * @date: 2022/8/30 11:21
 */
@Slf4j
public class SpecialCharInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String dictValue = "<>?''/？%";
        if(StringUtils.isEmpty(dictValue)){
            return true;
        }
        String reg = "["+dictValue+"]{1,}";
        //noinspection AlibabaUndefineMagicConstant
        if("GET".equals(httpServletRequest.getMethod())){
            try {
                String urls = httpServletRequest.getQueryString();
                if (!StringUtils.isEmpty(urls)) {
                    // url参数转义
                    urls = URLDecoder.decode(urls, "utf-8");
                    Pattern p = Pattern.compile(reg);
                    Matcher m = p.matcher(urls);
                    if (m.find()) {
                        throw new RuntimeException("请求参数中不能包含特殊字符");
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else if("POST".equals(httpServletRequest.getMethod())){
            //raw json
//            String postData = getBodyString(httpServletRequest);
//            if (StringUtils.isEmpty(postData)) {
//                //form-data
//                Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
//                Set<String> strings = parameterMap.keySet();
//                for (String key : strings) {
//                    String[] strings1 = parameterMap.get(key);
//                    for (int i = 0; i < strings1.length; i++) {
//                        postData+=strings1[i];
//                    }
//                }
//                parameterMap = null;
//            }
//
//            if (!StringUtils.isEmpty(postData)) {
//                log.info("postData="+postData);
//                Pattern p = Pattern.compile(reg);
//                Matcher m = p.matcher(postData);
//                if (m.find()) {
//                    throw new RuntimeException("请求参数中不能包含特殊字符");
//                }
//            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }

    public String getBodyString(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = cloneInputStream(request.getInputStream());
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public InputStream cloneInputStream(ServletInputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buffer)) > -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            byteArrayOutputStream.flush();
        }catch (IOException e) {
            e.printStackTrace();
        }
        InputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        return byteArrayInputStream;
    }
}
