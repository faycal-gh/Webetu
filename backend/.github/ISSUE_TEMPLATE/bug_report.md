---
name: Bug Report
about: Create a report to help us improve
title: '[BUG] '
labels: bug
assignees: ''
---

## 🐛 Bug Description

A clear and concise description of what the bug is.

## 📋 To Reproduce

Steps to reproduce the behavior:

1. Send request to '...'
2. With payload '...'
3. Observe error '...'

**Minimal reproducible example:**

```bash
# Example curl command or code snippet
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "test", "password": "test"}'
```

## ✅ Expected Behavior

A clear and concise description of what you expected to happen.

## ❌ Actual Behavior

A clear and concise description of what actually happened.

## 📸 Screenshots / Logs

If applicable, add screenshots or error logs to help explain your problem.

```
Paste error logs here
```

## 🖥️ Environment

**Backend:**
- OS: [e.g., Ubuntu 22.04, macOS, Windows 11]
- Java Version: [e.g., Java 17]
- Spring Boot Version: [e.g., 3.2.2]
- Deployment: [e.g., Docker, Local, Koyeb]

**Configuration:**
- Profile: [e.g., default, prod]
- Environment Variables: [list any relevant env vars, **DO NOT include secrets**]

**Client:**
- Frontend Framework: [e.g., React 18, Vue 3]
- Browser: [e.g., Chrome 120, Firefox 121]

## 🔍 Additional Context

Add any other context about the problem here. For example:
- Does this happen consistently or intermittently?
- Did this work before? If so, when did it break?
- Are there any workarounds?

## 🔖 Related Issues

- Related to #(issue number)
- Similar to #(issue number)

## ✔️ Checklist

- [ ] I have searched existing issues to avoid duplicates
- [ ] I have provided steps to reproduce
- [ ] I have included relevant logs/screenshots
- [ ] I have removed any sensitive information (passwords, tokens, etc.)
