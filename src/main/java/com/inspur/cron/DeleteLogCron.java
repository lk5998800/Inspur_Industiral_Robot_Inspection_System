package com.inspur.cron;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

/**
 * 定时删除日志
 * @author kliu
 * @date 2022/8/6 11:10
 */
@Component
@Slf4j
public class DeleteLogCron {
    @Value("${log.filepath}")
    private String logFilePath;

    /**
     * 每天凌晨1点执行一次，删除30天前的日志信息
     * @author kliu
     * @date 2022/8/6 9:56
     */
    @Scheduled(cron = "0 0 1 ? * ? ")
    @Transactional(rollbackFor = Exception.class)
    public void taskExecute() {
        long dayBefore30Sjc = DateUtil.offsetDay(DateUtil.date(), -30).getTime();
        File file = new File(logFilePath);
        deleteLogFile(file, dayBefore30Sjc);
    }

    /**
     * 递归删除文件
     * @param file
     * @param sjc
     * @return void
     * @author kliu
     * @date 2022/8/6 11:23
     */
    private void deleteLogFile(File file, long sjc){
        if (file.isDirectory()){
            for (File listFile : file.listFiles()) {
                if (listFile.isDirectory()){
                    deleteLogFile(listFile, sjc);
                }else{
                    if (listFile.isFile()){
                        long lastModifiedSjc = listFile.lastModified();
                        if (lastModifiedSjc<sjc){
                            listFile.delete();
                        }
                    }
                }
            }
        }
    }
}
