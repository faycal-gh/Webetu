# Security Policy

## Supported Versions

We release patches for security vulnerabilities in the following versions:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

We take the security of Progress API seriously. If you discover a security vulnerability, please follow these steps:

### 1. **DO NOT** Create a Public Issue

Please do not report security vulnerabilities through public GitHub issues, discussions, or pull requests.

### 2. Report Privately

Send an email to: **ghalifaycal27@gmail.com** (or create a private security advisory on GitHub)

Include the following information:
- Type of vulnerability
- Full paths of source file(s) related to the vulnerability
- Location of the affected source code (tag/branch/commit or direct URL)
- Step-by-step instructions to reproduce the issue
- Proof-of-concept or exploit code (if possible)
- Impact of the vulnerability
- Suggested fix (if available)

### 3. Response Timeline

- **24 hours**: Initial acknowledgment of your report
- **72 hours**: Initial assessment and severity classification
- **7 days**: Detailed response with fix timeline
- **30 days**: Security patch release (for critical issues)

## Security Best Practices

### For Developers

#### JWT Secret Management
```bash
# NEVER commit JWT secrets to version control
# Generate strong secrets:
openssl rand -base64 32

# Use environment variables in production:
export JWT_SECRET=<your-generated-secret>
```

#### Environment Variables
- Use `.env` files for local development only
- Set environment variables directly in production platforms
- Never commit `.env` files to Git
- Rotate secrets regularly

#### Dependencies
```bash
# Check for vulnerabilities regularly:
./mvnw dependency-check:check

# Keep dependencies up to date:
./mvnw versions:display-dependency-updates
```

### For Deployment

#### Production Checklist

- [ ] Set strong `JWT_SECRET` (not the default value)
- [ ] Use HTTPS for all CORS origins
- [ ] Enable `SPRING_PROFILES_ACTIVE=prod`
- [ ] Restrict CORS to specific domains (not `*`)
- [ ] Use secure deployment platform
- [ ] Enable monitoring and logging
- [ ] Implement rate limiting at infrastructure level
- [ ] Use Redis for token blacklist in distributed deployments
- [ ] Regular security updates and patches
- [ ] Configure firewall rules
- [ ] Use secrets management service (AWS Secrets Manager, HashiCorp Vault, etc.)

#### Container Security
```dockerfile
# Use specific image versions (not 'latest')
FROM eclipse-temurin:17-jre-alpine@sha256:...

# Run as non-root user
USER appuser

# Use read-only filesystem where possible
# Scan images for vulnerabilities
```

#### Network Security
- Use HTTPS/TLS for all production traffic
- Configure proper security headers (already included)
- Implement Web Application Firewall (WAF)
- Use DDoS protection

### Security Headers (Already Configured)

This backend automatically sets the following security headers:

```http
Content-Security-Policy: default-src 'self'
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
```

## Known Security Considerations

### In-Memory Token Blacklist

️ **Limitation**: The current implementation uses in-memory storage for blacklisted tokens.

**Security Implications**:
- Tokens are not blacklisted across multiple instances
- Blacklist is cleared on application restart
- Logged-out users could still access the API if tokens are cached

**Mitigation for Production**:
- Implement Redis-based token blacklist
- Use short token expiration times (15 minutes)
- Monitor for suspicious activity

### Rate Limiting

️ **Limitation**: In-memory rate limiting per instance.

**Mitigation**:
- Deploy behind API Gateway with distributed rate limiting
- Use Redis for distributed rate limiting
- Implement additional rate limiting at infrastructure level

### External API Dependency

️ **Consideration**: This backend proxies requests to `progres.mesrs.dz/api`.

**Security Measures**:
- Validate all responses from external API
- Implement timeouts (10 seconds configured)
- Handle errors gracefully
- No credentials are stored or cached
- Tokens are stored only in memory during request processing

## Disclosure Policy

When we receive a security bug report, we will:

1. Confirm the vulnerability
2. Determine the severity and impact
3. Develop and test a fix
4. Release a security patch
5. Publicly disclose the vulnerability after the patch is released

We ask that you:
- Allow us reasonable time to fix the issue before any public disclosure
- Make a good faith effort to avoid privacy violations, data destruction, and interruption of service

## Security Hall of Fame

We recognize and thank security researchers who responsibly disclose vulnerabilities:

<!-- List of contributors will be added here -->

---

## Additional Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)
- [Docker Security Best Practices](https://docs.docker.com/engine/security/)

## Contact

For security-related questions: **security@yourdomain.com**

For general support: See [README.md](./README.md)

---

**Last Updated**: January 2026
