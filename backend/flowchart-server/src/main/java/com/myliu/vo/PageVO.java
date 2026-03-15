package com.myliu.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页结果VO
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Data
public class PageVO<T> {

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总数
     */
    private Long total;

    /**
     * 当前页
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    public static <T> PageVO<T> of(List<T> list, Long total, Integer page, Integer size) {
        PageVO<T> vo = new PageVO<>();
        vo.setList(list);
        vo.setTotal(total);
        vo.setPage(page);
        vo.setSize(size);
        return vo;
    }
}
