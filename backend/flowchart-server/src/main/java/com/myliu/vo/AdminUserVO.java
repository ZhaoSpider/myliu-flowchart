package com.myliu.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端用户信息VO
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
public class AdminUserVO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;
}
