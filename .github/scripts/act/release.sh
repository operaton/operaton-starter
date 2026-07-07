#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
WORKFLOW_FILE=".github/workflows/release.yml"

REQUIRED_SECRETS=(
  GITHUB_TOKEN
  GPG_SECRET_KEY
  GPG_PASSPHRASE
  MAVEN_CENTRAL_USERNAME
  MAVEN_CENTRAL_TOKEN
  NPM_TOKEN
  DOCKERHUB_USERNAME
  DOCKERHUB_TOKEN
)

usage() {
  cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Run the release GitHub Actions workflow locally via act.

Options:
  --next-version VALUE   Next development version (without -SNAPSHOT suffix).
                         Leave empty to let the workflow auto-compute it.
  --dry-run              Skip remote operations (default).
  --no-dry-run           Perform the real release.
  -h, --help             Show this help and exit.

Secrets are read from environment variables. If a .env file exists at the
repository root it is sourced first; environment variables take precedence.

Required environment variables:
  GITHUB_TOKEN, GPG_SECRET_KEY, GPG_PASSPHRASE,
  MAVEN_CENTRAL_USERNAME, MAVEN_CENTRAL_TOKEN,
  NPM_TOKEN, DOCKERHUB_USERNAME, DOCKERHUB_TOKEN
EOF
}

check_deps() {
  if ! command -v act &>/dev/null; then
    echo "Error: 'act' is not installed or not on PATH." >&2
    echo "Install it from: https://nektosact.com" >&2
    exit 1
  fi
}

load_env() {
  local env_file="${REPO_ROOT}/.env"
  if [[ ! -f "$env_file" ]]; then
    return 0
  fi
  echo "Loading secrets from ${env_file} (existing shell vars take precedence)..."
  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ "$line" =~ ^[[:space:]]*# ]] && continue
    [[ -z "${line//[[:space:]]/}" ]] && continue
    if [[ "$line" =~ ^([A-Za-z_][A-Za-z0-9_]*)=(.*)$ ]]; then
      local var_name="${BASH_REMATCH[1]}"
      local var_value="${BASH_REMATCH[2]}"
      # Strip surrounding single or double quotes
      var_value="${var_value%\"}"
      var_value="${var_value#\"}"
      var_value="${var_value%\'}"
      var_value="${var_value#\'}"
      # Strip trailing inline comment: remove ' #...' or '\t#...' suffix
      var_value="$(echo "$var_value" | sed 's/[[:space:]]*#.*$//')"
      # Only export if not already set in the environment
      if [[ -z "${!var_name+x}" ]]; then
        export "$var_name"="$var_value"
      fi
    fi
  done < "$env_file"
}

check_secrets() {
  local missing=()
  for secret in "${REQUIRED_SECRETS[@]}"; do
    if [[ -z "${!secret:-}" ]]; then
      missing+=("$secret")
    fi
  done
  if [[ ${#missing[@]} -gt 0 ]]; then
    echo "Error: The following required environment variables are not set:" >&2
    for var in "${missing[@]}"; do
      echo "  - $var" >&2
    done
    echo "" >&2
    echo "Set them in your shell or copy .env.template to .env and fill in the values." >&2
    exit 1
  fi
}

next_version=""
dry_run=""

parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --next-version)
        if [[ $# -lt 2 || "$2" == --* ]]; then
          echo "Error: --next-version requires a value." >&2
          exit 1
        fi
        next_version="$2"
        shift 2
        ;;
      --dry-run)
        dry_run="true"
        shift
        ;;
      --no-dry-run)
        dry_run="false"
        shift
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        echo "Error: Unknown argument: $1" >&2
        usage >&2
        exit 1
        ;;
    esac
  done
}

prompt_inputs() {
  if [[ -z "$next_version" ]]; then
    read -r -p "Next version [auto-compute]: " next_version || true
  fi

  if [[ -z "$dry_run" ]]; then
    local answer
    read -r -p "Dry run? [Y/n]: " answer || true
    answer="${answer:-Y}"
    if [[ "$answer" =~ ^[Nn] ]]; then
      dry_run="false"
    else
      dry_run="true"
    fi
  fi
}

run_act() {
  echo ""
  echo "Running release workflow via act..."
  echo "  next_version : ${next_version:-<auto-compute>}"
  echo "  dry_run      : ${dry_run}"
  echo ""

  local secret_file
  secret_file="$(mktemp)"
  chmod 600 "${secret_file}"
  trap 'rm -f "${secret_file}"' EXIT

  cat > "${secret_file}" <<EOF
GITHUB_TOKEN=${GITHUB_TOKEN}
GPG_SECRET_KEY=${GPG_SECRET_KEY}
GPG_PASSPHRASE=${GPG_PASSPHRASE}
MAVEN_CENTRAL_USERNAME=${MAVEN_CENTRAL_USERNAME}
MAVEN_CENTRAL_TOKEN=${MAVEN_CENTRAL_TOKEN}
NPM_TOKEN=${NPM_TOKEN}
DOCKERHUB_USERNAME=${DOCKERHUB_USERNAME}
DOCKERHUB_TOKEN=${DOCKERHUB_TOKEN}
EOF

  act workflow_dispatch \
    -W "${REPO_ROOT}/${WORKFLOW_FILE}" \
    --input next_version="${next_version}" \
    --input dry_run="${dry_run}" \
    --secret-file "${secret_file}"
}

main() {
  cd "${REPO_ROOT}"

  # parse_args first so --help works even when act is not installed
  parse_args "$@"

  if [[ ! -f "${WORKFLOW_FILE}" ]]; then
    echo "Error: Workflow file not found at ${REPO_ROOT}/${WORKFLOW_FILE}" >&2
    exit 1
  fi

  check_deps
  load_env
  check_secrets
  prompt_inputs
  run_act
}

main "$@"
