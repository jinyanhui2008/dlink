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


import request from 'umi-request';
import {CAParam, StudioMetaStoreParam, StudioParam} from "@/components/Studio/StudioEdit/data";

export async function executeSql(params: StudioParam) {
  return request<API.Result>('/api/studio/executeSql', {
    method: 'POST',
    data: {
      ...params,
    },
  });
}

export async function executeDDL(params: StudioParam) {
  return request<API.Result>('/api/studio/executeDDL', {
    method: 'POST',
    data: {
      ...params,
    },
  });
}

export async function explainSql(params: StudioParam) {
  return request<API.Result>('/api/studio/explainSql', {
    method: 'POST',
    data: {
      ...params,
    },
  });
}

export async function getStreamGraph(params: StudioParam) {
  return request<API.Result>('/api/studio/getStreamGraph', {
    method: 'POST',
    data: {
      ...params,
    },
  });
}

export async function getJobPlan(params: StudioParam) {
  return request<API.Result>('/api/studio/getJobPlan', {
    method: 'POST',
    data: {
      ...params,
    },
  });
}

export async function getJobData(jobId: string) {
  return request<API.Result>('/api/studio/getJobData', {
    method: 'GET',
    params: {
      jobId,
    },
  });
}

export async function getCatalogueTreeData(params?: StudioParam) {
  return request<API.Result>('/api/catalogue/getCatalogueTreeData', {
    method: 'POST',
    data: {
      ...params,
    },
  });
}

export async function getLineage(params: CAParam) {
  return request<API.Result>('/api/studio/getLineage', {
    method: 'POST',
    data: {
      ...params,
    },
  });
}

export async function getMSCatalogs(params: StudioMetaStoreParam) {
  return request<API.Result>('/api/studio/getMSCatalogs', {
    method: 'POST',
    data: {
      ...params,
    },
  });
}

export async function getMSSchemaInfo(params: StudioMetaStoreParam) {
  return request<API.Result>('/api/studio/getMSSchemaInfo', {
    method: 'POST',
    data: {
      ...params,
    },
  });
}

//dolphinscheduler -- 是否允许调度海豚 -- whether dolphinscheduler dispatching is allowed
export async function isUsingDS() {
  return request<API.Result>('/api/scheduler', {
    method: 'GET'
  });
}

//dolphinscheduler -- 获取任务定义 -- get task definition
export async function getTaskDefinition(dinkyTaskId: string) {
  return request<API.Result>('/api/scheduler/task', {
    method: 'GET',
    params: {
      dinkyTaskId,
    },
  });
}

//dolphinscheduler -- 获取任务定义集合 -- get task definition collection
export async function getTaskMainInfos(dinkyTaskId: string) {
  return request<API.Result>('/api/scheduler/upstream/tasks', {
    method: 'GET',
    params: {
      dinkyTaskId,
    },
  });
}

//dolphinscheduler -- 创建任务定义 -- create task definition
export async function createTaskDefinition(dinkyTaskId: string, upstreamCodes: string, params: object) {
  return request<API.Result>('/api/scheduler/task', {
    method: 'POST',
    params: {
      dinkyTaskId,
      upstreamCodes
    },
    data: {
      ...params,
    },
  });
}

//dolphinscheduler -- 更新任务定义 -- update task definition
export async function updateTaskDefinition(processCode: string, projectCode: string, taskCode: string, upstreamCodes: string, params: object) {
  return request<API.Result>('/api/scheduler/task', {
    method: 'PUT',
    params: {
      processCode,
      projectCode,
      taskCode,
      upstreamCodes
    },
    data: {
      ...params,
    },
  });
}

//dolphinscheduler -- 工作流上线下线 -- Workflow online and offline
export async function postDolphinCatalogueRelease(catalogueId: string, releaseState: string) {
  return request<API.Result>('/api/scheduler/' + catalogueId + '/release', {
    method: 'POST',
    params: {
      releaseState
    }
  });
}

//dolphinscheduler -- 获取定时任务 -- Get scheduled tasks
export async function getScheduleByCatalogueId(catalogueId: string) {
  return request<API.Result>('/api/scheduler/' + catalogueId + '/schedule', {
    method: 'GET'
  });
}

//dolphinscheduler -- 添加或修改定时调度 -- Add or modify a scheduled schedule
export async function postScheduleByCatalogueId(catalogueId: string) {
  return request<API.Result>('/api/scheduler/' + catalogueId + '/schedule', {
    method: 'POST'
  });
}

//dolphinscheduler -- 执行工作流 -- Execute Workflow
export async function postStartProcessByCatalogueId(catalogueId: string) {
  return request<API.Result>('/api/scheduler/' + catalogueId + '/start-process', {
    method: 'POST'
  });
}

//dolphinscheduler -- 查看工作流日志 -- Execute Workflow
export async function getInstanceByCatalogueId(catalogueId: string) {
  return request<API.Result>('/api/scheduler/' + catalogueId + '/instance', {
    method: 'GET'
  });
}
