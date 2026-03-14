package com.myliu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myliu.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
