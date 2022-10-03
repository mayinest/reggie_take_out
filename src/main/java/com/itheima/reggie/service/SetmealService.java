package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveSetmealAndDish(SetmealDto setmealDto);

    SetmealDto getByIdWithDish(Long id);

    void updateWithDish(SetmealDto setmealDto);

    R<String> removeSetmealAndDish(List<Long> ids);

    R<String> stopSaleAndStartSale(int statusCode, List<Long> ids);
}
