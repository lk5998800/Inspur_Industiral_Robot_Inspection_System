package com.inspur.industrialinspection.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopInformationVo {
    //报警次数
    private Integer alarmsNumber;
    //最高温度
    private BigDecimal maxTemperature;
    //总任务
    private Integer countTask;
    //已执行任务
    private Integer executedTask;
    //机器人编号
    private Long robotId;
    //电量
    private String power;
    //任务状态
    private String taskStatus;
    //灭火器数量
    private String fireExtinguisherCount;
    //灭火器检测结果
    private boolean fireExtinguisherAbnormal;
}
