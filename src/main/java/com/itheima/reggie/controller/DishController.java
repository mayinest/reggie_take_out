package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page={}，pageSiz={}",page,pageSize);
        //分页模型
        Page pageInfo = new Page(page, pageSize);

        LambdaQueryWrapper<Dish> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(name != null,Dish::getName,name);
        dishService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

}
