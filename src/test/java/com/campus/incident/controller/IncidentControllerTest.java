package com.campus.incident.controller;

import com.campus.incident.config.SecurityConfig;
import com.campus.incident.config.DataInitializer;
import com.campus.incident.entity.IncidentStatus;
import com.campus.incident.entity.IncidentReport;
import com.campus.incident.entity.User;
import com.campus.incident.entity.UserRole;
import com.campus.incident.repository.UserRepository;
import com.campus.incident.repository.IncidentCategoryRepository;
import com.campus.incident.repository.IncidentReportRepository;
import com.campus.incident.service.IncidentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware; // ⬅️ IMPORT FOR THE FIX
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext; // ⬅️ IMPORT TO MOCK JPA METAMODEL
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = IncidentController.class,
        // Exclude DataInitializer, which requires repository beans
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = DataInitializer.class)
)
@Import({SecurityConfig.class}) // Loads the security rules
public class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncidentService incidentService;

    // Mocks for SecurityConfig and DataInitializer dependencies:
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private IncidentCategoryRepository categoryRepository;
    @MockBean
    private IncidentReportRepository incidentRepository;

    // 💥 THE FIX: Mock the AuditorAware bean to bypass JPA Auditing initialization failure!
    @MockBean
    private AuditorAware<String> auditorAware;

    // Mock JPA metamodel to avoid "JPA metamodel must not be empty" in @WebMvcTest
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @Test
    @WithMockUser(username = "reporter", roles = {"REPORTER"})
    void whenReporterTriesToAssignIncident_thenIsForbidden() throws Exception {
        // Test ensures a REPORTER cannot access a restricted endpoint (e.g., /assign)
        String url = "/api/incidents/1/assign";
        String assignPayload = "{\"assignedToId\": 3, \"notes\": \"Assigning maintenance\"}";

        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignPayload))
                .andExpect(status().isForbidden()); // Expect HTTP 403 Forbidden
    }

    @Test
    @WithMockUser(username = "maintenance", roles = {"MAINTENANCE"})
    void whenMaintenanceUpdatesStatus_thenIsOk() throws Exception {
        // Test ensures a MAINTENANCE user CAN update an incident status.
        String url = "/api/incidents/1/status";

        // Stub current user lookup
        User maintenanceUser = new User();
        maintenanceUser.setUsername("maintenance");
        maintenanceUser.setRole(UserRole.MAINTENANCE);
        when(userRepository.findByUsername("maintenance")).thenReturn(Optional.of(maintenanceUser));

        // Mock both overloads in case controller uses either, return populated entity to serialize
        IncidentReport response = new IncidentReport();
        response.setTitle("t");
        response.setDescription("d");
        response.setStatus(IncidentStatus.UNDER_REVIEW);
        response.setCreatedAt(java.time.LocalDateTime.now());
        response.setUpdatedAt(java.time.LocalDateTime.now());

        when(incidentService.updateIncidentStatus(any(Long.class), any(IncidentStatus.class), any(User.class)))
                .thenReturn(response);
        when(incidentService.updateIncidentStatus(any(Long.class), any(IncidentStatus.class), any(User.class), any(String.class)))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.patch(url)
                        .with(csrf())
                        .param("status", "UNDER_REVIEW"))
                .andExpect(status().isOk()); // Expect HTTP 200 OK
    }
}