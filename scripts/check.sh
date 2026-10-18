#!/usr/bin/env bash
set -euo pipefail

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
project_root="$(CDPATH= cd -- "$script_dir/.." && pwd)"
java_bin="java"
if [[ -n "${JAVA_HOME:-}" ]]; then
    java_bin="$JAVA_HOME/bin/java"
fi
if ! java_info="$("$java_bin" -version 2>&1)"; then
    printf '%s\n' 'A working JDK 21 or newer is required. Set JAVA_HOME to the installed JDK.' >&2
    exit 2
fi
java_pattern='version "([0-9]+)'
if [[ ! "$java_info" =~ $java_pattern ]]; then
    printf '%s\n' 'Cannot determine the Java version. Set JAVA_HOME to a compatible JDK.' >&2
    exit 2
fi
if (( BASH_REMATCH[1] < 21 )); then
    printf '%s\n' 'JDK 21 or newer is required. Set JAVA_HOME; the machine default is not changed.' >&2
    exit 2
fi
cd "$project_root"
exec ./mvnw -B -ntp verify -Ppostgres-it "$@"
