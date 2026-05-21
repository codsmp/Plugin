#!/usr/bin/env bash
set -euo pipefail

# Usage: ./scripts/set-branch-protection.sh [owner/repo] [branch]
# Example: ./scripts/set-branch-protection.sh codsmp/Plugin main

REPO=${1:-codsmp/Plugin}
BRANCH=${2:-main}

echo "Setting branch protection for ${REPO} branch ${BRANCH}"

read -r -d '' PAYLOAD <<JSON || true
{
  "required_status_checks": null,
  "enforce_admins": false,
  "required_pull_request_reviews": {
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": false,
    "bypass_pull_request_allowances": {
      "users": ["Falthera"],
      "teams": [],
      "apps": []
    }
  },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false
}
JSON

# Apply protection (requires gh CLI authenticated with repo admin rights)
gh api --method PUT "/repos/${REPO}/branches/${BRANCH}/protection" --input - <<EOF
${PAYLOAD}
EOF

echo "Branch protection updated. Note: setting 'enforce_admins': false allows repository administrators (including organization owners) to bypass protections. The explicit bypass user is 'Falthera'."
