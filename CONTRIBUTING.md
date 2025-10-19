# Git Flow and Commit Conventions

## Git Flow Workflow

This project follows Git Flow branching strategy with the following branch structure:

### Branch Types

```
main            # Production releases
  └── develop   # Integration/development branch
       ├── feature/*   # New features
       ├── bugfix/*    # Bug fixes
       ├── hotfix/*    # Urgent production fixes
       └── release/*   # Release preparation
```

### Branch Naming Convention

- `feature/short-description` - New features (e.g., `feature/encrypted-storage`)
- `bugfix/issue-description` - Bug fixes (e.g., `bugfix/service-crash`)
- `hotfix/critical-fix` - Critical production fixes (e.g., `hotfix/security-patch`)
- `release/version` - Release branches (e.g., `release/1.0.0`)

**Rules:**
- Use lowercase with hyphens
- Maximum 50 characters
- Be descriptive but concise

## Workflow Process

### 1. Start New Work

```bash
# Update develop branch
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/your-feature-name
```

### 2. Make Changes and Commit

```bash
# Stage changes
git add .

# Commit with conventional commit message
git commit -m "feat(location): add location encryption"

# Push to remote
git push origin feature/your-feature-name
```

### 3. Create Pull Request

1. Push your branch to GitHub
2. Open PR from `your-branch` → `develop`
3. Wait for CI checks to pass
4. Get code review approval
5. Merge to develop

### 4. After Merge

```bash
# Delete local branch
git branch -d feature/your-feature-name

# Update develop
git checkout develop
git pull origin develop
```

## Commit Message Convention

### Format

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

### Commit Types

- `feat` - New feature
- `fix` - Bug fix
- `refactor` - Code refactoring (no functional change)
- `test` - Add or update tests
- `docs` - Documentation changes
- `style` - Code style/formatting
- `chore` - Maintenance tasks (dependencies, build)
- `ci` - CI/CD pipeline changes

### Scope

Module or component affected:
- `location` - Location App
- `internet` - Internet App
- `core` - Core modules
- `ipc` - Inter-process communication
- `service` - Background service
- `security` - Security/encryption
- `ci` - CI/CD

### Subject Line

- Use imperative mood: "add feature" not "added feature"
- Lowercase first letter (except proper nouns)
- No period at the end
- Maximum 50 characters

### Examples

```bash
# Good commits
git commit -m "feat(location): implement encrypted database storage"
git commit -m "fix(internet): resolve IPC timeout on Android 12+"
git commit -m "test(location): add service lifecycle scenario tests"
git commit -m "refactor(core): extract IPC protocol to shared module"
git commit -m "docs: update README with security implementation"
git commit -m "ci: add test report generation to pipeline"
```

```bash
# Commit with body
git commit -m "feat(location): add KeyStore encryption

Implement Android KeyStore for secure key management.
Database passphrase is now encrypted with KeyStore key
before storing in SharedPreferences.

Resolves #123"
```

```bash
# Bad commits (avoid these)
git commit -m "updated files"           # Too vague
git commit -m "Fix bug"                 # No scope, no description
git commit -m "WIP"                     # Not descriptive
```

## Release Process

### Creating a Release

```bash
# From develop, create release branch
git checkout develop
git pull
git checkout -b release/1.0.0

# Update version in build.gradle.kts
# versionCode = 1
# versionName = "1.0.0"

# Commit version bump
git commit -m "chore: bump version to 1.0.0"

# Push release branch
git push origin release/1.0.0

# Create PR: release/1.0.0 → main
# After merge to main, tag the release
git checkout main
git pull
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# Merge back to develop
git checkout develop
git merge main
git push origin develop
```

### Hotfix Process

```bash
# From main, create hotfix branch
git checkout main
git pull
git checkout -b hotfix/critical-security-fix

# Make fix and commit
git commit -m "fix(security): patch XSS vulnerability"

# Create PR: hotfix/critical-security-fix → main
# After merge, tag and merge back to develop
```

## Code Review Guidelines

- All changes must go through pull request
- At least one approval required before merge
- CI/CD checks must pass (build, tests, lint)
- Keep PRs focused (one feature/fix per PR)
- Respond to review feedback promptly

## Continuous Integration

GitHub Actions runs automatically on:
- Every push to feature/bugfix branches
- Every pull request to develop/main
- Pipeline must pass before merge

CI checks:
- Code builds successfully
- All unit tests pass
- Lint checks pass
- Test reports generated
