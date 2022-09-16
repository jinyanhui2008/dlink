package com.dlink.scheduler.utils;

import com.dlink.scheduler.exception.SchedulerException;
import com.dlink.scheduler.result.PageInfo;
import com.dlink.scheduler.result.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * @author 郑文豪
 */
public class MyJSONUtil {

    private static final Logger logger = LoggerFactory.getLogger(MyJSONUtil.class);

    public static <T> T toBean(String content, TypeReference<T> typeReference) {
        try {
            return JSONUtil.toBean(content, typeReference, true);
        } catch (Exception e) {
            logger.error("json转换异常 json:{},异常信息:{}", content, e.getMessage(), e);
            throw new SchedulerException("数据转换异常");
        }
    }

    public static <T> T toBean(JSONObject content, Class<T> beanClass) {
        try {
            return JSONUtil.toBean(content, beanClass);
        } catch (Exception e) {
            logger.error("json转换异常 json:{},异常信息:{}", content, e.getMessage(), e);
            throw new SchedulerException("数据转换异常");
        }
    }

    public static <T> T toBean(String content, Class<T> beanClass) {
        try {
            return JSONUtil.toBean(content, beanClass);
        } catch (Exception e) {
            logger.error("json转换异常 json:{},异常信息:{}", content, e.getMessage(), e);
            throw new SchedulerException("数据转换异常");
        }
    }

    public static <T> T verifyResult(Result<T> result) {
        if (result.getFailed()) {
            throw new SchedulerException(result.getMsg());
        }
        return result.getData();
    }

    /**
     * json字符串转分页对象
     *
     * @param content json字符串
     * @return {@link PageInfo}
     * @author 郑文豪
     * @date 2022/9/8 9:29
     */
    public static PageInfo<JSONObject> toPageBean(String content) {
        return verifyResult(MyJSONUtil.toBean(content,
            new TypeReference<Result<PageInfo<JSONObject>>>() {
            }));
    }

    /**
     * json字符串转分页对象,根据名称精确查找
     *
     * @param content   json字符串
     * @param name      名称
     * @param beanClass 要转换的class
     * @return {@link T}
     * @author 郑文豪
     * @date 2022/9/8 9:27
     */
    public static <T> T toPageBeanAndFindByName(String content, String name, Class<T> beanClass) {
        PageInfo<JSONObject> data = toPageBean(content);
        if (data == null || data.getTotalList() == null) {
            return null;
        }

        for (JSONObject jsonObject : data.getTotalList()) {
            if (name.equalsIgnoreCase(jsonObject.getStr("name"))) {
                return toBean(jsonObject, beanClass);
            }
        }
        return null;
    }
}
