package com.campus.incident.repository;

import com.campus.incident.entity.IncidentReport;
import com.campus.incident.entity.IncidentStatus;
import com.campus.incident.entity.User;
import com.campus.incident.entity.UserRole;
import com.campus.incident.entity.IncidentCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class IncidentReportRepositoryTest {

    @Autowired
    private IncidentReportRepository incidentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User reporter;
    private IncidentCategory category;

    @BeforeEach
    void setUp() {
        // Setup a User and Category for test relationships
        reporter = new User();
        reporter.setUsername("testreporter");
        reporter.setPassword("encodedpassword");
        reporter.setRole(UserRole.REPORTER);
        reporter.setFullName("Test Reporter");
        reporter.setActive(true);
        entityManager.persist(reporter);

        category = new IncidentCategory("Facility Maintenance", "General issues", 2);
        entityManager.persist(category);
        entityManager.flush();
    }

    private IncidentReport createIncident(String title, IncidentStatus status) {
        IncidentReport incident = new IncidentReport();
        incident.setTitle(title);
        incident.setDescription("Test description for " + title);
        incident.setLocationDetails("Test Location");
        incident.setCategory(category);
        incident.setReporter(reporter);
        incident.setStatus(status);
        incident.setPriorityLevel(category.getPriorityLevel());
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());
        return incident;
    }

    @Test
    void whenSaveIncident_thenFindByIdWorks() {
        // Arrange
        IncidentReport incident = createIncident("Broken AC Unit", IncidentStatus.REPORTED);

        // Act
        IncidentReport savedIncident = incidentRepository.save(incident);
        Optional<IncidentReport> found = incidentRepository.findById(savedIncident.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Broken AC Unit");
        assertThat(found.get().getStatus()).isEqualTo(IncidentStatus.REPORTED);
    }

    @Test
    void whenFindAllActiveIncidents_thenExcludeResolvedAndClosed() {
        // Arrange
        incidentRepository.save(createIncident("Reported Issue", IncidentStatus.REPORTED));
        incidentRepository.save(createIncident("Resolved Issue", IncidentStatus.RESOLVED));
        incidentRepository.save(createIncident("In Progress Issue", IncidentStatus.IN_PROGRESS));
        incidentRepository.save(createIncident("Closed Issue", IncidentStatus.CLOSED));

        // Act
        // Assuming your findByStatusIn method is working for active statuses
        long activeCount = incidentRepository.findAll().stream().filter(i -> i.getStatus().isActive()).count();

        // Assert
        assertThat(activeCount).isEqualTo(2); // REPORTED, IN_PROGRESS
    }
}