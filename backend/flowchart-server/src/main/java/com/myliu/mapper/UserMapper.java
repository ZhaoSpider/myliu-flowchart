package com.myliu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myliu.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问层
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 插入用户角色关联
     */
    @Insert("INSERT INTO sys_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    void insertUserRole(Long userId, Long roleId);

    /**
     * 删除用户角色关联
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    void deleteUserRole(Long userId);

    /**
     * 根据ID查询用户（包括已删除的用户，用于显示创建者信息）
     */
    @Select("SELECT id, username, nickname, email, phone, avatar, status, created_at, updated_at, deleted FROM sys_user WHERE id = #{id}")
    User selectByIdIncludeDeleted(Long id);
}
