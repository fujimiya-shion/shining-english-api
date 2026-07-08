#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANSIBLE_DIR="${SCRIPT_DIR}/../ansible"

if [ ! -f "${ANSIBLE_DIR}/.vault_pass" ]; then
    echo "Error: .vault_pass not found. Create it with the vault password."
    exit 1
fi

if [ ! -f "${ANSIBLE_DIR}/inventory/production.yml" ]; then
    echo "Error: inventory/production.yml not found. Copy from production.example.yml and configure."
    exit 1
fi

echo "Setting up app server..."
cd "${ANSIBLE_DIR}"

ansible-playbook playbooks/setup-app-server.yml -v

echo "Setup complete."
