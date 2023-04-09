package com.wsj.xunyou.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wsj.xunyou.mapper.UserMapper;
import com.wsj.xunyou.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class getTotalItems{
    @Resource
    private UserMapper userMapper;
    @Test
    void test(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        System.out.println(userMapper.selectCount(queryWrapper));
    }
}
