package com.myliu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 保存内容请求
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
public class SaveContentRequest {

    /**
     * 流程图JSON内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 变更说明（可选）
     */
    private String changeLog;
}
