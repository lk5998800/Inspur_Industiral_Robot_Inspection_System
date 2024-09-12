package com.inspur.industrialinspection.entity.vo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class WarnMessageResultVo {
    private String name;
    private String type;
    private String status;
    private String time;
    private String typeChi;
}