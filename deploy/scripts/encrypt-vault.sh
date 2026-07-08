#!/usr/bin/env bash
set -euo pipefail

VAULT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../ansible" && pwd)"
VAULT_PASS_FILE="${VAULT_DIR}/.vault_pass"

for f in "${VAULT_DIR}/inventory/group_vars"/vault_*.yml; do
    [ -f "$f" ] || continue
    if [ "$(head -1 "$f" 2>/dev/null)" != "" ] && [ "$(head -1 "$f" 2>/dev/null | cut -c1)" = '$' ]; then
        echo "Already encrypted: $(basename "$f")"
    else
        echo "Encrypting: $(basename "$f")"
        ansible-vault encrypt --vault-password-file "${VAULT_PASS_FILE}" "$f"
    fi
done

echo "Done."
