package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;


    /**
     * 新建菜品
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {

        this.save(dishDto);

        Long dishId = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 修改的获取数据
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(dish.getId() != null,DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    /**
     * 修改菜品
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {

        this.updateById(dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);

        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());


        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public R<String> removeByIdsAndFlavor(List<Long> ids) {
        /**
         * 删除涉及了两张表，dish和dish_flavor表
         * 1.首选判断是否能删除，停售了才能删除，不停售则不能删除--->这里黑马用的思路是queryWrapper添加条件去判断有几条
         * 2.如果能删除则先删除dish表，然后再通过dish表中的id去删除dish_flavor表中匹配的dish_id
         这里我觉得需要加个事务，同时成功和同时失败
         */
        //1.
        LambdaQueryWrapper<Dish> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(Dish::getId,ids);//这里的理解是后面集合用in，单个值用eq----暂定
        queryWrapper.eq(Dish::getStatus,1);
        int count = this.count(queryWrapper);
        if(count > 0){
            return R.error("该菜品还在销售，不能删除");
        }
        //2.
        this.removeByIds(ids);
        LambdaQueryWrapper<DishFlavor> queryWrapper1 = Wrappers.lambdaQuery();
        queryWrapper1.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper1);
        return R.success("删除成功");
    }
}
