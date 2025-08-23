package com.campus.incident.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;
    
    @Email(message = "Email must be valid")
    @Column(unique = true)
    private String email;
    
    @Column(name = "full_name")
    private String fullName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.REPORTER;
    
    @Column(name = "is_anonymous")
    private boolean isAnonymous = false;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<IncidentReport> reports = new HashSet<>();
    
    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<IncidentReport> assignedIncidents = new HashSet<>();
    
    // Constructors
    public User() {}
    
    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Set<IncidentReport> getReports() { return reports; }
    public void setReports(Set<IncidentReport> reports) { this.reports = reports; }
    
    public Set<IncidentReport> getAssignedIncidents() { return assignedIncidents; }
    public void setAssignedIncidents(Set<IncidentReport> assignedIncidents) { this.assignedIncidents = assignedIncidents; }
    
    // Business Methods
    public boolean hasRole(UserRole role) {
        return this.role == role;
    }
    
    public boolean canManageIncidents() {
        return this.role == UserRole.ADMIN || this.role == UserRole.MAINTENANCE;
    }
    
    public boolean canViewAllReports() {
        return this.role == UserRole.ADMIN || this.role == UserRole.MAINTENANCE;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", isAnonymous=" + isAnonymous +
                '}';
    }
}
