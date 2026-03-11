package com.myliu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程图文件实体
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
@TableName("flowchart_file")
public class FlowchartFile {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 缩略图路径
     */
    private String thumbnail;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 所属项目ID
     */
    private Long projectId;

    /**
     * 状态：1草稿，2已发布
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除：0未删除，1已删除
     */
    @TableLogic
    private Integer deleted;
}
