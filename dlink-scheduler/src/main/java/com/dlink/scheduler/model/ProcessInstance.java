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

import com.dlink.scheduler.enums.CommandType;
import com.dlink.scheduler.enums.ExecutionStatus;
import com.dlink.scheduler.enums.FailureStrategy;
import com.dlink.scheduler.enums.Flag;
import com.dlink.scheduler.enums.Priority;
import com.dlink.scheduler.enums.TaskDependType;
import com.dlink.scheduler.enums.WarningType;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * process instance
 */
@Data
public class ProcessInstance {

    private int id;

    @ApiModelProperty(value = "工作流编号")
    private Long processDefinitionCode;

    @ApiModelProperty(value = "流程定义版本")
    private int processDefinitionVersion;

    @ApiModelProperty(value = "流程状态")
    private ExecutionStatus state;

    @ApiModelProperty(value = "故障转移的恢复标志")
    private Flag recovery;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "运行时间")
    private int runTimes;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "地址")
    private String host;

    @ApiModelProperty(value = "工作流定义")
    private ProcessDefinition processDefinition;

    @ApiModelProperty(value = "过程命令类型")
    private CommandType commandType;

    @ApiModelProperty(value = "命令参数")
    private String commandParam;

    @ApiModelProperty(value = "节点依赖类型")
    private TaskDependType taskDependType;

    @ApiModelProperty(value = "任务最大尝试次数")
    private int maxTryTimes;

    @ApiModelProperty(value = "任务失败时的失败策略")
    private FailureStrategy failureStrategy;

    @ApiModelProperty(value = "预警类型")
    private WarningType warningType;

    @ApiModelProperty(value = "告警组id")
    private Integer warningGroupId;

    @ApiModelProperty(value = "调度时间")
    private Date scheduleTime;

    @ApiModelProperty(value = "命令启动时间")
    private Date commandStartTime;

    @ApiModelProperty(value = "用户定义参数字符串")
    private String globalParams;

    @ApiModelProperty(value = "dagData")
    private DagData dagData;

    @ApiModelProperty(value = "执行人id")
    private int executorId;

    @ApiModelProperty(value = "执行人")
    private String executorName;

    @ApiModelProperty(value = "租户编号")
    private String tenantCode;

    @ApiModelProperty(value = "队列")
    private String queue;

    @ApiModelProperty(value = "是否子工作流")
    private Flag isSubProcess;

    @ApiModelProperty(value = "task locations for web")
    private String locations;

    @ApiModelProperty(value = "历史命令")
    private String historyCmd;

    @ApiModelProperty(value = "依赖流程调度时间")
    private String dependenceScheduleTimes;

    @ApiModelProperty(value = "流程持续时间")
    private String duration;

    @ApiModelProperty(value = "优先级")
    private Priority processInstancePriority;

    @ApiModelProperty(value = "告警组名称")
    private String workerGroup;

    @ApiModelProperty(value = "环境编号")
    private Long environmentCode;

    @ApiModelProperty(value = "超时警告")
    private int timeout;

    @ApiModelProperty(value = "租户id")
    private int tenantId;

    @ApiModelProperty(value = "常量池")
    private String varPool;

    @ApiModelProperty(value = "serial queue next processInstanceId")
    private int nextProcessInstanceId;

    @ApiModelProperty(value = "预运行")
    private int dryRun;

    @ApiModelProperty(value = "重启时间")
    private Date restartTime;

    @ApiModelProperty(value = "workflow block flag")
    private boolean isBlocked;

    @ApiModelProperty(value = "海豚调度url")
    private String schedulerUrl;
}
