package com.campus.incident.service;

import com.campus.incident.entity.IncidentReport;
import com.campus.incident.entity.IncidentStatus;
import com.campus.incident.entity.User;
import com.campus.incident.entity.UserRole;
import com.campus.incident.repository.IncidentReportRepository;
import com.campus.incident.repository.UserRepository;
import com.campus.incident.service.impl.IncidentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IncidentServiceTest {

    @InjectMocks
    private IncidentServiceImpl incidentService;

    @Mock
    private IncidentReportRepository incidentRepository;

    @Mock
    private UserRepository userRepository;

    private User adminUser;
    private IncidentReport incident;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole(UserRole.ADMIN);

        incident = new IncidentReport();
        incident.setId(100L);
        incident.setTitle("Leaky Faucet");
        incident.setStatus(IncidentStatus.REPORTED);
        incident.setReporter(adminUser);
    }

    @Test
    void whenUpdateStatusToValidNextStatus_thenStatusIsUpdatedAndSaved() {
        // Arrange
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(IncidentReport.class))).thenReturn(incident);

        // Act
        IncidentReport updatedIncident = incidentService.updateIncidentStatus(
                100L, IncidentStatus.UNDER_REVIEW, adminUser, "Starting review."
        );

        // Assert
        assertThat(updatedIncident.getStatus()).isEqualTo(IncidentStatus.UNDER_REVIEW);
        verify(incidentRepository, times(1)).save(any(IncidentReport.class)); // Verify save was called once
    }

    @Test
    void whenUpdateStatusToInvalidStatus_thenThrowsException() {
        // Arrange
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));

        // Act & Assert: Directly from REPORTED to RESOLVED is invalid according to entity logic
        assertThrows(RuntimeException.class, () -> {
            incidentService.updateIncidentStatus(
                    100L, IncidentStatus.RESOLVED, adminUser, "Skip everything"
            );
        });

        verify(incidentRepository, never()).save(any()); // Verify save was never called
    }
}