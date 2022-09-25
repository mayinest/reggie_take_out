package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        log.info("page={},pageSize={}",page,pageSize);
        //分页模型
        Page pageInfo = new Page(page, pageSize);
//源码建议不要new
        LambdaQueryWrapper<Category> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.orderByAsc(Category::getType,Category::getSort);
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 保存员工
     * @param request
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Category category) {
        log.info("添加类型={}", category.getType());
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        categoryService.remove(ids);
        return R.success("删除成功");
    }

    /**
     * 修改
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息={}",category);
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    /**
     * 添加菜品分类下拉列表
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = Wrappers.lambdaQuery();
        //添加条件===》是套餐还是菜品
        queryWrapper.eq(category.getType() != null, Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }


}

