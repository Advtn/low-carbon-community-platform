package com.lowcarbon.platform.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lowcarbon.platform.entity.User;
import com.lowcarbon.platform.enums.UserRole;
import com.lowcarbon.platform.mapper.UserMapper;
import com.lowcarbon.platform.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username).last("limit 1")
        ));
    }

    @Override
    public List<User> findTop10ByRoleOrderByTotalPointsDescTotalCarbonReductionDesc(UserRole role) {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, role)
                .orderByDesc(User::getTotalPoints)
                .orderByDesc(User::getTotalCarbonReduction)
                .last("limit 10"));
    }

    @Override
    public long countByRole(UserRole role) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, role));
    }

    @Override
    public long sumTotalPointsByRole(UserRole role) {
        Object value = userMapper.selectObjs(new QueryWrapper<User>()
                .select("coalesce(sum(total_points), 0)")
                .eq("role", role.name()))
                .stream()
                .findFirst()
                .orElse(0);
        return ((Number) value).longValue();
    }

    @Override
    public double sumTotalCarbonReductionByRole(UserRole role) {
        Object value = userMapper.selectObjs(new QueryWrapper<User>()
                .select("coalesce(sum(total_carbon_reduction), 0)")
                .eq("role", role.name()))
                .stream()
                .findFirst()
                .orElse(0D);
        return ((Number) value).doubleValue();
    }

    @Override
    public Optional<User> findByIdForUpdate(Long id) {
        return Optional.ofNullable(userMapper.selectByIdForUpdate(id));
    }

    @Override
    public List<User> findAll() {
        return userMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }

    @Override
    public boolean existsById(Long id) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getId, id)) > 0;
    }

    @Override
    public void deleteById(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public User save(User user) {
        if (Objects.isNull(user.getId())) {
            userMapper.insert(user);
        } else {
            userMapper.updateById(user);
        }
        return user;
    }
}
