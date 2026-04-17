package com.lowcarbon.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lowcarbon.platform.entity.MallItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MallItemMapper extends BaseMapper<MallItem> {

    @Select("select * from mall_items where id = #{id} for update")
    MallItem selectByIdForUpdate(Long id);
}

