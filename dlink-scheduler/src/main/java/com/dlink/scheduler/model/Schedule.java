/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dlink.scheduler.model;

import com.dlink.scheduler.enums.FailureStrategy;
import com.dlink.scheduler.enums.Priority;
import com.dlink.scheduler.enums.ReleaseState;
import com.dlink.scheduler.enums.WarningType;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * schedule
 */
@Data
public class Schedule {

    private int id;

    @ApiModelProperty(value = "工作流编号")
    private long processDefinitionCode;

    @ApiModelProperty(value = "工作流编号")
    private String processDefinitionName;

    @ApiModelProperty(value = "项目名")
    private String projectName;

    @ApiModelProperty(value = "描述")
    private String definitionDescription;

    @ApiModelProperty(value = "计划开始时间")
    private Date startTime;

    @ApiModelProperty(value = "计划结束时间")
    private Date endTime;

    @ApiModelProperty(value = "时区id")
    private String timezoneId;

    @ApiModelProperty(value = "crontab表达式")
    private String crontab;

    @ApiModelProperty(value = "故障处理对策")
    private FailureStrategy failureStrategy;

    @ApiModelProperty(value = "预警型")
    private WarningType warningType;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "创建用户id")
    private int userId;

    @ApiModelProperty(value = "创建用户名")
    private String userName;

    @ApiModelProperty(value = "释放状态")
    private ReleaseState releaseState;

    @ApiModelProperty(value = "警告组id")
    private int warningGroupId;

    @ApiModelProperty(value = "流程实例优先")
    private Priority processInstancePriority;

    @ApiModelProperty(value = "警告组名称")
    private String workerGroup;

    @ApiModelProperty(value = "环境变量")
    private Long environmentCode;

}
