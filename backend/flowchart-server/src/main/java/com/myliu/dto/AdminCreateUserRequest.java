package com.myliu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员创建用户请求
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
public class AdminCreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String nickname;

    private String email;

    private String phone;

    private Integer status;

    private Long roleId;
}
