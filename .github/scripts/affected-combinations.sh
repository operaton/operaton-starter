#!/usr/bin/env bash
# Determines which project-type × build-system combinations are affected by
# changed JTE template files in a PR, and outputs a GitHub Actions matrix JSON.
#
# Usage: affected-combinations.sh [<base-ref>]
#   base-ref defaults to origin/main
#
# Output (to stdout): JSON array for use as a GitHub Actions matrix.
# If no combinations are affected, outputs an empty array [].
# All 6 MVP combinations are output when a shared/common template changes.
#
# Template directory → combination mapping:
#   common/                            → all 6 combinations
#   process-application/ (top-level)   → PROCESS_APPLICATION × all 3 build systems
#   process-application/maven/         → PROCESS_APPLICATION × MAVEN
#   process-application/gradle-groovy/ → PROCESS_APPLICATION × GRADLE_GROOVY
#   process-application/gradle-kotlin/ → PROCESS_APPLICATION × GRADLE_KOTLIN
#   process-archive/ (top-level)       → PROCESS_ARCHIVE × all 3 build systems
#   process-archive/maven/             → PROCESS_ARCHIVE × MAVEN
#   process-archive/gradle-groovy/     → PROCESS_ARCHIVE × GRADLE_GROOVY
#   process-archive/gradle-kotlin/     → PROCESS_ARCHIVE × GRADLE_KOTLIN
#   dmn-project/ spike/ (other)        → not in MVP matrix; ignored
#
set -euo pipefail

BASE_REF="${1:-origin/main}"
TEMPLATE_PREFIX="starter-templates/src/main/jte/"

# Collect changed JTE files
changed_files=$(git diff --name-only "${BASE_REF}...HEAD" 2>/dev/null || \
                git diff --name-only HEAD~1 2>/dev/null || echo "")

# Filter to only JTE template files
template_changes=$(echo "$changed_files" | grep "^${TEMPLATE_PREFIX}" || true)

if [ -z "$template_changes" ]; then
  echo "[]"
  exit 0
fi

# Accumulate needed combinations as newline-separated strings (bash 3.x compatible)
needed=""

add() {
  local combo="$1"
  if ! echo "$needed" | grep -qxF "$combo"; then
    needed="${needed}${combo}"$'\n'
  fi
}

add_all() {
  add "PROCESS_APPLICATION/MAVEN"
  add "PROCESS_APPLICATION/GRADLE_GROOVY"
  add "PROCESS_APPLICATION/GRADLE_KOTLIN"
  add "PROCESS_ARCHIVE/MAVEN"
  add "PROCESS_ARCHIVE/GRADLE_GROOVY"
  add "PROCESS_ARCHIVE/GRADLE_KOTLIN"
}

add_pa_all() {
  add "PROCESS_APPLICATION/MAVEN"
  add "PROCESS_APPLICATION/GRADLE_GROOVY"
  add "PROCESS_APPLICATION/GRADLE_KOTLIN"
}

add_pac_all() {
  add "PROCESS_ARCHIVE/MAVEN"
  add "PROCESS_ARCHIVE/GRADLE_GROOVY"
  add "PROCESS_ARCHIVE/GRADLE_KOTLIN"
}

while IFS= read -r file; do
  [ -z "$file" ] && continue
  rel="${file#${TEMPLATE_PREFIX}}"

  case "$rel" in
    common/*)
      add_all ;;
    process-application/maven/*)
      add "PROCESS_APPLICATION/MAVEN" ;;
    process-application/gradle-groovy/*)
      add "PROCESS_APPLICATION/GRADLE_GROOVY" ;;
    process-application/gradle-kotlin/*)
      add "PROCESS_APPLICATION/GRADLE_KOTLIN" ;;
    process-application/*)
      add_pa_all ;;
    process-archive/maven/*)
      add "PROCESS_ARCHIVE/MAVEN" ;;
    process-archive/gradle-groovy/*)
      add "PROCESS_ARCHIVE/GRADLE_GROOVY" ;;
    process-archive/gradle-kotlin/*)
      add "PROCESS_ARCHIVE/GRADLE_KOTLIN" ;;
    process-archive/*)
      add_pac_all ;;
    dmn-project/* | spike/*)
      ;;
    *)
      add_all ;;
  esac
done <<< "$template_changes"

# Filter empty lines
needed=$(echo "$needed" | grep -v '^$' || true)

if [ -z "$needed" ]; then
  echo "[]"
  exit 0
fi

# Build GitHub Actions matrix JSON
items=""
while IFS= read -r combo; do
  [ -z "$combo" ] && continue
  project_type="${combo%%/*}"
  build_system="${combo##*/}"
  pt_label=$(echo "$project_type" | tr '[:upper:]_' '[:lower:]-')
  bs_label=$(echo "$build_system" | tr '[:upper:]_' '[:lower:]-')
  entry="{\"project-type\":\"${pt_label}\",\"build-system\":\"${bs_label}\",\"projectType\":\"${project_type}\",\"buildSystem\":\"${build_system}\"}"
  if [ -z "$items" ]; then
    items="$entry"
  else
    items="${items},${entry}"
  fi
done <<< "$needed"

echo "[${items}]"
