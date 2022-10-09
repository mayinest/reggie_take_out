package com.itheima.reggie.controller;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrderDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 历史订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page,int pageSize){

        Page<Orders> ordersPage = new Page<>(page, pageSize);
        Page<OrderDto> orderDtoPage = new Page<>();

        LambdaQueryWrapper<Orders> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(ordersPage, queryWrapper);
        BeanUtils.copyProperties(ordersPage,orderDtoPage,"records");
        List<Orders> records = ordersPage.getRecords();



        List<OrderDto> list = records.stream().map((item) -> {
            OrderDto orderDto = new OrderDto();
            BeanUtils.copyProperties(item,orderDto);
            Long id = item.getId();
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = Wrappers.lambdaQuery();
            queryWrapper1.eq(OrderDetail::getOrderId,id);
            int count = orderDetailService.count(queryWrapper1);
            orderDto.setSumNum(count);
            return orderDto;
        }).collect(Collectors.toList());
        orderDtoPage.setRecords(list);
        return R.success(orderDtoPage);
    }

    /**
     * 客户端订单明细
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number,String beginTime,String endTime){
        //构造分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        Page<OrderDto> ordersDtoPage=new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //根据number进行模糊查询
        //!StringUtils.isEmpty(number)判断number是否为空
        queryWrapper.like(!StringUtils.isEmpty(number),Orders::getNumber,number);
        //根据Datetime进行时间范围查询

        if(beginTime!=null&&endTime!=null){
            queryWrapper.ge(Orders::getOrderTime,beginTime);
            queryWrapper.le(Orders::getOrderTime,endTime);
        }
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //进行分页查询
        orderService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 派送
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> send(@RequestBody Orders orders){
        Long id = orders.getId();
        Integer status = orders.getStatus();
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getId,id);
        Orders one = orderService.getOne(queryWrapper);
        one.setStatus(status);
        orderService.updateById(one);
        return R.success("派送成功");
    }
    /**
     * 再来一单
     * 前端传过一个order的id，我可以通过order的id去查找order_detail表中的数据得到一个集合，
     * 然后遍历这个集合，取出值正好可以存到shopping_cart表中
     */
    //再来一单
    @Transactional
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders order1){
        LambdaQueryWrapper<OrderDetail> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(order1.getId()!=null,OrderDetail::getOrderId,order1.getId());
        List<OrderDetail> list = orderDetailService.list(queryWrapper);
        List<ShoppingCart> collect = list.stream().map((item) -> {
            ShoppingCart shoppingCart = new ShoppingCart();
           shoppingCart.setName(item.getName());
           shoppingCart.setImage(item.getImage());
           shoppingCart.setUserId(BaseContext.getCurrentId());
           if (item.getDishId() != null){
           shoppingCart.setDishId(item.getDishId());
               shoppingCart.setDishFlavor(item.getDishFlavor());}
           if (item.getSetmealId() != null){
           shoppingCart.setSetmealId(item.getSetmealId());}

           shoppingCart.setNumber(item.getNumber());
           shoppingCart.setAmount(item.getAmount());
           shoppingCart.setCreateTime(LocalDateTime.now());
           return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartService.saveBatch(collect);
        return R.success("再来一单");
    }



}
