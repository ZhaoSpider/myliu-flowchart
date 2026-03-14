package com.myliu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户信息
     */
    private UserVO user;
}
