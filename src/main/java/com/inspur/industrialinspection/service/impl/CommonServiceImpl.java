package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.codec.Base64;
import com.alibaba.druid.util.StringUtils;
import com.inspur.industrialinspection.service.CommonService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 公共Service
 * @author kliu
 * @date 2022/5/9 17:26
 */
@Service
public class CommonServiceImpl implements CommonService {
    @Value("${composite.image.wwurl}")
    private String compositeImageUrl;
    /**
     * double 保留两位小数
     * @param value
     * @return double
     * @author kliu
     * @date 2022/5/25 8:39
     */
    @Override
    public double getFormatValue(double value) {
        BigDecimal b = new BigDecimal(value);
        return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    @Override
    public double getFormatValue(double value, int scale) {
        BigDecimal b = new BigDecimal(value);
        return b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * gzip 压缩
     * @param content
     * @return java.lang.String
     * @author kliu
     * @date 2022/5/25 8:39
     */
    @Override
    public String gzipCompress(String content) {
        if (content == null || content.length() == 0) {
            return content;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        byte[] bytes = out.toByteArray();
        return Base64.encode(bytes);
    }

    /**
     * gzip 解压
     * @param content
     * @return java.lang.String
     * @author kliu
     * @date 2022/5/25 8:40
     */
    @Override
    public String gzipUnCompress(String content) {
        if (content == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = null;
        GZIPInputStream ginzip = null;
        byte[] compressed = null;
        String decompressed = null;
        try {
            compressed = Base64.decode(content);
            in = new ByteArrayInputStream(compressed);
            ginzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            decompressed = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ginzip != null) {
                try {
                    ginzip.close();
                } catch (IOException e) {
                }//加入Java开发交流君样：756584822一起吹水聊天
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return decompressed;
    }

    /**
     * 获取页面上的html
     * @param url
     * @return java.lang.String
     * @author kliu
     * @date 2022/7/4 20:08
     */
    @Override
    public String getHtml(String url) {
        BufferedReader in = null;
        //定义字符缓冲区
        StringBuffer stringBuffer = new StringBuffer();
        try {
            //创建URL地址
            URL net = new URL(url);
            //打开URL
            URLConnection connection = net.openConnection();
            //开启输入/输出。注意：请求网络需要传参必须开启
            //实例化字符缓冲输入流来读取数据
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line = null;
            while ((line = in.readLine()) != null) {
                //用stringBuffer拼接数据
                stringBuffer.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuffer.toString();
    }

    @Override
    public String url2Https(String url) {
        if (StringUtils.isEmpty(url)) {
            return url;
        }
        if (url.indexOf("repic")>-1){
            url = compositeImageUrl+url.replace("./repic","/repic");
        }else{
            int position = url.indexOf("/",10);
            url = compositeImageUrl+url.substring(position);
        }

        return url;
    }

    @Override
    public String getExceptionSrintStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
