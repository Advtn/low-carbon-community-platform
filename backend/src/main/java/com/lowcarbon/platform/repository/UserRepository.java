package com.lowcarbon.platform.repository;

import com.lowcarbon.platform.entity.User;
import com.lowcarbon.platform.enums.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    List<User> findTop10ByRoleOrderByTotalPointsDescTotalCarbonReductionDesc(UserRole role);

    long countByRole(UserRole role);

    long sumTotalPointsByRole(UserRole role);

    double sumTotalCarbonReductionByRole(UserRole role);

    Optional<User> findByIdForUpdate(Long id);

    List<User> findAll();

    Optional<User> findById(Long id);

    boolean existsById(Long id);

    void deleteById(Long id);

    User save(User user);
}
