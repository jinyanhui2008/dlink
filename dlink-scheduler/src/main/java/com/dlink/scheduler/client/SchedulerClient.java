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

package com.dlink.scheduler.client;

import com.dlink.scheduler.constant.Constants;
import com.dlink.scheduler.enums.FailureStrategy;
import com.dlink.scheduler.enums.Priority;
import com.dlink.scheduler.enums.WarningType;
import com.dlink.scheduler.model.Schedule;
import com.dlink.scheduler.model.ScheduleVo;
import com.dlink.scheduler.result.PageInfo;
import com.dlink.scheduler.result.Result;
import com.dlink.scheduler.utils.MyJSONUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;

/**
 * @author 郑文豪
 */
@Component
public class SchedulerClient {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerClient.class);

    @Value("${dinky.dolphinscheduler.url}")
    private String url;
    @Value("${dinky.dolphinscheduler.token}")
    private String tokenKey;

    public Schedule createSchedule(Long projectCode, Long processCode, String schedule, WarningType warningType,
                                   int warningGroupId, FailureStrategy failureStrategy, Priority processInstancePriority,
                                   String workerGroup, Long environmentCode) {
        Map<String, Object> map = new HashMap<>();
        map.put("projectCode", projectCode);
        String format = StrUtil.format(url + "/projects/{projectCode}/schedules", map);

        Map<String, Object> params = new HashMap<>();
        params.put("processDefinitionCode", processCode);
        params.put("schedule", schedule);
        params.put("warningType", warningType);
        params.put("warningGroupId", warningGroupId);
        params.put("failureStrategy", failureStrategy);
        params.put("processInstancePriority", processInstancePriority);
        params.put("workerGroup", workerGroup);
        params.put("environmentCode", environmentCode);

        String content = HttpRequest.post(format)
            .header(Constants.TOKEN, tokenKey)
            .form(params)
            .timeout(5000)
            .execute().body();

        return MyJSONUtil.verifyResult(MyJSONUtil.toBean(content, new TypeReference<Result<Schedule>>() {
        }));
    }

    public Schedule updateSchedule(Long projectCode, Integer scheduleId, String schedule, WarningType warningType,
                                   int warningGroupId, FailureStrategy failureStrategy, Priority processInstancePriority,
                                   String workerGroup, Long environmentCode) {
        Map<String, Object> map = new HashMap<>();
        map.put("projectCode", projectCode);
        map.put("id", scheduleId);
        String format = StrUtil.format(url + "/projects/{projectCode}/schedules", map);

        Map<String, Object> params = new HashMap<>();
        params.put("schedule", schedule);
        params.put("warningType", warningType);
        params.put("warningGroupId", warningGroupId);
        params.put("failureStrategy", failureStrategy);
        params.put("processInstancePriority", processInstancePriority);
        params.put("workerGroup", workerGroup);
        params.put("environmentCode", environmentCode);

        String content = HttpRequest.put(format)
            .header(Constants.TOKEN, tokenKey)
            .form(params)
            .timeout(5000)
            .execute().body();

        return MyJSONUtil.verifyResult(MyJSONUtil.toBean(content, new TypeReference<Result<Schedule>>() {
        }));
    }

    /**
     * 获取调度集合
     *
     * @param projectCode 项目编号
     * @param processCode 工作流编号
     * @param processName 描述
     * @return {@link ScheduleVo}
     * @author 郑文豪
     * @date 2022/9/27 9:31
     */
    public List<ScheduleVo> querySchedules(Long projectCode, Long processCode, String processName) {
        Map<String, Object> map = new HashMap<>();
        map.put("projectCode", projectCode);
        String format = StrUtil.format(url + "/projects/{projectCode}/schedules", map);

        Map<String, Object> params = new HashMap<>();
        params.put("processDefinitionCode", processCode);
        params.put("searchVal", processName);

        String content = HttpRequest.get(format)
            .header(Constants.TOKEN, tokenKey)
            .form(params)
            .timeout(5000)
            .execute().body();

        PageInfo<JSONObject> data = MyJSONUtil.toPageBean(content);
        List<ScheduleVo> lists = new ArrayList<>();
        if (data == null || data.getTotalList() == null) {
            return lists;
        }

        for (JSONObject jsonObject : data.getTotalList()) {
            lists.add(MyJSONUtil.toBean(jsonObject, ScheduleVo.class));
        }
        return lists;
    }

    /**
     * 获取调度信息
     *
     * @param projectCode 项目编号
     * @param processCode 工作流编号
     * @param processName 工作流名称
     * @return {@link ScheduleVo}
     * @author 郑文豪
     * @date 2022/9/27 9:31
     */
    public ScheduleVo querySchedule(Long projectCode, Long processCode, String processName) {
        List<ScheduleVo> scheduleVos = querySchedules(projectCode, processCode, processName);
        for (ScheduleVo list : scheduleVos) {
            if (list.getProcessDefinitionName().equalsIgnoreCase(processName)) {
                return list;
            }
        }
        return null;
    }

    /**
     * 执行时间预览
     *
     * @param projectCode 项目编号
     * @param schedule    表达式
     * @return {@link List}
     * @author 郑文豪
     * @date 2022/9/28 10:50
     */
    public List<String> previewSchedule(Long projectCode, String schedule) {
        Map<String, Object> map = new HashMap<>();
        map.put("projectCode", projectCode);
        String format = StrUtil.format(url + "/projects/{projectCode}/schedules/preview", map);

        Map<String, Object> params = new HashMap<>();
        params.put("schedule", schedule);

        String content = HttpRequest.post(format)
            .header(Constants.TOKEN, tokenKey)
            .form(params)
            .timeout(5000)
            .execute().body();

        return MyJSONUtil.verifyResult(MyJSONUtil.toBean(content, new TypeReference<Result<List<String>>>() {
        }));
    }
}
