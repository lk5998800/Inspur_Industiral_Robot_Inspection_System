package com.inspur.industrialinspection.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 人员管理
 * @author kliu
 * @date 2022/7/21 16:46
 */
@ApiModel("人员管理")
@ToString
@Data
public class PersonnelManagement {
  @ApiModelProperty("人员id")
  private long personnelId;
  @Pattern(regexp = "^((?!=|\\+|-|@|>|<|%).)((?!>|<|%).){0,50}$", message = "人员姓名不能以=,+,-或@开头，不能包含<,>或%字符，最长为50个字符")
  @Size(max = 50, message = "人员姓名长度不能大于50")
  @NotBlank(message = "人员姓名不能为空")
  @ApiModelProperty("人员姓名")
  private String personnelName;
  @Size(max = 12, message = "联系电话长度必须介于11-12位之间")
  @Size(min = 11, message = "联系电话长度必须介于11-12位之间")
  @NotBlank(message = "联系电话不能为空")
  @ApiModelProperty("联系电话")
  private String personnelTel;
  @Pattern(regexp = "^((?!=|\\+|-|@|>|<|%).)((?!>|<|%).){0,100}$", message = "所属公司不能以=,+,-或@开头，不能包含<,>或%字符，最长为50个字符")
  @Size(max = 100, message = "所属公司长度不能大于100")
  @ApiModelProperty("所属公司")
  private String personnelFirm;
  @Pattern(regexp = "^((?!=|\\+|-|@|>|<|%).)((?!>|<|%).){0,50}$", message = "所属部门不能以=,+,-或@开头，不能包含<,>或%字符，最长为50个字符")
  @Size(max = 50, message = "所属部门长度不能大于50")
  @ApiModelProperty("所属部门")
  private String personnelDepartment;
  @NotBlank(message = "人员类型不能为空")
  @ApiModelProperty("人员类型")
  private String personnelType;
  @Size(max = 50, message = "电子邮箱长度不能大于50")
  @ApiModelProperty("电子邮箱")
  private String personnelEmail;
  @Size(max = 300, message = "人员介绍长度不能大于300")
  @ApiModelProperty("人员介绍")
  private String personnelIntroduce;
  @Size(max = 20, message = "生效日期长度不能大于20")
  @ApiModelProperty("生效日期")
  private String personnelEffectiveDate;
  @Size(max = 20, message = "失效日期长度不能大于20")
  @ApiModelProperty("失效日期")
  private String personnelExpirationDate;
  @ApiModelProperty("人像url")
  private String personnelUrl;
  @ApiModelProperty("人像特征值")
  private String personnelFacialFeature;
}
