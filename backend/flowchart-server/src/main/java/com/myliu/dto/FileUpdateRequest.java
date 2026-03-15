package com.myliu.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新文件请求
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
public class FileUpdateRequest {

    /**
     * 文件名
     */
    @Size(max = 255, message = "文件名不能超过255个字符")
    private String fileName;

    /**
     * 状态：1草稿，2已发布
     */
    private Integer status;
}
