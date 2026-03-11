package com.myliu.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;

/**
 * 用户主体信息
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Principal {

    private Long userId;
    private String username;

    @Override
    public String getName() {
        return username;
    }
}
