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

package com.dlink.scheduler.utils;

import com.dlink.scheduler.model.DlinkTaskParams;
import com.dlink.scheduler.model.SubProcessTaskParams;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import cn.hutool.json.JSONUtil;

/**
 * @author 郑文豪
 */
public class ParamUtil {

    /**
     * 封装分页查询
     *
     * @return {@link Map}
     * @author 郑文豪
     * @date 2022/9/7 16:57
     */
    public static Map<String, Object> getPageParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", Integer.MAX_VALUE);
        return params;
    }

    /**
     * 封装分页查询
     *
     * @param name 查询条件
     * @return {@link Map}
     * @author 郑文豪
     * @date 2022/9/7 16:58
     */
    public static Map<String, Object> getPageParams(String name) {
        Map<String, Object> params = getPageParams();
        params.put("searchVal", name);
        return params;
    }

    /**
     * 实体转map
     *
     * @param object 实体对象
     * @return {@link Map}
     * @author 郑文豪
     * @date 2022/9/26 15:33
     */
    public static Map<String, Object> objectToMap(Object object) throws IllegalAccessException {
        Map<String, Object> dataMap = new HashMap<>();
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            dataMap.put(field.getName(), field.get(object));
        }
        return dataMap;
    }

    public static String getDlinkTaskParams(String url, Integer taskId) {
        DlinkTaskParams taskParams = new DlinkTaskParams();
        taskParams.setAddress(url);
        taskParams.setTaskId(taskId.toString());
        return JSONUtil.toJsonStr(taskParams);
    }

    public static String getSubProcessParams(Long processCode) {
        SubProcessTaskParams taskParams = new SubProcessTaskParams();
        taskParams.setProcessDefinitionCode(processCode);
        return JSONUtil.toJsonStr(taskParams);
    }
}
