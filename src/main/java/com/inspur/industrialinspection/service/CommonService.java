package com.inspur.industrialinspection.service;

/**
 * 公共Service
 * @author kliu
 * @date 2022/5/9 17:26
 */
public interface CommonService {
    /**
     * double 保留两位小数
     * @param value
     * @return double
     * @author kliu
     * @date 2022/5/25 8:39
     */
    double getFormatValue(double value);
    /**
     * 格式化数据，传入小数位数
     * @param value
     * @return double
     * @author kliu
     * @date 2022/5/25 8:39
     */
    double getFormatValue(double value, int scale);
    /**
     * gzip 压缩
     * @param content
     * @return java.lang.String
     * @author kliu
     * @date 2022/5/25 8:39
     */
    String gzipCompress(String content);
    /**
     * gzip 解压
     * @param content
     * @return java.lang.String
     * @author kliu
     * @date 2022/5/25 8:40
     */
    String gzipUnCompress(String content);

    /**
     * 获取页面上的html
     * @param url
     * @return java.lang.String
     * @author kliu
     * @date 2022/7/4 20:08
     */
    String getHtml(String url);

    /**
     * url转https
     * @param url
     * @return java.lang.String
     * @author kliu
     * @date 2022/10/17 15:11
     */
    String url2Https(String url);
    /**
     * 获取异常全路径
     * @param e
     * @return java.lang.String
     * @author kliu
     * @date 2022/11/30 13:36
     */
    String getExceptionSrintStackTrace(Exception e);
}
