package com.myliu.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件信息VO
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
public class FileVO {

    private Long id;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String thumbnail;

    private Long creatorId;

    private Long projectId;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
