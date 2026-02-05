# Contributing to Progress API

First off, thank you for considering contributing to Progress API! 

This document provides guidelines for contributing to this project. Following these guidelines helps communicate that you respect the time of the developers managing and developing this open source project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Pull Request Process](#pull-request-process)
- [Contributing Academic Structure Data](#contributing-academic-structure-data)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Enhancements](#suggesting-enhancements)

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

### Our Standards

- **Be respectful** and inclusive
- **Be collaborative** and constructive
- **Focus on what is best** for the community
- **Show empathy** towards other community members

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When you create a bug report, include as many details as possible:

- **Use a clear and descriptive title**
- **Describe the exact steps to reproduce the problem**
- **Provide specific examples** (code snippets, curl commands, etc.)
- **Describe the behavior you observed** and what you expected
- **Include screenshots** if applicable
- **Specify your environment** (OS, Java version, Spring Boot version)

Use the bug report template when creating issues.

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

- **Use a clear and descriptive title**
- **Provide a detailed description** of the suggested enhancement
- **Explain why this enhancement would be useful** to most users
- **List any similar features** in other projects (if applicable)

### Your First Code Contribution

Unsure where to begin? Look for issues labeled:
- `good first issue` - simpler issues for newcomers
- `help wanted` - issues where we'd appreciate community help

## Development Setup

### Prerequisites

- Java 17 or higher
- Maven 3.9+
- Git
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

### Setting Up Your Development Environment

For detailed setup instructions, environment variables, and running the application, see the main [README.md](./README.md#-quick-start).

**Quick setup:**

1. Fork and clone the repository
2. Add the upstream remote
3. Create a feature branch
4. Copy `.env.example` to `.env` and configure
5. Start coding!

```bash
git clone https://github.com/YOUR-USERNAME/progress-rebuild.git
cd progress-rebuild/backend
git remote add upstream https://github.com/ORIGINAL-OWNER/progress-rebuild.git
git checkout -b feature/your-feature-name
```

## Coding Standards

### Java Style Guide

- Follow standard Java naming conventions
- Use **camelCase** for variables and methods
- Use **PascalCase** for class names
- Use **UPPER_SNAKE_CASE** for constants
- Maximum line length: **120 characters**
- Indentation: **4 spaces** (no tabs)

### Code Organization

- Keep controllers thin - delegate to services
- Services contain business logic
- Use DTOs for data transfer
- Separate configuration classes
- One public class per file

### Documentation

- **JavaDoc** for all public classes and methods
- **Inline comments** for complex logic
- **Update README.md** if adding new features
- **API documentation** using OpenAPI annotations

Example:
```java
/**
 * Authenticates a user against the Progres external API.
 * 
 * @param request The login request containing username and password
 * @return LoginResponse with JWT tokens and user information
 * @throws ApiException if authentication fails
 */
public LoginResponse authenticate(LoginRequest request) {
    // Implementation
}
```

### Testing Requirements

All contributions **must include tests**:

- **Unit tests** for services and utilities
- **Integration tests** for controllers
- **Minimum 80% code coverage** for new code
- Mock external dependencies

Example test structure:
```java
@Test
void shouldAuthenticateUserSuccessfully() {
    // Given
    LoginRequest request = new LoginRequest("user", "pass");
    
    // When
    LoginResponse response = authService.authenticate(request);
    
    // Then
    assertNotNull(response.getToken());
    assertEquals("Authentication successful", response.getMessage());
}
```

### Commit Message Guidelines

Follow conventional commits format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Examples:**
```
feat(auth): add refresh token endpoint

Implement refresh token functionality to allow users to
obtain new access tokens without re-authenticating.

Closes #123
```

```
fix(security): resolve JWT expiration validation issue

Fixed bug where expired tokens were still being accepted
due to incorrect time comparison logic.

Fixes #456
```

## Pull Request Process

### Before Submitting

1. **Update your fork** with the latest upstream changes:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run all tests** and ensure they pass:
   ```bash
   ./mvnw clean test
   ```

3. **Run code coverage** and check the report:
   ```bash
   ./mvnw clean test jacoco:report
   ```

4. **Check for security vulnerabilities:**
   ```bash
   ./mvnw dependency-check:check
   ```

5. **Build the project:**
   ```bash
   ./mvnw clean package
   ```

### Submitting Your Pull Request

1. **Push to your fork:**
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create a pull request** on GitHub

3. **Fill out the PR template** completely

4. **Link related issues** using keywords (Fixes #123, Closes #456)

### Pull Request Requirements

Your PR must:

- Pass all CI/CD checks (build, tests, security scan)
- Include tests for new functionality
- Maintain or improve code coverage
- Update documentation (README, JavaDoc, etc.)
- Follow coding standards
- Have a clear description of changes
- Include screenshots (for UI changes)

### Review Process

1. A maintainer will review your PR within **2-3 business days**
2. Address any requested changes promptly
3. Once approved, a maintainer will merge your PR
4. Your contribution will be included in the next release

## Project Structure

Understanding the codebase structure helps you navigate and contribute effectively:

```
backend/
├── src/main/java/com/progress/api/
│   ├── config/          # Configuration classes (Security, CORS, etc.)
│   ├── controller/      # REST API controllers
│   ├── dto/             # Data Transfer Objects
│   ├── exception/       # Custom exceptions and handlers
│   ├── security/        # JWT and authentication logic
│   └── service/         # Business logic and external API calls
├── src/main/resources/
│   ├── application.yml       # Development configuration
│   └── application-prod.yml  # Production configuration
└── src/test/            # Test cases
```

## Contributing Academic Structure Data

Help expand the AI recommendation system by adding your university's academic structure!

### Why Contribute?

The recommendation system uses a JSON file containing fields, majors, and specialities to suggest academic paths. Currently, we have a generic LMD structure, but **your university may have unique options**.

### How to Add Your University

1. **Edit the academic structure file:**
   ```
   src/main/resources/data/academic-structure.json
   ```

2. **Add your university** under the `universities` key:
   ```json
   {
     "universities": {
       "univ-tlemcen": {
         "name": "Université Abou Bekr Belkaid Tlemcen",
         "fields": [
           {
             "code": "ST",
             "name": "Sciences et Technologies",
             "nameAr": "علوم وتكنولوجيا",
             "levels": {
               "L1": { ... },
               "L2": { "majors": [...] },
               "L3": { "specialities": [...] }
             },
             "masterSpecialities": [...]
           }
         ]
       }
     }
   }
   ```

3. **Required fields for each entry:**
   - `code`: Unique identifier (e.g., "GM", "L3_CM")
   - `name`: French name
   - `nameAr`: Arabic name

4. **Optional fields:**
   - `description`: Brief description
   - `masterOptions`: Array of master codes
   - `parentMajor`: For specialities, reference to L2 major

### Structure Example

```
Field (ST)
├── L1 (common core)
│   └── nextOptions: [L2_GM, L2_GC, ...]
├── L2 Majors
│   ├── Génie Mécanique (L2_GM)
│   │   └── nextOptions: [L3_CM, L3_SDM, L3_ENERG]
│   └── Génie Civil (L2_GC)
│       └── nextOptions: [L3_GC]
└── L3 Specialities
    ├── Construction Mécanique (L3_CM)
    │   ├── parentMajor: L2_GM
    │   └── masterOptions: [M_CIM, M_FAB]
    └── ...
```

### Commit Message Format

```
feat(academic-data): add Tlemcen University structure

Added ST and MI fields for Université de Tlemcen including:
- 5 L2 majors
- 12 L3 specialities
- 8 Master specialities
```

### Testing Your Changes

After adding data, test by running the application and calling:
```bash
curl -X POST http://localhost:8080/api/recommendations/suggest \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"universityCode": "univ-tlemcen"}'
```

## Development Workflow

### Feature Development

1. Create a new branch from `main`
2. Implement your feature with tests
3. Update documentation
4. Submit a pull request
5. Address review feedback
6. Merge after approval

### Bug Fixes

1. Create a new branch: `fix/bug-description`
2. Write a failing test that reproduces the bug
3. Fix the bug
4. Ensure the test passes
5. Submit a pull request

## Getting Help

If you need help:

-  **GitHub Discussions** - Ask questions and discuss ideas
-  **GitHub Issues** - Report bugs or request features
-  **Email** - Contact the maintainers directly

## Recognition

Contributors will be recognized in:
- The project's README
- Release notes
- GitHub's contributor graph

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to Progress API! 
