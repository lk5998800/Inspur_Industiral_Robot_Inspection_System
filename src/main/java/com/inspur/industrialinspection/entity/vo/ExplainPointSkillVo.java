package com.inspur.industrialinspection.entity.vo;

import lombok.Data;


@Data
public class ExplainPointSkillVo {
    private long roomId;
    private String pointName;
    private Integer waitingTime = 0;
    private String broadcast;
    private String roomName;
    private Boolean hasExplainPointStatus = false;

}
