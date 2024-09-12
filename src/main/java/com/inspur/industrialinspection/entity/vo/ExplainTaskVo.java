package com.inspur.industrialinspection.entity.vo;


import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class ExplainTaskVo {
    private long id;
    private long roomId;
    private String taskName;
    private String taskType;
    private String taskTime;
    private String taskDescribe;
    private String points;
    private String startTime;
    private String endTime;
    private String status;
    private String reason;
    private String createTime;
    private long createUserId;
    private String roomName;
}
