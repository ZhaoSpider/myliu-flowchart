package com.myliu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myliu.dto.AdminCreateUserRequest;
import com.myliu.dto.AdminUpdateUserRequest;
import com.myliu.entity.FlowchartFile;
import com.myliu.entity.FlowchartVersion;
import com.myliu.entity.Role;
import com.myliu.entity.User;
import com.myliu.exception.BusinessException;
import com.myliu.mapper.FileMapper;
import com.myliu.mapper.RoleMapper;
import com.myliu.mapper.UserMapper;
import com.myliu.mapper.VersionMapper;
import com.myliu.service.AdminService;
import com.myliu.vo.AdminFileVO;
import com.myliu.vo.AdminUserVO;
import com.myliu.vo.PageVO;
import com.myliu.vo.SystemStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 管理员服务实现
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final FileMapper fileMapper;
    private final VersionMapper versionMapper;
    private final PasswordEncoder passwordEncoder;

    // 管理员角色编码
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    
    // 手机号正则（中国大陆）
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    // 邮箱正则
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @Override
    public PageVO<AdminUserVO> getUserList(Integer page, Integer size, String keyword, Integer status) {
        log.info("管理端获取用户列表 | page: {}, size: {}, keyword: {}, status: {}", page, size, keyword, status);
        
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreatedAt);
        
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        
        List<AdminUserVO> list = result.getRecords().stream()
                .map(this::convertToAdminUserVO)
                .collect(Collectors.toList());
        
        return PageVO.of(list, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public AdminUserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToAdminUserVO(user);
    }

    @Override
    public AdminUserVO createUser(AdminCreateUserRequest request) {
        log.info("管理员创建用户 | username: {}", request.getUsername());
        
        // 检查用户名是否存在
        Long usernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (usernameCount > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        if (StringUtils.hasText(request.getEmail())) {
            Long emailCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail())
            );
            if (emailCount > 0) {
                throw new BusinessException("邮箱已被使用");
            }
            // 验证邮箱格式
            if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
                throw new BusinessException("邮箱格式不正确");
            }
        }
        
        // 检查手机号是否存在
        if (StringUtils.hasText(request.getPhone())) {
            Long phoneCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone())
            );
            if (phoneCount > 0) {
                throw new BusinessException("手机号已被使用");
            }
            // 验证手机号格式
            if (!PHONE_PATTERN.matcher(request.getPhone()).matches()) {
                throw new BusinessException("手机号格式不正确，请输入11位有效手机号");
            }
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        
        userMapper.insert(user);
        log.info("用户创建成功 | userId: {}, username: {}", user.getId(), user.getUsername());
        
        // 分配角色
        if (request.getRoleId() != null) {
            assignRole(user.getId(), request.getRoleId());
        } else {
            // 默认分配普通用户角色
            Role userRole = roleMapper.selectOne(
                    new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, "USER")
            );
            if (userRole != null) {
                assignRole(user.getId(), userRole.getId());
            }
        }
        
        return convertToAdminUserVO(user);
    }

    @Override
    public AdminUserVO updateUser(Long id, AdminUpdateUserRequest request) {
        log.info("管理员更新用户 | userId: {}", id);
        
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            // 检查邮箱是否被其他用户使用
            Long emailCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getEmail, request.getEmail())
                            .ne(User::getId, id)
            );
            if (emailCount > 0) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
            // 验证邮箱格式
            if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
                throw new BusinessException("邮箱格式不正确");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            // 检查手机号是否被其他用户使用
            Long phoneCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getPhone, request.getPhone())
                            .ne(User::getId, id)
            );
            if (phoneCount > 0) {
                throw new BusinessException("手机号已被其他用户使用");
            }
            // 验证手机号格式
            if (StringUtils.hasText(request.getPhone()) && !PHONE_PATTERN.matcher(request.getPhone()).matches()) {
                throw new BusinessException("手机号格式不正确，请输入11位有效手机号");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        userMapper.updateById(user);
        
        // 更新角色
        if (request.getRoleId() != null) {
            // 删除旧角色
            userMapper.deleteUserRole(id);
            // 分配新角色
            assignRole(id, request.getRoleId());
        }
        
        log.info("用户更新成功 | userId: {}", id);
        return convertToAdminUserVO(user);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("管理员删除用户 | userId: {}", id);
        
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 删除用户角色关联
        userMapper.deleteUserRole(id);
        // 逻辑删除用户
        userMapper.deleteById(id);
        
        log.info("用户删除成功 | userId: {}, username: {}", id, user.getUsername());
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        log.info("管理员重置用户密码 | userId: {}", id);
        
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        
        log.info("密码重置成功 | userId: {}", id);
    }

    @Override
    public SystemStatsVO getSystemStats() {
        log.info("获取系统统计数据");
        
        SystemStatsVO stats = new SystemStatsVO();
        
        // 用户统计
        stats.setTotalUsers(userMapper.selectCount(null));
        stats.setTodayNewUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .ge(User::getCreatedAt, LocalDate.now().atStartOfDay())
        ));
        
        // 文件统计
        stats.setTotalFiles(fileMapper.selectCount(null));
        stats.setTodayNewFiles(fileMapper.selectCount(
                new LambdaQueryWrapper<FlowchartFile>()
                        .ge(FlowchartFile::getCreatedAt, LocalDate.now().atStartOfDay())
        ));
        
        // 版本统计
        stats.setTotalVersions(versionMapper.selectCount(null));
        
        // JVM信息
        Runtime runtime = Runtime.getRuntime();
        stats.setMaxMemory(runtime.maxMemory() / 1024 / 1024); // MB
        stats.setUsedMemory((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024); // MB
        stats.setFreeMemory(runtime.freeMemory() / 1024 / 1024); // MB
        
        // 运行时间
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        stats.setUptime(runtimeMXBean.getUptime());
        
        return stats;
    }

    @Override
    public boolean isAdmin(Long userId) {
        List<Role> roles = roleMapper.selectByUserId(userId);
        return roles.stream().anyMatch(role -> 
                ADMIN_ROLE.equals(role.getRoleCode()) || SUPER_ADMIN_ROLE.equals(role.getRoleCode())
        );
    }

    // ==================== 文件管理 ====================

    @Override
    public PageVO<AdminFileVO> getFileList(Integer page, Integer size, String keyword, Integer status, Long creatorId) {
        log.info("管理端获取文件列表 | page: {}, size: {}, keyword: {}, status: {}, creatorId: {}", page, size, keyword, status, creatorId);
        
        Page<FlowchartFile> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<FlowchartFile> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(FlowchartFile::getFileName, keyword);
        }
        
        if (status != null) {
            wrapper.eq(FlowchartFile::getStatus, status);
        }
        
        if (creatorId != null) {
            wrapper.eq(FlowchartFile::getCreatorId, creatorId);
        }
        
        wrapper.orderByDesc(FlowchartFile::getCreatedAt);
        
        Page<FlowchartFile> result = fileMapper.selectPage(pageParam, wrapper);
        
        List<AdminFileVO> list = result.getRecords().stream()
                .map(this::convertToAdminFileVO)
                .collect(Collectors.toList());
        
        return PageVO.of(list, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public void deleteFile(Long id) {
        log.info("管理员删除文件 | fileId: {}", id);
        
        FlowchartFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 删除文件的所有版本
        versionMapper.delete(
                new LambdaQueryWrapper<FlowchartVersion>()
                        .eq(FlowchartVersion::getFileId, id)
        );
        
        // 逻辑删除文件
        fileMapper.deleteById(id);
        
        log.info("文件删除成功 | fileId: {}, fileName: {}", id, file.getFileName());
    }

    /**
     * 分配角色
     */
    private void assignRole(Long userId, Long roleId) {
        userMapper.insertUserRole(userId, roleId);
    }

    /**
     * 转换为AdminUserVO
     */
    private AdminUserVO convertToAdminUserVO(User user) {
        AdminUserVO vo = new AdminUserVO();
        BeanUtils.copyProperties(user, vo);
        
        // 获取用户角色
        List<Role> roles = roleMapper.selectByUserId(user.getId());
        vo.setRoles(roles.stream().map(Role::getRoleName).collect(Collectors.toList()));
        vo.setRoleIds(roles.stream().map(Role::getId).collect(Collectors.toList()));
        
        return vo;
    }

    /**
     * 转换为AdminFileVO
     */
    private AdminFileVO convertToAdminFileVO(FlowchartFile file) {
        AdminFileVO vo = new AdminFileVO();
        BeanUtils.copyProperties(file, vo);
        
        // 获取创建者信息（包括已删除的用户）
        if (file.getCreatorId() != null) {
            User creator = userMapper.selectByIdIncludeDeleted(file.getCreatorId());
            if (creator != null) {
                vo.setCreatorName(creator.getUsername());
                vo.setCreatorNickname(creator.getNickname());
            }
        }
        
        return vo;
    }
}
