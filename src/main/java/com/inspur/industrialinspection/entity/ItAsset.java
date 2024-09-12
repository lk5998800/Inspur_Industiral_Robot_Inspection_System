package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.*;

/**
 * 资产信息
 * @author kliu
 * @date 2022/7/25 11:20
 */
@Data
@ApiModel("资产信息")
@ToString
public class ItAsset {
  @ApiModelProperty("资产id")
  private long id;
  @Pattern(regexp = "^((?!>|<|%).)((?!>|<|%).){0,50}$", message = "资产编号不能包含<,>或%字符，最长为50个字符")
  @Size(max= 50, message = "资产编号长度不能大于50")
  @NotBlank(message = "资产编号不能为空")
  @ApiModelProperty("资产编号")
  private String assetNo;
  @Pattern(regexp = "^((?!>|<|%).)((?!>|<|%).){0,50}$", message = "资产名称不能包含<,>或%字符，最长为50个字符")
  @Size(max= 50, message = "资产名称长度不能大于50")
  @NotBlank(message = "资产名称不能为空")
  @ApiModelProperty("资产名称")
  private String assetName;
  @Min(value = 1, message = "机房id不能为空")
  @ApiModelProperty("机房id")
  private long roomId;
  @ApiModelProperty("机房名称")
  private String roomName;
  @Size(max= 50, message = "品牌长度不能大于50")
  @ApiModelProperty("品牌")
  private String brand;
  @Size(max= 50, message = "型号长度不能大于50")
  @ApiModelProperty("型号")
  private String model;
  @ApiModelProperty("品牌名称")
  private String brandContent;
  @ApiModelProperty("型号名称")
  private String modelContent;
  @ApiModelProperty("负责人id")
  private long personInChargeId;
  @ApiModelProperty("负责人")
  private String personnelName;
  @ApiModelProperty("负责人部门")
  private String personnelDepartment;
  @Size(max= 200, message = "资产描述长度不能大于200")
  @ApiModelProperty("资产描述")
  private String assetDesc;
  @Size(max= 20, message = "U位长度不能大于20")
  @ApiModelProperty("U位")
  private String uBit;
  @NotBlank(message = "机柜行数不能为空")
  @ApiModelProperty("机柜行")
  private String cabinetRow;
  @Min(value = 1, message = "机柜列数不能为空")
  @ApiModelProperty("机柜列")
  private long cabinetColumn;
  @ApiModelProperty("创建时间")
  private String createTime;
  @ApiModelProperty("资产状态")
  private String result;
  @ApiModelProperty("二维码")
  private String ewmRfid;
  @ApiModelProperty("点位名称")
  private String pointName;
  @ApiModelProperty("楼栋id")
  private long buildingId;
}
