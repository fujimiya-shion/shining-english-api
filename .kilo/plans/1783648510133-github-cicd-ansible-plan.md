# GitHub CI/CD with Ansible — Shining English API

## Trigger & Image Tag Strategy

| Event | CI Build + Push | Image Tag(s) | CD Deploy |
|---|---|---|---|
| Push `main` | ✅ | `sha-{GITHUB_SHA::7}` | ❌ |
| Push tag `v*` | ✅ | `{GIT_TAG}`, `latest` | ✅ |

## Workflow Files to Create

### 1. `.github/workflows/ci.yml` — Continuous Integration

- **Trigger:** `push: { branches: [main], tags: [v*] }`
- **Steps:**
  1. Checkout code
  2. Set up JDK 21 (temurin)
  3. Cache Gradle dependencies
  4. Build JAR (`./gradlew bootJar --no-daemon`)
  5. Run tests (`./gradlew test --no-daemon`)
  6. Set up Docker Buildx
  7. Login to DockerHub (secrets: `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`)
  8. Compute image tags (see matrix above)
  9. Build & push multi-platform image using `docker/prod/Dockerfile`
  10. If tag push, also tag + push `latest`

### 2. `.github/workflows/cd.yml` — Continuous Deployment

- **Trigger:** `workflow_run: { workflows: [CI], types: [completed], branches: [v*] }`  
  *or simpler:* on tag push, accept `APP_TAG` input triggered after CI completes.
- **Better approach:** combine CI + CD in one workflow with conditional job.
  - One workflow file: `.github/workflows/ci-cd.yml`
  - **Job 1 — build** (always runs): builds and pushes Docker image
  - **Job 2 — deploy** (only on tag push, needs Job 1): runs Ansible deploy

### 3. Secrets / Variables (GitHub Actions)

| Secret Name | Purpose |
|---|---|
| `ANSIBLE_VAULT_PASSWORD` | Decrypt Ansible vault files (contains DockerHub creds, DB creds, etc.) |
| `SSH_PRIVATE_KEY` | SSH key to connect to app server |
| `KNOWN_HOSTS` | SSH known_hosts entry for server(s) — output of `ssh-keyscan <server-ip>` |
| `ANSIBLE_INVENTORY` | Content of `deploy/ansible/inventory/production.yml` (gitignored) |

### 4. Ansible Execution from Runner

- Install `ansible` via pip in the Actions runner.
- Write vault password from `ANSIBLE_VAULT_PASSWORD` secret to a temp file (`.vault_pass`).
- Write inventory from `ANSIBLE_VAULT_PASSWORD` to `.kilo/plans/1783648510133-github-cicd-ansible-plan.md`.
  - *Wait, that's the wrong file. Let me fix:*
- Write inventory from `ANSIBLE_INVENTORY` secret to `deploy/ansible/inventory/production.yml`.
- Setup SSH agent with `SSH_PRIVATE_KEY` and `KNOWN_HOSTS`.
- Run:
  ```bash
  ansible-playbook \
    -i deploy/ansible/inventory/production.yml \
    deploy/ansible/playbooks/deploy-app.yml \
    --vault-password-file .vault_pass \
    -e APP_TAG="${GIT_TAG}"
  ```

## Implementation Checklist

- [ ] Create `.github/workflows/ci-cd.yml`
  - [ ] Job `build`: Gradle build + test + Docker build & push
  - [ ] Job `deploy` (needs build, if tag push): Ansible deploy
- [ ] Add DockerHub login step using `docker/login-action@v3`
- [ ] Add step to persist `.vault_pass` and `production.yml` from secrets
- [ ] Add SSH agent setup step
- [ ] Add `ansible-playbook` execution step
- [ ] Update `.gitignore` if needed (ensure no secrets leaked)

## Validation

1. Push a non-tag commit to `main`: only CI runs, image tagged `sha-xxxxx`
2. Push tag `v0.1.0-test`: CI builds + pushes `v0.1.0` and `latest`, CD runs deploy with `APP_TAG=v0.1.0`
3. Verify DockerHub has the expected tags
4. Verify app server pulls and runs the new image

## Risks & Notes

- **Ansible vault password**: must be stored as GitHub secret; the `.vault_pass` file is written at runtime and never persisted.
- **SSH key**: use a dedicated deploy key (read-only SSH key) stored as GitHub secret.
- **Inventory**: `production.yml` is gitignored because it contains IPs. Store full content as GitHub secret.
- **Test before tag push**: The workflow does NOT enforce any PR/approval gating. Consider branch protection rules on `main`.
