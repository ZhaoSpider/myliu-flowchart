package com.myliu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myliu.entity.FlowchartFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件数据访问层
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Mapper
public interface FileMapper extends BaseMapper<FlowchartFile> {
}
