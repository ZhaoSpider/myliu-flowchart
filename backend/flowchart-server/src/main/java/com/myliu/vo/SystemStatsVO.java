package com.myliu.vo;

import lombok.Data;

/**
 * 系统统计VO
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
public class SystemStatsVO {

    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 今日新增用户
     */
    private Long todayNewUsers;

    /**
     * 总文件数
     */
    private Long totalFiles;

    /**
     * 今日新增文件
     */
    private Long todayNewFiles;

    /**
     * 总版本数
     */
    private Long totalVersions;

    /**
     * 系统运行时间（毫秒）
     */
    private Long uptime;

    /**
     * JVM最大内存
     */
    private Long maxMemory;

    /**
     * JVM已用内存
     */
    private Long usedMemory;

    /**
     * JVM空闲内存
     */
    private Long freeMemory;
}
