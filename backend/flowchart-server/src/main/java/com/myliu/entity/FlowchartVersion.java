package com.myliu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程图版本实体
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
@TableName("flowchart_version")
public class FlowchartVersion {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 版本号
     */
    private String versionNo;

    /**
     * 流程图JSON内容
     */
    private String content;

    /**
     * 变更说明
     */
    private String changeLog;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
