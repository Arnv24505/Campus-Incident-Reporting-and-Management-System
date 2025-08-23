package com.campus.incident.repository;

import com.campus.incident.entity.User;
import com.campus.incident.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByIsActive(boolean isActive);
    
    List<User> findByRoleAndIsActive(UserRole role, boolean isActive);
    
    @Query("SELECT u FROM User u WHERE u.role IN (:roles) AND u.isActive = true")
    List<User> findActiveUsersByRoles(@Param("roles") List<UserRole> roles);
    
    @Query("SELECT u FROM User u WHERE u.username LIKE %:searchTerm% OR u.fullName LIKE %:searchTerm% OR u.email LIKE %:searchTerm%")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveUsersByRole(@Param("role") UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.id IN (SELECT DISTINCT ir.assignedTo.id FROM IncidentReport ir WHERE ir.status IN ('ASSIGNED', 'IN_PROGRESS'))")
    List<User> findUsersWithActiveAssignments();
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
