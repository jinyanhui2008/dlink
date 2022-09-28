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

import com.dlink.scheduler.enums.ResourceType;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Resource {

    private int id;

    @ApiModelProperty(value = "父id")
    private int pid;

    @ApiModelProperty(value = "资源的别名")
    private String alias;

    @ApiModelProperty(value = "全名")
    private String fullName;

    @ApiModelProperty(value = "是否是目录")
    private boolean isDirectory = false;

    @ApiModelProperty(value = "备注")
    private String description;

    @ApiModelProperty(value = "文件名")
    private String fileName;

    @ApiModelProperty(value = "用户id")
    private int userId;

    @ApiModelProperty(value = "资源类型")
    private ResourceType type;

    @ApiModelProperty(value = "资源大小")
    private long size;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "用户名")
    private String userName;

}
