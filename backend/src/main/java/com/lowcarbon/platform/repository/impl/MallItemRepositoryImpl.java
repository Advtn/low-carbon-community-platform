package com.lowcarbon.platform.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lowcarbon.platform.entity.MallItem;
import com.lowcarbon.platform.mapper.MallItemMapper;
import com.lowcarbon.platform.repository.MallItemRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MallItemRepositoryImpl implements MallItemRepository {

    private final MallItemMapper itemMapper;

    public MallItemRepositoryImpl(MallItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    @Override
    public List<MallItem> findByEnabledTrueOrderByIdAsc() {
        return itemMapper.selectList(new LambdaQueryWrapper<MallItem>()
                .eq(MallItem::getEnabled, true)
                .orderByAsc(MallItem::getId));
    }

    @Override
    public long countByEnabledTrue() {
        return itemMapper.selectCount(new LambdaQueryWrapper<MallItem>()
                .eq(MallItem::getEnabled, true));
    }

    @Override
    public long sumEnabledStock() {
        Object value = itemMapper.selectObjs(new QueryWrapper<MallItem>()
                .select("coalesce(sum(stock), 0)")
                .eq("enabled", true))
                .stream()
                .findFirst()
                .orElse(0);
        return ((Number) value).longValue();
    }

    @Override
    public Optional<MallItem> findByIdForUpdate(Long id) {
        return Optional.ofNullable(itemMapper.selectByIdForUpdate(id));
    }

    @Override
    public List<MallItem> findAll() {
        return itemMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public Optional<MallItem> findById(Long id) {
        return Optional.ofNullable(itemMapper.selectById(id));
    }

    @Override
    public boolean existsById(Long id) {
        return itemMapper.selectCount(new LambdaQueryWrapper<MallItem>().eq(MallItem::getId, id)) > 0;
    }

    @Override
    public void deleteById(Long id) {
        itemMapper.deleteById(id);
    }

    @Override
    public MallItem save(MallItem item) {
        if (Objects.isNull(item.getId())) {
            itemMapper.insert(item);
        } else {
            itemMapper.updateById(item);
        }
        return item;
    }
}
