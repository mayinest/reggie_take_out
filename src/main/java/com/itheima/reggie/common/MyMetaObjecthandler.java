package com.itheima.reggie.common;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义元数据对象处理器
 * 公共字段填充
 * 1.要进行公共填写字段的实体类的属性上加入=====》@TableField(fill = FieldFill.INSERT)//插入时填充字段
 *                                  =====》@TableField(fill = FieldFill.INSERT_UPDATE)//插入和更新的时候填充字段
 * 2.编写元数据处理对象（该类）,实现 MetaObjectHandler接口
 */
@Component
@Slf4j
public class MyMetaObjecthandler implements MetaObjectHandler {
//@TableField(fill = FieldFill.INSERT)//插入时填充字段
// 走这个方法
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("createUser",BaseContext.getCurrentId());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }
//@TableField(fill = FieldFill.INSERT_UPDATE)//插入和更新的时候填充字段
// 走这个方法
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("公共字段自动填充[update]");
        log.info(metaObject.toString());
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",BaseContext.getCurrentId());
    }
}
