/*
 *  海豚 设置执行计划 (未完成)
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

import type {FormInstance} from 'antd/es/form';
import {Button, Form, InputNumber, message, Select, Switch, Checkbox, Row, Col, DatePicker, Input} from "antd";
import {StateType} from "@/pages/DataStudio/model";
import {connect} from "umi";
import React, {useState, useEffect} from "react";
import {
  createTaskDefinition, getScheduleByCatalogueId,
  updateTaskDefinition
} from "@/pages/DataStudio/service";
import {CODE} from "@/components/Common/crud";
import TextArea from "antd/es/input/TextArea";
import moment from "moment";

const DolphinRunScheduler = (props: any) => {
  const {data, handleDolphinModalVisible} = props;
  const {RangePicker} = DatePicker;
  const formRef = React.createRef<FormInstance>();
  const [processCode, setProcessCode] = useState("");
  const CheckboxGroup = Checkbox.Group;

  const dateFormat = 'YYYY-MM-DD HH:mm:ss';

  const layout = {
    labelCol: {span: 6},
    wrapperCol: {span: 18},
  };
  useEffect(() => {
    setFormValue();
  }, [data])

  //赋值数据
  const setFormValue = () => {
    //是否已有数据
    if (data) {
      // debugger

      let result = getScheduleByCatalogueId(data)

      result.then(result => {
        if (result.code == CODE.SUCCESS) {

          let SEvalue = [moment(result.scheduleRequest.startTime, dateFormat), moment(result.scheduleRequest.endTime, dateFormat)]

          formRef.current.setFieldsValue({
            SR_SET: SEvalue,
            SR_crontab: result.scheduleRequest?.crontab,
            SR_timezoneId: result.scheduleRequest?.timezoneId,
            environmentCode: result.environmentCode,
            failureStrategy: result.failureStrategy,
            processInstancePriority: result.processInstancePriority,
            scheduleId: result.scheduleId,
            warningGroupId: result.warningGroupId,
            warningType: result.warningType,
            workerGroup: result.workerGroup
          });
        } else {
          message.error(result.msg);
        }
      })

      setProcessCode(data.processDefinitionCode)

      setTimeoutFlagHidden(data.timeoutFlag === 'OPEN');

      let tns = []
      if (data.timeoutNotifyStrategy === "WARNFAILED") {
        tns = ['WARN', 'FAILED']
      } else {
        // debugger
        tns = data.timeoutNotifyStrategy ? data.timeoutNotifyStrategy.split(',') : []
      }
      // debugger
      //前置任务勾选
      let upstreamCodesTem = [];
      for (let key in data.upstreamTaskMap) {
        upstreamCodesTem.push(parseInt(key))
      }

      formRef.current.setFieldsValue({
        upstreamCodes: upstreamCodesTem,
        taskPriority: data.taskPriority,
        failRetryTimes: data.failRetryTimes,
        description: data.description,
        failRetryInterval: data.failRetryInterval,
        delayTime: data.delayTime,
        timeout: data.timeout,
        flag: data.flag === 'YES',
        timeoutFlag: data.timeoutFlag === 'OPEN',
        timeoutNotifyStrategy: tns
      });

    }
  };

  //表单提交，添加/更新海豚任务
  const onFinish = (values: any) => {
    // console.log(values);
    values.flag === true ? values.flag = 'YES' : values.flag = 'NO';
    values.upstreamCodes ? values.upstreamCodes = values.upstreamCodes.toString() : "";
    values.processCode = processCode;

    if (values.timeoutFlag === false) {
      values.timeoutFlag = 'CLOSE'
      values.timeoutNotifyStrategy = null;
      values.timeout = 0;
    } else {
      values.timeoutFlag = 'OPEN'
      values.timeout = 1;
      if (values.timeoutNotifyStrategy && values.timeoutNotifyStrategy.length > 1) {
        values.timeoutNotifyStrategy = "WARNFAILED";
      } else if (values.timeoutNotifyStrategy && values.timeoutNotifyStrategy.length === 1) {
        values.timeoutNotifyStrategy = values.timeoutNotifyStrategy[0]
      } else {
        message.error(`超时策略必须选一个`);
        return
      }
    }

    if (!data) {
      const res = createTaskDefinition(data, values.upstreamCodes, values);
      res.then((result) => {
        if (result.code == CODE.SUCCESS) {
          handleDolphinModalVisible(false);
        } else {
          message.error(`创建任务失败，原因：\n${result.msg}`);
        }
      })
    } else {
      const res = updateTaskDefinition(data.processDefinitionCode, data.projectCode, data.code, values.upstreamCodes, values);
      res.then((result) => {
        if (result.code == CODE.SUCCESS) {
          handleDolphinModalVisible(false);
        } else {
          message.error(`创建任务失败，原因：\n${result.msg}`);
        }
      })
    }
  };

  const [timeoutFlagHidden, setTimeoutFlagHidden] = useState<boolean>(false);

  function onSwitchChange(checked: boolean) {
    setTimeoutFlagHidden(checked);
    formRef.current.setFieldsValue({
      timeout: 1
    });
  }

  return (
    <Form {...layout} ref={formRef} name="control-hooks" onFinish={onFinish}>
      <Form.Item name={['upstreamCodes']} style={{marginBottom: 10}} label="起止时间">
        <RangePicker showTime/>
      </Form.Item>

      <Form.Item name={['SR_crontab']} style={{marginBottom: 10}} label="定时">
        <Input/>
      </Form.Item>

      <Form.Item name={['SR_timezoneId']} style={{marginBottom: 10}} label="失败重试次数">
        <InputNumber min={0} max={99} style={{width: 180}}/>
      </Form.Item>
      <Form.Item name={['failureStrategy']} style={{marginBottom: 10}} label="失败策略">
        <Radio value={'CONTINUE'}>继续</Radio>
        <Radio value={'END'}>结束</Radio>
      </Form.Item>
      <Form.Item name={['processInstancePriority']} style={{marginBottom: 10}} label="通知策略">
        <InputNumber min={0} style={{width: 180}}/>
      </Form.Item>
      <Form.Item name={['timeoutFlag']} style={{marginBottom: 10}} label="超时告警" valuePropName="checked">
        <Switch checkedChildren="OPEN" unCheckedChildren="CLOSE" onChange={onSwitchChange}/>
      </Form.Item>
      <Form.Item name={['timeoutNotifyStrategy']} style={{marginBottom: 10}} hidden={!timeoutFlagHidden} label="超时策略">
        <CheckboxGroup>
          <Row>
            <Col span={12}>
              <Checkbox value="WARN">超时警告</Checkbox>
            </Col>
            <Col span={12}>
              <Checkbox value="FAILED">超时失败</Checkbox>
            </Col>
          </Row>
        </CheckboxGroup>
      </Form.Item>
      <Form.Item name={['timeout']} style={{marginBottom: 10}} hidden={!timeoutFlagHidden} label="超时告警时长(分钟)">
        <InputNumber min={1} value={30} style={{width: 180}}/>
      </Form.Item>
      <Form.Item name={['flag']} style={{marginBottom: 10}} label="运行标志" valuePropName="checked">
        <Switch checkedChildren="YES" unCheckedChildren="NO"/>
      </Form.Item>
      <Form.Item name={['description']} style={{marginBottom: 10}} label="备注">
        <TextArea rows={3} placeholder="备注信息" maxLength={250}/>
      </Form.Item>
      <Form.Item wrapperCol={{offset: 8, span: 16}}>
        <Button type="primary" htmlType="submit">
          保存
        </Button>
      </Form.Item>
    </Form>
  );
}


export default connect(({Studio}: { Studio: StateType }) => ({
  current: Studio.current,
  currentSession: Studio.currentSession,
}))(DolphinRunScheduler);
