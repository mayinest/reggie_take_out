package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 菜品的分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        //分页模型
        Page<Dish> pageInfo = new Page(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     * 添加菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("添加成功");
    }

    /**
     * 更新的获取数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getUpdate(@PathVariable  Long id) {
       DishDto dishDto =  dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody  DishDto dishDto){
        log.info(dishDto.getFlavors().toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){
        log.info(ids.toString());
        List<Long> longs = Arrays.asList(ids);
        dishService.removeByIds(longs);
        return R.success("删除成功");
    }
    @PostMapping("/status/{StatusCode}")
    public R<String> StopSaleAndStartSale(@PathVariable int StatusCode,Long[] ids){
        ArrayList<Dish> dishArrayList = new ArrayList<>();

        for (int i =0; i< ids.length; i++){
            Dish dish = new Dish();
            dish.setId(ids[i]);
            dish.setStatus(StatusCode);
            dishArrayList.add(dish);
        }
        if(StatusCode == 0){
            dishService.updateBatchById(dishArrayList,ids.length);
            return R.success("停售成功");
        }
        if(StatusCode == 1){
            dishService.updateBatchById(dishArrayList,ids.length);
            return R.success("起售成功");
        }
        return R.success("未知错误,请刷新");
    }
}
