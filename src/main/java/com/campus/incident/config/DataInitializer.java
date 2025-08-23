package com.campus.incident.config;

import com.campus.incident.entity.*;
import com.campus.incident.repository.IncidentCategoryRepository;
import com.campus.incident.repository.IncidentReportRepository;
import com.campus.incident.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private IncidentCategoryRepository categoryRepository;
    
    @Autowired
    private IncidentReportRepository incidentRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no data exists
        if (userRepository.count() == 0) {
            initializeData();
        }
    }
    
    private void initializeData() {
        // Create incident categories
        List<IncidentCategory> categories = createCategories();
        categoryRepository.saveAll(categories);
        
        // Create users
        List<User> users = createUsers();
        userRepository.saveAll(users);
        
        // Create sample incidents
        createSampleIncidents(categories, users);
    }
    
    private List<IncidentCategory> createCategories() {
        return Arrays.asList(
            new IncidentCategory("Facility Maintenance", "General building and facility issues", 2),
            new IncidentCategory("Equipment Failure", "Broken or malfunctioning equipment", 3),
            new IncidentCategory("Safety Hazard", "Safety concerns and hazards", 4),
            new IncidentCategory("IT Issues", "Computer and technology problems", 2),
            new IncidentCategory("HVAC Problems", "Heating, ventilation, and air conditioning issues", 3),
            new IncidentCategory("Plumbing Issues", "Water, drainage, and plumbing problems", 3),
            new IncidentCategory("Electrical Problems", "Electrical and power issues", 4),
            new IncidentCategory("Structural Issues", "Building structure and integrity concerns", 4),
            new IncidentCategory("Cleaning & Sanitation", "Cleaning and hygiene related issues", 1),
            new IncidentCategory("Other", "Miscellaneous issues not covered by other categories", 1)
        );
    }
    
    private List<User> createUsers() {
        return Arrays.asList(
            createUser("admin", "admin123", "admin@campus.edu", "System Administrator", UserRole.ADMIN, false),
            createUser("maintenance1", "maintenance123", "maintenance1@campus.edu", "John Smith", UserRole.MAINTENANCE, false),
            createUser("maintenance2", "maintenance123", "maintenance2@campus.edu", "Sarah Johnson", UserRole.MAINTENANCE, false),
            createUser("reporter1", "reporter123", "student1@campus.edu", "Alice Brown", UserRole.REPORTER, false),
            createUser("reporter2", "reporter123", "faculty1@campus.edu", "Dr. Robert Wilson", UserRole.REPORTER, false),
            createUser("anonymous1", "anonymous123", null, "Anonymous Reporter", UserRole.REPORTER, true)
        );
    }
    
    private User createUser(String username, String password, String email, String fullName, UserRole role, boolean isAnonymous) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role);
        user.setAnonymous(isAnonymous);
        user.setActive(true);
        return user;
    }
    
    private void createSampleIncidents(List<IncidentCategory> categories, List<User> users) {
        User admin = users.stream().filter(u -> u.getRole() == UserRole.ADMIN).findFirst().orElse(users.get(0));
        User maintenance1 = users.stream().filter(u -> u.getUsername().equals("maintenance1")).findFirst().orElse(users.get(0));
        User reporter1 = users.stream().filter(u -> u.getUsername().equals("reporter1")).findFirst().orElse(users.get(0));
        User anonymous1 = users.stream().filter(u -> u.getUsername().equals("anonymous1")).findFirst().orElse(users.get(0));
        
        IncidentCategory facilityCategory = categories.stream().filter(c -> c.getName().equals("Facility Maintenance")).findFirst().orElse(categories.get(0));
        IncidentCategory safetyCategory = categories.stream().filter(c -> c.getName().equals("Safety Hazard")).findFirst().orElse(categories.get(0));
        IncidentCategory equipmentCategory = categories.stream().filter(c -> c.getName().equals("Equipment Failure")).findFirst().orElse(categories.get(0));
        IncidentCategory hvacCategory = categories.stream().filter(c -> c.getName().equals("HVAC Problems")).findFirst().orElse(categories.get(0));
        
        // Create sample incidents
        List<IncidentReport> incidents = Arrays.asList(
            // Incident 1: Broken chair in library
            createIncident(
                "Broken Chair in Library Study Area",
                "One of the chairs in the library study area has a broken leg and is unsafe to use. Students are still trying to sit on it.",
                "Library - 2nd Floor Study Area, Chair #12",
                facilityCategory,
                reporter1,
                null,
                IncidentStatus.REPORTED,
                2,
                false,
                false
            ),
            
            // Incident 2: Safety hazard - wet floor
            createIncident(
                "Wet Floor in Science Building",
                "There's a significant water leak in the science building hallway. The floor is very wet and slippery, creating a safety hazard.",
                "Science Building - Main Hallway, 1st Floor",
                safetyCategory,
                anonymous1,
                null,
                IncidentStatus.UNDER_REVIEW,
                4,
                true,
                false
            ),
            
            // Incident 3: Projector not working
            createIncident(
                "Projector Not Working in Room 201",
                "The projector in classroom 201 is not turning on. Tried multiple times but it remains completely unresponsive.",
                "Main Building - Room 201",
                equipmentCategory,
                reporter1,
                maintenance1,
                IncidentStatus.ASSIGNED,
                2,
                false,
                false
            ),
            
            // Incident 4: HVAC issue
            createIncident(
                "Air Conditioning Not Working in Computer Lab",
                "The air conditioning in the computer lab is not working properly. Room temperature is very high and uncomfortable for students.",
                "Technology Building - Computer Lab A",
                hvacCategory,
                reporter1,
                maintenance1,
                IncidentStatus.IN_PROGRESS,
                3,
                false,
                false
            ),
            
            // Incident 5: Resolved incident
            createIncident(
                "Light Bulb Replacement in Stairwell",
                "Several light bulbs in the main stairwell are burned out, making it difficult to see at night.",
                "Main Building - Central Stairwell",
                facilityCategory,
                reporter1,
                maintenance1,
                IncidentStatus.RESOLVED,
                1,
                false,
                false
            )
        );
        
        incidentRepository.saveAll(incidents);
        
        // Add some resolution logs to the resolved incident
        IncidentReport resolvedIncident = incidents.get(4);
        resolvedIncident.addResolutionLog("Light bulbs replaced", "Replaced 3 burned out light bulbs in stairwell", maintenance1);
        resolvedIncident.setActualResolutionDate(LocalDateTime.now().minusHours(2));
        incidentRepository.save(resolvedIncident);
    }
    
    private IncidentReport createIncident(String title, String description, String location, 
                                        IncidentCategory category, User reporter, User assignedTo,
                                        IncidentStatus status, Integer priority, boolean isUrgent, boolean isConfidential) {
        IncidentReport incident = new IncidentReport();
        incident.setTitle(title);
        incident.setDescription(description);
        incident.setLocationDetails(location);
        incident.setCategory(category);
        incident.setReporter(reporter);
        incident.setAssignedTo(assignedTo);
        incident.setStatus(status);
        incident.setPriorityLevel(priority);
        incident.setUrgent(isUrgent);
        incident.setConfidential(isConfidential);
        incident.setCreatedAt(LocalDateTime.now().minusDays((long) (Math.random() * 7)));
        incident.setUpdatedAt(LocalDateTime.now().minusDays((long) (Math.random() * 3)));
        
        if (assignedTo != null) {
            incident.setEstimatedResolutionDate(LocalDateTime.now().plusDays(3));
        }
        
        return incident;
    }
}
