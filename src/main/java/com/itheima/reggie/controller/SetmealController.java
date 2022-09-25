package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page pageInfo = new Page();
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = Wrappers.lambdaQuery();
        setmealLambdaQueryWrapper.like(name != null,Setmeal::getName,name);
        setmealService.page(pageInfo,setmealLambdaQueryWrapper);
        return R.success(pageInfo);

    }



}
