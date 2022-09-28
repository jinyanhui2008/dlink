/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dlink.controller;

import com.dlink.common.result.Result;
import com.dlink.init.SystemInit;
import com.dlink.model.Catalogue;
import com.dlink.scheduler.client.ExecutorClient;
import com.dlink.scheduler.client.ProcessClient;
import com.dlink.scheduler.client.SchedulerClient;
import com.dlink.scheduler.client.TaskClient;
import com.dlink.scheduler.enums.CommandType;
import com.dlink.scheduler.enums.ComplementDependentMode;
import com.dlink.scheduler.enums.ExecutionStatus;
import com.dlink.scheduler.enums.FailureStrategy;
import com.dlink.scheduler.enums.Priority;
import com.dlink.scheduler.enums.ReleaseState;
import com.dlink.scheduler.enums.RunMode;
import com.dlink.scheduler.enums.TaskDependType;
import com.dlink.scheduler.enums.TaskType;
import com.dlink.scheduler.enums.WarningType;
import com.dlink.scheduler.exception.SchedulerException;
import com.dlink.scheduler.model.DagData;
import com.dlink.scheduler.model.ProcessDefinition;
import com.dlink.scheduler.model.ProcessDto;
import com.dlink.scheduler.model.ProcessInstance;
import com.dlink.scheduler.model.Project;
import com.dlink.scheduler.model.ScheduleRequest;
import com.dlink.scheduler.model.ScheduleVo;
import com.dlink.scheduler.model.TaskDefinition;
import com.dlink.scheduler.model.TaskMainInfo;
import com.dlink.scheduler.model.TaskRequest;
import com.dlink.scheduler.utils.ParamUtil;
import com.dlink.service.CatalogueService;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author 郑文豪
 */
@RestController
@RequestMapping("/api/scheduler")
@Api(value = "海豚调度", tags = "海豚调度")
public class SchedulerController {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerController.class);

    @Value("${dinky.url}")
    private String dinkyUrl;
    @Value("${dinky.dolphinscheduler.url}")
    private String url;

    @Autowired
    private ProcessClient processClient;

    @Autowired
    private TaskClient taskClient;
    @Autowired
    private CatalogueService catalogueService;
    @Autowired
    private ExecutorClient executorClient;
    @Autowired
    private SchedulerClient schedulerClient;

    public static final String DEFAULT_WARNING_TYPE = "NONE";
    public static final String DEFAULT_NOTIFY_GROUP_ID = "1";
    public static final String DEFAULT_FAILURE_POLICY = "CONTINUE";
    public static final String DEFAULT_PROCESS_INSTANCE_PRIORITY = "MEDIUM";

    @GetMapping
    @ApiOperation(value = "查看海豚调度是否可用", notes = "查看海豚调度是否可用")
    public Result<Boolean> isExist() {
        try {
            SystemInit.getProject();
        } catch (SchedulerException e) {
            logger.info(e.getMessage(), e);
            return Result.succeed(false);
        }
        return Result.succeed(true);
    }

    /**
     * 获取任务定义
     */
    @GetMapping("/task")
    @ApiOperation(value = "获取任务定义", notes = "获取任务定义")
    public Result<TaskDefinition> getTaskDefinition(@ApiParam(value = "dinky任务id") @RequestParam Integer dinkyTaskId) {
        TaskDefinition taskDefinition = null;
        Project dinkyProject = SystemInit.getProject();

        Catalogue catalogue = catalogueService.getTaskById(dinkyTaskId);
        if (catalogue == null) {
            return Result.failed("节点获取失败");
        }
        if (catalogue.getParentId() == 0) {
            return Result.failed("节点没有父级目录");
        }
        Catalogue parent = catalogueService.getById(catalogue.getParentId());
        if (parent == null) {
            return Result.failed("节点父级获取失败");
        }
        String taskName = catalogue.getName() + ":" + catalogue.getTaskId();
        String processName = parent.getName() + ":" + parent.getId();

        long projectCode = dinkyProject.getCode();
        TaskMainInfo taskMainInfo = taskClient.getTaskMainInfo(projectCode, processName, taskName);

        if (taskMainInfo != null) {
            taskDefinition = taskClient.getTaskDefinition(projectCode, taskMainInfo.getTaskCode());

            if (taskDefinition != null) {
                taskDefinition.setProcessDefinitionCode(taskMainInfo.getProcessDefinitionCode());
                taskDefinition.setProcessDefinitionName(taskMainInfo.getProcessDefinitionName());
                taskDefinition.setProcessDefinitionVersion(taskMainInfo.getProcessDefinitionVersion());
                taskDefinition.setUpstreamTaskMap(taskMainInfo.getUpstreamTaskMap());
            } else {
                return Result.failed("请先工作流保存");
            }
        }
        return Result.succeed(taskDefinition);
    }

    /**
     * 获取前置任务定义集合
     */
    @GetMapping("/upstream/tasks")
    @ApiOperation(value = "获取前置任务定义集合", notes = "获取前置任务定义集合")
    public Result<List<TaskMainInfo>> getTaskMainInfos(@ApiParam(value = "dinky任务id") @RequestParam Integer dinkyTaskId) {

        Project dinkyProject = SystemInit.getProject();

        Catalogue catalogue = catalogueService.getTaskById(dinkyTaskId);
        if (catalogue == null) {
            return Result.failed("节点获取失败");
        }
        if (catalogue.getParentId() == 0) {
            return Result.failed("节点没有父级目录");
        }
        Catalogue parent = catalogueService.getById(catalogue.getParentId());
        if (parent == null) {
            return Result.failed("节点父级获取失败");
        }
        String processName = parent.getName() + ":" + parent.getId();

        long projectCode = dinkyProject.getCode();

        List<TaskMainInfo> taskMainInfos = taskClient.getTaskMainInfos(projectCode, processName, "");
        //去掉本身
        taskMainInfos.removeIf(taskMainInfo -> (catalogue.getName() + ":" + catalogue.getId()).equalsIgnoreCase(taskMainInfo.getTaskName()));

        return Result.succeed(taskMainInfos);
    }

    /**
     * 创建任务定义
     */
    @PostMapping("/task")
    @ApiOperation(value = "创建任务定义", notes = "创建任务定义")
    public Result<String> createTaskDefinition(@ApiParam(value = "前置任务编号 逗号隔开") @RequestParam(required = false) String upstreamCodes,
                                               @ApiParam(value = "dinky任务id") @RequestParam Integer dinkyTaskId,
                                               @ApiParam(value = "子节点工作流编号") @RequestParam(required = false) Long processDefinitionCode,
                                               @Valid @RequestBody TaskRequest taskRequest) {
        String taskParams;
        if (StringUtils.isNotBlank(taskRequest.getTaskType())) {
            TaskType taskType = TaskType.valueOf(taskRequest.getTaskType());
            switch (taskType) {
                case DINKY:
                    taskParams = ParamUtil.getDlinkTaskParams(url, dinkyTaskId);
                    break;
                case SUB_PROCESS:
                    taskParams = ParamUtil.getSubProcessParams(processDefinitionCode);
                    break;
                default:
                    throw new SchedulerException("未处理类型");
            }
            taskRequest.setTaskParams(taskParams);
        }
        Project dinkyProject = SystemInit.getProject();

        Catalogue catalogue = catalogueService.getTaskById(dinkyTaskId);
        if (catalogue == null) {
            return Result.failed("节点获取失败");
        }
        if (catalogue.getParentId() == 0) {
            return Result.failed("节点没有父级目录");
        }
        Catalogue parent = catalogueService.getById(catalogue.getParentId());
        if (parent == null) {
            return Result.failed("节点父级获取失败");
        }
        String taskName = catalogue.getName() + ":" + catalogue.getTaskId();
        String processName = parent.getName() + ":" + parent.getId();

        long projectCode = dinkyProject.getCode();
        ProcessDefinition process = processClient.getProcessDefinitionInfo(projectCode, processName);
        taskRequest.setName(taskName);
        if (process == null) {
            Long taskCode = taskClient.genTaskCode(projectCode);
            taskRequest.setCode(taskCode);
            JSONObject jsonObject = JSONUtil.parseObj(taskRequest);
            JSONArray array = new JSONArray();
            array.set(jsonObject);
            processClient.createProcessDefinition(projectCode, processName, taskCode, array.toString());

            return Result.succeed("添加工作流定义成功");
        } else {
            if (process.getReleaseState() == ReleaseState.ONLINE) {
                return Result.failed("工作流定义 [" + processName + "] 已经上线已经上线");
            }
            long processCode = process.getCode();
            TaskMainInfo taskDefinitionInfo = taskClient.getTaskMainInfo(projectCode, processName, taskName);
            if (taskDefinitionInfo != null) {
                return Result.failed("添加失败,工作流定义[" + processName + "]已存在任务定义[" + taskName + "] 请刷新");
            }

            String taskDefinitionJsonObj = JSONUtil.toJsonStr(taskRequest);
            taskClient.createTaskDefinition(projectCode, processCode, upstreamCodes, taskDefinitionJsonObj);

            return Result.succeed("添加任务定义成功");
        }

    }

    /**
     * 更新任务定义
     */
    @PutMapping("/task")
    @ApiOperation(value = "更新任务定义", notes = "更新任务定义")
    public Result<String> updateTaskDefinition(@ApiParam(value = "项目编号") @RequestParam long projectCode,
                                               @ApiParam(value = "工作流定义编号") @RequestParam long processCode,
                                               @ApiParam(value = "任务定义编号") @RequestParam long taskCode,
                                               @ApiParam(value = "前置任务编号 逗号隔开") @RequestParam(required = false) String upstreamCodes,
                                               @Valid @RequestBody TaskRequest taskRequest) {

        TaskDefinition taskDefinition = taskClient.getTaskDefinition(projectCode, taskCode);
        if (taskDefinition == null) {
            return Result.failed("任务不存在");
        }
        if (!TaskType.DINKY.name().equals(taskDefinition.getTaskType())) {
            return Result.failed("海豚调度类型为[" + taskDefinition.getTaskType() + "] 不支持,非DINKY类型");
        }
        DagData dagData = processClient.getProcessDefinitionInfo(projectCode, processCode);
        if (dagData == null) {
            return Result.failed("工作流定义不存在");
        }
        ProcessDefinition process = dagData.getProcessDefinition();
        if (process == null) {
            return Result.failed("工作流定义不存在");
        }
        if (process.getReleaseState() == ReleaseState.ONLINE) {
            return Result.failed("工作流定义 [" + process.getName() + "] 已经上线");
        }

        taskRequest.setName(taskDefinition.getName());
        taskRequest.setTaskParams(taskDefinition.getTaskParams());
        taskRequest.setTaskType(TaskType.DINKY.name());

        String taskDefinitionJsonObj = JSONUtil.toJsonStr(taskRequest);
        taskClient.updateTaskDefinition(projectCode, taskCode, upstreamCodes, taskDefinitionJsonObj);
        return Result.succeed("修改成功");
    }

    @ApiOperation(value = "工作流上线下线", notes = "工作流上线下线")
    @PostMapping(value = "/{catalogueId}/release")
    public Result<String> releaseProcessDefinition(@ApiParam(value = "dinky目录id") @PathVariable(value = "catalogueId") Integer catalogueId,
                                                   @ApiParam(value = "上线/下线 状态") @RequestParam(value = "releaseState") ReleaseState releaseState) {
        Project dinkyProject = SystemInit.getProject();
        Long projectCode = dinkyProject.getCode();
        Catalogue catalogue = catalogueService.getById(catalogueId);
        if (catalogue == null) {
            return Result.failed("目录获取失败");
        }
        if (catalogue.getType() != null) {
            return Result.failed("当前节点不是目录");
        }
        String processName = catalogue.getName() + ":" + catalogue.getId();

        ProcessDefinition processDefinition = processClient.getProcessDefinitionInfo(projectCode, processName);
        if (processDefinition == null) {
            return Result.failed("请先创建对应的工作流");
        }
        processClient.releaseProcessDefinition(projectCode, processDefinition.getCode(), releaseState);
        return Result.succeed("操作成功");
    }

    @ApiOperation(value = "执行工作流", notes = "执行工作流")
    @PostMapping(value = "/{catalogueId}/start-process")
    public Result<String> startProcessInstance(@ApiParam(value = "dinky目录id") @PathVariable(value = "catalogueId") Integer catalogueId,
                                               @ApiParam(value = "cron 时间") @RequestParam(value = "scheduleTime") String scheduleTime,
                                               @ApiParam(value = "失败策略") @RequestParam(value = "failureStrategy") FailureStrategy failureStrategy,
                                               @ApiParam(value = "开始节点列表") @RequestParam(value = "startNodeList", required = false) String startNodeList,
                                               @ApiParam(value = "节点依赖类型") @RequestParam(value = "taskDependType", required = false) TaskDependType taskDependType,
                                               @ApiParam(value = "命令类型") @RequestParam(value = "execType", required = false) CommandType execType,
                                               @ApiParam(value = "预警类型") @RequestParam(value = "warningType") WarningType warningType,
                                               @ApiParam(value = "通知组id") @RequestParam(value = "warningGroupId", required = false, defaultValue = "0") Integer warningGroupId,
                                               @ApiParam(value = "流程实例优先") @RequestParam(value = "runMode", required = false) RunMode runMode,
                                               @ApiParam(value = "流程实例优先") @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority,
                                               @ApiParam(value = "通知组名称") @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                               @ApiParam(value = "环境编号") @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") Long environmentCode,
                                               @ApiParam(value = "超时") @RequestParam(value = "timeout", required = false) Integer timeout,
                                               @ApiParam(value = "传递给新流程实例的全局参数值") @RequestParam(value = "startParams", required = false) String startParams,
                                               @ApiParam(value = "在并行模式下执行补码时期望的并行数") @RequestParam(value = "expectedParallelismNumber", required = false) Integer expectedParallelismNumber,
                                               @ApiParam(value = "预执行") @RequestParam(value = "dryRun", defaultValue = "0", required = false) int dryRun,
                                               @ApiParam(value = "任务节点依赖类型") @RequestParam(value = "complementDependentMode", required = false) ComplementDependentMode complementDependentMode) {

        Project dinkyProject = SystemInit.getProject();
        Long projectCode = dinkyProject.getCode();
        Catalogue catalogue = catalogueService.getById(catalogueId);
        if (catalogue == null) {
            return Result.failed("目录获取失败");
        }
        if (catalogue.getType() != null) {
            return Result.failed("当前节点不是目录");
        }
        String processName = catalogue.getName() + ":" + catalogue.getId();

        ProcessDefinition processDefinition = processClient.getProcessDefinitionInfo(projectCode, processName);
        if (processDefinition == null) {
            return Result.failed("请先创建对应的工作流");
        }

        executorClient.startProcessInstance(projectCode, processDefinition.getCode(), scheduleTime, failureStrategy, startNodeList, taskDependType,
            execType, warningType, warningGroupId, runMode, processInstancePriority, workerGroup, environmentCode, timeout, startParams,
            expectedParallelismNumber, dryRun, complementDependentMode);

        return Result.succeed("操作成功");
    }

    @ApiOperation(value = "查看工作流日志", notes = "查看工作流日志")
    @GetMapping("/{catalogueId}/instance")
    public Result<List<ProcessInstance>> queryProcessInstanceList(@ApiParam(value = "dinky目录id") @PathVariable(value = "catalogueId") Integer catalogueId,
                                                                  @ApiParam(value = "名称") @RequestParam(value = "searchVal", required = false) String searchVal,
                                                                  @ApiParam(value = "执行用户") @RequestParam(value = "executorName", required = false) String executorName,
                                                                  @ApiParam(value = "状态") @RequestParam(value = "stateType", required = false) ExecutionStatus stateType,
                                                                  @ApiParam(value = "主机") @RequestParam(value = "host", required = false) String host,
                                                                  @ApiParam(value = "开始时间") @RequestParam(value = "startDate", required = false) String startTime,
                                                                  @ApiParam(value = "结束时间") @RequestParam(value = "endDate", required = false) String endTime,
                                                                  @ApiParam(value = "分页") @RequestParam("pageNo") Integer pageNo,
                                                                  @ApiParam(value = "分页") @RequestParam("pageSize") Integer pageSize) {

        Project dinkyProject = SystemInit.getProject();
        Long projectCode = dinkyProject.getCode();
        Catalogue catalogue = catalogueService.getById(catalogueId);
        if (catalogue == null) {
            return Result.failed("目录获取失败");
        }
        if (catalogue.getType() != null) {
            return Result.failed("当前节点不是目录");
        }
        String processName = catalogue.getName() + ":" + catalogue.getId();

        ProcessDefinition processDefinition = processClient.getProcessDefinitionInfo(projectCode, processName);
        if (processDefinition == null) {
            return Result.failed("请先创建对应的工作流");
        }

        List<ProcessInstance> lists = processClient.queryProcessInstanceList(projectCode, processDefinition.getCode(), startTime, endTime, searchVal,
            executorName, stateType, host, pageNo, pageSize);

        String schedulerUrl = url + "/ui/projects/" + projectCode + "/workflow/instances/";
        for (ProcessInstance list : lists) {
            list.setSchedulerUrl(schedulerUrl + list.getId() + "?code=" + list.getProcessDefinitionCode());
        }
        return Result.succeed(lists);
    }

    @ApiOperation(value = "获取定时任务", notes = "获取定时任务")
    @GetMapping("/{catalogueId}/schedule")
    public Result<ScheduleVo> queryScheduleListPaging(@ApiParam(value = "dinky目录id") @PathVariable(value = "catalogueId") Integer catalogueId) {
        Project dinkyProject = SystemInit.getProject();
        Long projectCode = dinkyProject.getCode();
        Catalogue catalogue = catalogueService.getById(catalogueId);
        if (catalogue == null) {
            return Result.failed("目录获取失败");
        }
        if (catalogue.getType() != null) {
            return Result.failed("当前节点不是目录");
        }
        String processName = catalogue.getName() + ":" + catalogue.getId();

        ProcessDefinition processDefinition = processClient.getProcessDefinitionInfo(projectCode, processName);
        if (processDefinition == null) {
            return Result.failed("请先创建对应的工作流");
        }
        ScheduleVo scheduleVo = schedulerClient.querySchedule(projectCode, processDefinition.getCode(), processName);
        return Result.succeed(scheduleVo);
    }

    @ApiOperation(value = "添加或修改定时调度", notes = "添加或修改定时调度")
    @PostMapping("/{catalogueId}")
    public Result<String> createSchedule(@ApiParam(value = "dinky目录id") @PathVariable(value = "catalogueId") Integer catalogueId,
                                         @ApiParam(value = "调度id") @RequestParam(value = "scheduleId", required = false) Integer scheduleId,
                                         @ApiParam(value = "预警类型") @RequestParam(value = "warningType", required = false, defaultValue = DEFAULT_WARNING_TYPE) WarningType warningType,
                                         @ApiParam(value = "警告组id") @RequestParam(value = "warningGroupId", required = false, defaultValue = DEFAULT_NOTIFY_GROUP_ID) int warningGroupId,
                                         @ApiParam(value = "失败策略") @RequestParam(value = "failureStrategy", required = false, defaultValue = DEFAULT_FAILURE_POLICY) FailureStrategy failureStrategy,
                                         @ApiParam(value = "警告组名称") @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                         @ApiParam(value = "环境编号") @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") Long environmentCode,
                                         @ApiParam(value = "流程实例优先") @RequestParam(value = "processInstancePriority", required = false, defaultValue = DEFAULT_PROCESS_INSTANCE_PRIORITY)
                                             Priority processInstancePriority,
                                         @ApiParam(value = "预警类型") @RequestBody ScheduleRequest scheduleRequest) {

        Project dinkyProject = SystemInit.getProject();
        Long projectCode = dinkyProject.getCode();
        Catalogue catalogue = catalogueService.getById(catalogueId);
        if (catalogue == null) {
            return Result.failed("目录获取失败");
        }
        if (catalogue.getType() != null) {
            return Result.failed("当前节点不是目录");
        }
        String processName = catalogue.getName() + ":" + catalogue.getId();

        ProcessDefinition processDefinition = processClient.getProcessDefinitionInfo(projectCode, processName);
        if (processDefinition == null) {
            return Result.failed("请先创建对应的工作流");
        }
        String scheduleJson = JSONUtil.toJsonStr(scheduleRequest);
        if (scheduleId == null) {
            schedulerClient.createSchedule(projectCode, processDefinition.getCode(), scheduleJson,
                warningType, warningGroupId, failureStrategy, processInstancePriority, workerGroup, environmentCode);
            return Result.succeed("添加成功");
        } else {
            schedulerClient.updateSchedule(projectCode, scheduleId, scheduleJson,
                warningType, warningGroupId, failureStrategy, processInstancePriority, workerGroup, environmentCode);
            return Result.succeed("修改成功");
        }
    }

    @ApiOperation(value = "执行时间预览", notes = "执行时间预览")
    @PostMapping("/schedule/preview")
    public Result<List<String>> previewSchedule(@ApiParam(value = "crontab表达式") @RequestParam String schedule) {
        Project dinkyProject = SystemInit.getProject();
        Long projectCode = dinkyProject.getCode();
        List<String> lists = schedulerClient.previewSchedule(projectCode, schedule);
        return Result.succeed(lists);
    }

    @ApiOperation(value = "获取工作流程集合", notes = "获取工作流程集合")
    @GetMapping(value = "/process/simple-list")
    public Result<ProcessDto> queryProcessDefinitionSimpleList() {
        Project dinkyProject = SystemInit.getProject();
        Long projectCode = dinkyProject.getCode();
        ProcessDto processDto = processClient.queryProcessDefinitionSimpleList(projectCode);
        return Result.succeed(processDto);
    }
}
