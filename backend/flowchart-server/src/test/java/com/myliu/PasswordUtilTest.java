package com.myliu;

import com.myliu.util.PasswordUtil;
import org.junit.jupiter.api.Test;

/**
 * 密码工具测试类
 *
 * @author MyLiu
 * @since 1.0.0
 */
public class PasswordUtilTest {

    @Test
    public void testEncodePassword() {
        String rawPassword = "admin123";
        
        // 生成新的密码哈希
        String newHash = PasswordUtil.encode(rawPassword);
        
        System.out.println("========================================");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后的哈希: " + newHash);
        System.out.println("========================================");
        
        // 验证数据库中的哈希是否匹配
        String dbHash = "$2a$10$S9qo8uLOickgxZ2MRZoMyeljZRgDgJj/n3.QGh3TTTh8vZPPJjqZ1a";
        boolean matchResult = PasswordUtil.matches(rawPassword, dbHash);
        
        System.out.println("数据库中的哈希: " + dbHash);
        System.out.println("密码匹配结果: " + matchResult);
        System.out.println("========================================");
        
        // 生成可用于更新数据库的SQL
        System.out.println("更新数据库SQL:");
        System.out.println("UPDATE sys_user SET password = '" + newHash + "' WHERE username = 'admin';");
        System.out.println("========================================");
        
        // 验证新生成的哈希是否正确
        boolean newHashValid = PasswordUtil.matches(rawPassword, newHash);
        System.out.println("新哈希验证结果: " + newHashValid);
    }
}
