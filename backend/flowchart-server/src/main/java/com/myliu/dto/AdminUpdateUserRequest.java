package com.myliu.dto;

import lombok.Data;

/**
 * 管理员更新用户请求
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
public class AdminUpdateUserRequest {

    private String nickname;

    private String email;

    private String phone;

    private Integer status;

    private Long roleId;
}
