package com.myliu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建文件请求
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
public class FileCreateRequest {

    /**
     * 文件名
     */
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名不能超过255个字符")
    private String fileName;

    /**
     * 初始内容（可选）
     */
    private String content;
}
