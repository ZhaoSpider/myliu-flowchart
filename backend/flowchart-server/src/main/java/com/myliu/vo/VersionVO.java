package com.myliu.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 版本信息VO
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
public class VersionVO {

    private Long id;

    private Long fileId;

    private String versionNo;

    private String changeLog;

    private Long creatorId;

    private LocalDateTime createdAt;
}
