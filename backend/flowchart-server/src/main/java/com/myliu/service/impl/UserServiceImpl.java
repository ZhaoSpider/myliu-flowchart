package com.myliu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myliu.dto.LoginRequest;
import com.myliu.dto.RegisterRequest;
import com.myliu.dto.UpdatePasswordRequest;
import com.myliu.dto.UpdateProfileRequest;
import com.myliu.entity.Role;
import com.myliu.entity.User;
import com.myliu.exception.BusinessException;
import com.myliu.mapper.RoleMapper;
import com.myliu.mapper.UserMapper;
import com.myliu.security.JwtTokenProvider;
import com.myliu.service.UserService;
import com.myliu.vo.LoginVO;
import com.myliu.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginVO login(LoginRequest request) {
        log.info("用户登录 | username: {}", request.getUsername());
        
        // 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );

        // 用户不存在
        if (user == null) {
            log.warn("登录失败，用户不存在 | username: {}", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        // 密码错误
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败，密码错误 | username: {}", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        // 账号被禁用
        if (user.getStatus() == null || user.getStatus() != 1) {
            log.warn("登录失败，账号被禁用 | username: {}, status: {}", request.getUsername(), user.getStatus());
            throw new BusinessException("账号已被禁用");
        }

        // 生成Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        // 构建用户信息
        UserVO userVO = convertToVO(user);
        
        log.info("登录成功 | userId: {}, username: {}", user.getId(), user.getUsername());

        return new LoginVO(token, userVO);
    }

    @Override
    public void register(RegisterRequest request) {
        // 验证两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        Long usernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        if (usernameCount > 0) {
            log.warn("注册失败，用户名已存在 | username: {}", request.getUsername());
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        Long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, request.getEmail())
        );
        if (emailCount > 0) {
            log.warn("注册失败，邮箱已被注册 | email: {}", request.getEmail());
            throw new BusinessException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setStatus(1);

        userMapper.insert(user);
        
        // 分配默认角色（普通用户）
        Role userRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, "USER")
        );
        if (userRole != null) {
            userMapper.insertUserRole(user.getId(), userRole.getId());
            log.info("分配默认角色成功 | userId: {}, roleId: {}", user.getId(), userRole.getId());
        }
        
        log.info("用户注册成功 | userId: {}, username: {}, email: {}", user.getId(), user.getUsername(), user.getEmail());
    }

    @Override
    public UserVO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新字段
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            // 检查邮箱是否被其他用户使用
            Long emailCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getEmail, request.getEmail())
                            .ne(User::getId, userId)
            );
            if (emailCount > 0) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        userMapper.updateById(user);
        return convertToVO(user);
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        // 验证两次密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public UserVO getUserByUsername(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }

    /**
     * 实体转VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        
        // 获取用户角色编码列表
        List<Role> roles = roleMapper.selectByUserId(user.getId());
        vo.setRoles(roles.stream().map(Role::getRoleCode).collect(Collectors.toList()));
        
        return vo;
    }
}
