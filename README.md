# Campus Incident Reporting and Management System

A comprehensive, role-based incident management system designed for educational institutions to handle facility issues, safety problems, and equipment failures with proper workflow management and audit trails.

## 🎯 Purpose

This system enables users to:
- **Anonymously report** facility issues and safety concerns
- **Track incident lifecycle** from initial report to resolution
- **Manage assignments** to maintenance staff
- **Maintain detailed logs** of all resolution activities
- **Generate reports** and analytics for administrative oversight

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.2.0 with Java 17
- **Database**: H2 in-memory database (configurable for production)
- **Security**: Spring Security with role-based access control
- **API**: RESTful API with comprehensive endpoints
- **Validation**: Bean Validation (JSR-380)
- **Auditing**: JPA auditing for automatic timestamp management

### Core Components

#### 1. JPA Entities
- **User**: Role-based user management with anonymity support
- **IncidentReport**: Core incident tracking with status workflow
- **IncidentCategory**: Categorization with priority levels
- **IncidentStatus**: State machine for incident lifecycle
- **ResolutionLog**: Detailed activity tracking and audit trail
- **StatusUpdate**: Status transition history with notes

#### 2. Business Logic Layer
- **IncidentService**: Comprehensive incident management operations
- **Role-based permissions**: Different access levels for different user types
- **Workflow management**: Enforced status transitions
- **Audit logging**: Complete history of all changes

#### 3. Data Access Layer
- **Repository interfaces**: Custom query methods for complex operations
- **JPA relationships**: Proper entity associations and cascading
- **Performance optimization**: Lazy loading and efficient queries

## 🔐 Security & Roles

### User Roles
1. **REPORTER**: Can submit and view their own incidents
2. **MAINTENANCE**: Can view assigned incidents and update status
3. **ADMIN**: Full system access and management capabilities

### Access Control
- **Anonymous reporting**: Users can report without full identification
- **Role-based permissions**: Different operations available per role
- **Data isolation**: Users can only see incidents they're authorized to view
- **Audit trails**: All actions are logged with user attribution

## 📊 Incident Lifecycle

### Status Workflow
```
REPORTED → UNDER_REVIEW → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED
    ↓           ↓           ↓           ↓
CANCELLED   CANCELLED    ON_HOLD    ON_HOLD
```

### Business Rules
- **Status transitions**: Enforced workflow with validation
- **Assignment logic**: Only maintenance staff can be assigned
- **Priority management**: Automatic priority based on category
- **Resolution tracking**: Time, cost, and material logging

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Spring Boot 3.2.0

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd incident-management
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - **Main Application**: http://localhost:8080
   - **H2 Database Console**: http://localhost:8080/h2-console
   - **API Documentation**: Available at `/api/incidents`

### Default Credentials
- **Admin**: `admin` / `admin123`
- **Maintenance**: `maintenance1` / `maintenance123`
- **Reporter**: `reporter1` / `reporter123`

## 📡 API Endpoints

### Core Incident Operations
- `POST /api/incidents` - Create new incident
- `GET /api/incidents/{id}` - Get incident details
- `PUT /api/incidents/{id}` - Update incident
- `DELETE /api/incidents/{id}` - Delete incident

### Status Management
- `PATCH /api/incidents/{id}/status` - Update incident status
- `PATCH /api/incidents/{id}/assign` - Assign incident to staff
- `PATCH /api/incidents/{id}/start-work` - Begin work on incident
- `PATCH /api/incidents/{id}/complete-work` - Mark work as complete

### Resolution Logging
- `POST /api/incidents/{id}/logs` - Add resolution log entry
- `POST /api/incidents/{id}/time-logs` - Log time spent
- `POST /api/incidents/{id}/cost-logs` - Log costs incurred
- `POST /api/incidents/{id}/material-logs` - Log materials used

### Search & Reporting
- `GET /api/incidents/search` - Search incidents with pagination
- `GET /api/incidents/dashboard/stats` - Get dashboard statistics
- `GET /api/incidents/export/csv` - Export incidents to CSV
- `GET /api/incidents/export/pdf` - Export incidents to PDF

### Filtering & Analytics
- `GET /api/incidents/status/{status}` - Get incidents by status
- `GET /api/incidents/category/{categoryId}` - Get incidents by category
- `GET /api/incidents/urgent` - Get urgent incidents
- `GET /api/incidents/overdue` - Get overdue incidents

## 🗄️ Database Schema

### Key Tables
- **users**: User accounts with roles and permissions
- **incident_reports**: Main incident data with relationships
- **incident_categories**: Predefined incident categories
- **resolution_logs**: Detailed activity and time tracking
- **status_updates**: Complete status transition history

### Relationships
- **One-to-Many**: User → IncidentReports (as reporter)
- **One-to-Many**: User → IncidentReports (as assignee)
- **One-to-Many**: Category → IncidentReports
- **One-to-Many**: IncidentReport → ResolutionLogs
- **One-to-Many**: IncidentReport → StatusUpdates

## 🔧 Configuration

### Application Properties
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:incidentdb
    username: sa
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  security:
    user:
      name: admin
      password: admin123
      roles: ADMIN
```

### Customization Options
- **Database**: Change from H2 to PostgreSQL/MySQL for production
- **Security**: Integrate with LDAP/Active Directory
- **Notifications**: Add email/SMS notification services
- **File attachments**: Enable file uploads for incidents
- **Workflow**: Customize status transitions and business rules

## 📈 Features

### Core Functionality
- ✅ **Anonymous reporting** with optional user identification
- ✅ **Role-based access control** with granular permissions
- ✅ **Status workflow management** with validation
- ✅ **Assignment and tracking** to maintenance staff
- ✅ **Resolution logging** with time, cost, and material tracking
- ✅ **Audit trails** for all system changes
- ✅ **Search and filtering** with pagination
- ✅ **Dashboard analytics** and reporting
- ✅ **Export capabilities** (CSV, PDF)
- ✅ **Bulk operations** for efficient management

### Advanced Features
- ✅ **Priority management** with automatic categorization
- ✅ **Urgency flags** for critical issues
- ✅ **Confidentiality settings** for sensitive reports
- ✅ **Estimated resolution times** with overdue tracking
- ✅ **Category-based priority** assignment
- ✅ **Status transition validation** with business rules
- ✅ **User activity tracking** and performance metrics

## 🧪 Testing

### Test Coverage
- **Unit tests**: Service layer business logic
- **Integration tests**: Repository and service integration
- **Security tests**: Role-based access control
- **API tests**: REST endpoint functionality

### Running Tests
```bash
mvn test                    # Run all tests
mvn test -Dtest=*Service   # Run service tests only
mvn test -Dtest=*Controller # Run controller tests only
```

## 🚀 Deployment

### Production Considerations
1. **Database**: Use production database (PostgreSQL/MySQL)
2. **Security**: Configure proper authentication (LDAP/OAuth)
3. **Monitoring**: Add health checks and metrics
4. **Backup**: Implement database backup strategies
5. **SSL**: Enable HTTPS for production use

### Docker Support
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/incident-management-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 Contributing

### Development Guidelines
1. **Code Style**: Follow Java coding conventions
2. **Testing**: Write tests for new functionality
3. **Documentation**: Update API documentation
4. **Security**: Follow security best practices
5. **Performance**: Consider database query optimization

### Code Review Process
1. Create feature branch from main
2. Implement changes with tests
3. Submit pull request with description
4. Code review and approval
5. Merge to main branch

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

### Documentation
- **API Reference**: Available at `/api/incidents`
- **Database Schema**: See entity classes for structure
- **Business Rules**: Documented in service implementations

### Common Issues
1. **Database Connection**: Check H2 console access
2. **Authentication**: Verify user credentials and roles
3. **Permissions**: Ensure user has required role for operation
4. **Status Transitions**: Check business rule validation

### Getting Help
- **Issues**: Create GitHub issue with detailed description
- **Questions**: Use GitHub discussions for general questions
- **Contributions**: Submit pull requests for improvements

---

**Built with ❤️ for educational institutions and campus management teams.**
