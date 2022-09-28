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
import com.dlink.scheduler.enums.CommandType;
import com.dlink.scheduler.enums.ComplementDependentMode;
import com.dlink.scheduler.enums.FailureStrategy;
import com.dlink.scheduler.enums.Priority;
import com.dlink.scheduler.enums.RunMode;
import com.dlink.scheduler.enums.TaskDependType;
import com.dlink.scheduler.enums.WarningType;
import com.dlink.scheduler.result.Result;
import com.dlink.scheduler.utils.MyJSONUtil;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;

/**
 * @author 郑文豪
 */
@Component
public class ExecutorClient {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorClient.class);

    @Value("${dinky.dolphinscheduler.url}")
    private String url;
    @Value("${dinky.dolphinscheduler.token}")
    private String tokenKey;

    /**
     * 执行工作流
     *
     * @param projectCode               描述
     * @param processCode               描述
     * @param scheduleTime              描述
     * @param failureStrategy           描述
     * @param startNodeList             描述
     * @param taskDependType            描述
     * @param execType                  描述
     * @param warningType               描述
     * @param warningGroupId            描述
     * @param runMode                   描述
     * @param processInstancePriority   描述
     * @param workerGroup               描述
     * @param environmentCode           描述
     * @param timeout                   描述
     * @param startParams               描述
     * @param expectedParallelismNumber 描述
     * @param dryRun                    描述
     * @param complementDependentMode   描述
     * @author 郑文豪
     * @date 2022/9/26 15:21
     */
    public void startProcessInstance(long projectCode, long processCode, String scheduleTime, FailureStrategy failureStrategy,
                                     String startNodeList, TaskDependType taskDependType, CommandType execType, WarningType warningType,
                                     Integer warningGroupId, RunMode runMode, Priority processInstancePriority, String workerGroup,
                                     Long environmentCode, Integer timeout, String startParams, Integer expectedParallelismNumber,
                                     int dryRun, ComplementDependentMode complementDependentMode) {

        Map<String, Object> map = new HashMap<>();
        map.put("projectCode", projectCode);
        String format = StrUtil.format(url + "/projects/{projectCode}/executors/start-process-instance", map);

        Map<String, Object> params = new HashMap<>();
        params.put("processDefinitionCode", processCode);
        params.put("scheduleTime", scheduleTime);
        params.put("failureStrategy", failureStrategy);
        params.put("startNodeList", startNodeList);
        params.put("taskDependType", taskDependType);
        params.put("execType", execType);
        params.put("warningType", warningType);
        params.put("warningGroupId", warningGroupId);
        params.put("runMode", runMode);
        params.put("processInstancePriority", processInstancePriority);
        params.put("workerGroup", workerGroup);
        params.put("environmentCode", environmentCode);
        params.put("timeout", timeout);
        params.put("startParams", startParams);
        params.put("expectedParallelismNumber", expectedParallelismNumber);
        params.put("dryRun", dryRun);
        params.put("complementDependentMode", complementDependentMode);

        String content = HttpRequest.post(format)
            .header(Constants.TOKEN, tokenKey)
            .form(params)
            .timeout(5000)
            .execute().body();

        MyJSONUtil.verifyResult(MyJSONUtil.toBean(content, Result.class));
    }

}
