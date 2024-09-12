package com.inspur.industrialinspection.entity.vo;

import lombok.Data;

@Data
public class ExplainPointStatusVo {
    private long roomId;
    private String pointName;
    private Boolean hasExplainPointStatus;
}
