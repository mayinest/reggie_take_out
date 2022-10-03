package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {


    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 添加套餐
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveSetmealAndDish(SetmealDto setmealDto) {
//        public interface SetmealService extends IService<Setmeal>
        /**
         * 这里父类的IService类型是Setmeal，所以this。save是保存的setmeal这个表的信息
         * setmealDto继承了Setmeal
         */
        this.save(setmealDto);
        /**
         * 1.这里取出SetmealDto中的setmealDish类型的集合
         * 2.然后通过遍历的方式给setmealDish类型的list集合添加setmeal_id
         * 3.最后吧setmealDishService注入，再通过批量保存saveBatch
         */
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes = setmealDishes.stream().map((item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        })).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 更新的时候获取数据
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        /**
         * 首先我要返回SetmealDto对象，这个对象涉及两个表，其中该方法父类中是对Setmeal的直接操作，
         * SetmealDishService是对setmeal_dish表的操作
         *
         *
         * 1.第一步直接通过id获取到setmeal的对象数据，并copy给setmealDto
         * 2.第二步通过queryWrapper添加条件--->setmeal_dish表中setmeal_id等于setmeal的id中的数据，也copy给SetmealDto对象
         */
        //1.
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        //2.
        LambdaQueryWrapper<SetmealDish> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(setmeal.getId() != null,SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    /**
     * 修改套餐
     * @param setmealDto
     */
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        /**
         *首先我去更新套餐一样涉及两个表，Setmeal可以直接修改，而setmeal_dish表有多个数据，
         * 且只有一个setmeal_id关联，不好修改，所以采用直接删除重新添加
         *
         *
         * 1.首先用父类方法保存基本数据--setmeal表
         * 2.通过setmeal的id把匹配的setmeal_id全部删除
         * 3.再遍历setmealDato中的Dish存到SetmealDish中重新保存
         */
        //1.
        this.updateById(setmealDto);
        //2.
        LambdaQueryWrapper<SetmealDish> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //3.
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes = setmealDishes.stream().map((item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        })).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @Override
    public R<String> removeSetmealAndDish(List<Long> ids) {
        /**
         * 逻辑和菜品删除一样
         */
        LambdaQueryWrapper<Setmeal> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        if(count > 0){
            return R.error("套餐还在销售，不能删除");
        }
        this.removeByIds(ids);
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = Wrappers.lambdaQuery();
        queryWrapper1.in(SetmealDish::getDishId,ids);
        setmealDishService.remove(queryWrapper1);
        return R.success("删除成功");
    }

    @Override
    public R<String> stopSaleAndStartSale(int statusCode, List<Long> ids) {
        ArrayList<Setmeal> setmealArrayList = new ArrayList<>();

        for (int i =0; i < ids.size(); i++){
            Setmeal setmeal = new Setmeal();
            setmeal.setId(ids.get(i));
            setmeal.setStatus(statusCode);
            setmealArrayList.add(setmeal);
        }
        if(statusCode == 0){
          this.updateBatchById(setmealArrayList, ids.size());
            return R.success("停售成功");
        }
        if(statusCode == 1){
          this.updateBatchById(setmealArrayList,ids.size());
            return R.success("起售成功");
        }
        return R.success("未知错误,请刷新");
    }
}
