-- =====================================================
-- MyLiu Flowchart 数据库初始化脚本
-- 数据库名: myliu_flowchart
-- 创建时间: 2024
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `myliu_flowchart` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `myliu_flowchart`;

-- =====================================================
-- 用户相关表
-- =====================================================

-- 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`),
    KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `type` TINYINT DEFAULT 1 COMMENT '类型：1菜单，2按钮，3接口',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父级ID',
    `path` VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
    `component` VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =====================================================
-- 流程图相关表
-- =====================================================

-- 流程图文件表
DROP TABLE IF EXISTS `flowchart_file`;
CREATE TABLE `flowchart_file` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    `thumbnail` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
    `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
    `project_id` BIGINT DEFAULT NULL COMMENT '所属项目ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1草稿，2已发布',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程图文件表';

-- 流程图版本表
DROP TABLE IF EXISTS `flowchart_version`;
CREATE TABLE `flowchart_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_id` BIGINT NOT NULL COMMENT '文件ID',
    `version_no` VARCHAR(20) NOT NULL COMMENT '版本号',
    `content` LONGTEXT COMMENT '流程图JSON数据',
    `change_log` TEXT COMMENT '变更说明',
    `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_file_version` (`file_id`, `version_no`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程图版本表';

-- =====================================================
-- AI协作预留表（暂不实现）
-- =====================================================

-- AI协作记录表
DROP TABLE IF EXISTS `ai_collaboration`;
CREATE TABLE `ai_collaboration` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_id` BIGINT NOT NULL COMMENT '文件ID',
    `session_id` VARCHAR(100) DEFAULT NULL COMMENT '会话ID',
    `prompt` TEXT COMMENT '用户提示',
    `result` TEXT COMMENT 'AI返回结果',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1处理中，2成功，3失败',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_file_id` (`file_id`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI协作记录表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入默认角色
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`, `sort`) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1),
('管理员', 'ADMIN', '拥有管理权限', 2),
('普通用户', 'USER', '普通用户权限', 3);

-- 插入默认管理员用户
-- 密码: admin123 (BCrypt加密后的值)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '超级管理员', 'admin@myliu.com', 1);

-- 关联管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 插入默认权限
INSERT INTO `sys_permission` (`permission_name`, `permission_code`, `type`, `parent_id`, `path`, `icon`, `sort`) VALUES
-- 菜单权限
('系统管理', 'system', 1, 0, '/system', 'Setting', 1),
('用户管理', 'system:user', 1, 1, '/system/user', 'User', 1),
('角色管理', 'system:role', 1, 1, '/system/role', 'UserFilled', 2),
('权限管理', 'system:permission', 1, 1, '/system/permission', 'Lock', 3),
('文件管理', 'file', 1, 0, '/files', 'Document', 2),
('流程图管理', 'flowchart', 1, 0, '/flowchart', 'Edit', 3),
-- 按钮权限
('用户新增', 'system:user:add', 2, 2, NULL, NULL, 1),
('用户编辑', 'system:user:edit', 2, 2, NULL, NULL, 2),
('用户删除', 'system:user:delete', 2, 2, NULL, NULL, 3),
('文件上传', 'file:upload', 2, 5, NULL, NULL, 1),
('文件删除', 'file:delete', 2, 5, NULL, NULL, 2);

-- 关联超级管理员角色权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission`;

-- =====================================================
-- 创建视图（可选）
-- =====================================================

-- 用户信息视图
CREATE OR REPLACE VIEW `v_user_info` AS
SELECT 
    u.id,
    u.username,
    u.nickname,
    u.email,
    u.phone,
    u.avatar,
    u.status,
    u.created_at,
    GROUP_CONCAT(r.role_name) AS roles
FROM `sys_user` u
LEFT JOIN `sys_user_role` ur ON u.id = ur.user_id
LEFT JOIN `sys_role` r ON ur.role_id = r.id AND r.deleted = 0
WHERE u.deleted = 0
GROUP BY u.id;

-- =====================================================
-- 创建存储过程（可选）
-- =====================================================

-- 删除用户（逻辑删除）
DELIMITER //
CREATE PROCEDURE `sp_delete_user`(IN p_user_id BIGINT)
BEGIN
    UPDATE `sys_user` SET `deleted` = 1, `updated_at` = NOW() WHERE `id` = p_user_id;
    DELETE FROM `sys_user_role` WHERE `user_id` = p_user_id;
END //
DELIMITER ;

-- =====================================================
-- 数据库初始化完成
-- =====================================================
