# Release Guide

This document covers everything a maintainer needs to cut a new operaton-starter release.

## Prerequisites (One-Time Setup)

### 1. Claim the `org.operaton.dev` namespace at Sonatype Central

Before Maven Central publishing can succeed, the `org.operaton.dev` groupId must be claimed:

1. Create an account at [central.sonatype.com](https://central.sonatype.com)
2. Go to **Namespaces** → **Add Namespace**
3. Enter `org.operaton.dev`
4. Complete the verification (DNS TXT record or GitHub namespace claim)
   - See: [https://central.sonatype.org/register/namespace/](https://central.sonatype.org/register/namespace/)

This is a one-time step. Once verified, all releases under `org.operaton.dev` can publish without repeating it.

### 2. Generate required credentials

See the "GitHub Actions Secrets" table below for where to obtain each credential.

### 3. Add secrets to the GitHub repository

Go to **GitHub → Repository Settings → Secrets and variables → Actions** and add each secret from the table below.

---

## GitHub Actions Secrets

| Secret Name | What It Contains | How to Obtain | Enables |
|-------------|-----------------|---------------|---------|
| `DOCKERHUB_USERNAME` | Docker Hub account username | Docker Hub account settings | Docker image push |
| `DOCKERHUB_TOKEN` | Docker Hub access token (not password) | Docker Hub → Account Settings → Security → New Access Token | Docker image push |
| `MAVEN_CENTRAL_USERNAME` | Sonatype Central Portal user token username | central.sonatype.com → Account → Generate User Token | Maven Central publish |
| `MAVEN_CENTRAL_TOKEN` | Sonatype Central Portal user token password/token value | Same as above (token has separate username/password) | Maven Central publish |
| `GPG_PRIVATE_KEY` | Armored GPG private key (base64-encoded) | `gpg --armor --export-secret-keys KEY_ID \| base64` | Maven artifact signing |
| `GPG_PASSPHRASE` | GPG key passphrase | Set when generating the GPG key | Maven artifact signing |
| `NPM_TOKEN` | npm publish token | npmjs.com → Access Tokens → Generate New Token (Automation type) | npm package publish |
| `GITHUB_TOKEN` | Standard Actions token | Auto-provisioned by GitHub Actions — no setup needed | GitHub Release creation |

> **Note on `GITHUB_TOKEN`:** The release workflow requires `permissions: contents: write`. This is set in `.github/workflows/release.yml`. No manual secret is needed.

> **Note on `NPM_TOKEN`:** In the release workflow this is passed as `JRELEASER_NPM_TOKEN`. Use an **Automation** type token so 2FA doesn't block CI.

---

## Release Procedure

1. **Confirm main branch is green** — check the latest CI run on GitHub Actions.

2. **Tag the release:**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

3. **Monitor the release workflow:**
   - Go to GitHub → Actions → Release workflow
   - Watch all steps complete: Maven build → Docker build → npm pack → JReleaser full-release

4. **Verify artifacts:**
   - Docker Hub: `docker pull operaton/operaton-starter:1.0.0`
   - npm (MCP): `npm info operaton-starter-mcp version`
   - npm (CLI): `npm info operaton-starter version`
   - Maven Central: search `org.operaton.dev` at [central.sonatype.com](https://central.sonatype.com)
   - GitHub Release: confirm changelog and release notes at the repo releases page

---

## Dry Run (Validate Without Publishing)

To validate JReleaser configuration and credentials without actually publishing:

```bash
# Install JReleaser CLI (or use Docker)
curl -sL https://jreleaser.org/releases/latest/jreleaser-installer.sh | bash

# Set required env vars then run dry-run
export JRELEASER_GITHUB_TOKEN=ghp_...
export JRELEASER_GPG_SECRET_KEY=$(cat key.asc | base64)
export JRELEASER_GPG_PASSPHRASE=...
export JRELEASER_NPM_TOKEN=npm_...

jreleaser full-release --dry-run
```

JReleaser will validate all credentials and configuration, print what it would do, and exit without publishing anything.

---

## Troubleshooting

**GPG signing fails in CI:**
- Ensure `GPG_PRIVATE_KEY` is the full armored key including `-----BEGIN PGP PRIVATE KEY BLOCK-----` header, base64-encoded
- The release workflow imports the key with `echo "$JRELEASER_GPG_SECRET_KEY" | base64 -d | gpg --import` — verify this matches your key format
- The `maven-gpg-plugin` uses `--pinentry-mode loopback` to read the passphrase non-interactively

**Maven Central upload rejected:**
- Check the namespace claim is verified at `central.sonatype.com` (ARCH-14 prerequisite)
- Confirm `-sources.jar` and `-javadoc.jar` are present — build with `mvn verify -Prelease`
- Confirm all artifacts are GPG-signed (look for `.asc` files in `target/`)

**npm publish fails with 401/403:**
- Use an **Automation** type npm token — not a Publish token that requires 2FA
- Confirm the `operaton-starter-mcp` and `operaton-starter` packages are either new or your account has publish rights to them

**Docker push fails:**
- Confirm `DOCKERHUB_USERNAME` is the account owner (not org name) of the `operaton/operaton-starter` repository
- The `operaton/operaton-starter` Docker Hub repository must exist and the token must have Read & Write scope
