package com.myliu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人信息请求
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
public class UpdateProfileRequest {

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
