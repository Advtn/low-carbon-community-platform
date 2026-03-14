package com.lowcarbon.platform.repository;

import com.lowcarbon.platform.entity.User;
import com.lowcarbon.platform.enums.UserRole;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findTop10ByRoleOrderByTotalPointsDescTotalCarbonReductionDesc(UserRole role);

    long countByRole(UserRole role);

    @Query("select coalesce(sum(u.totalPoints), 0) from User u where u.role = :role")
    long sumTotalPointsByRole(@Param("role") UserRole role);

    @Query("select coalesce(sum(u.totalCarbonReduction), 0.0) from User u where u.role = :role")
    double sumTotalCarbonReductionByRole(@Param("role") UserRole role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);
}
