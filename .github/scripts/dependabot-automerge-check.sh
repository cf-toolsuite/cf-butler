#!/usr/bin/env bash
#
# Decide whether a Dependabot PR is ready for auto-merge.
#
# Single source of truth for the auto-merge gate, shared by the event-driven
# job and the scheduled sweep in dependabot-auto-merge.yml.
#
# Usage:
#   GH_TOKEN=<token> dependabot-automerge-check.sh <owner/repo> <pr-number>
#
# Prints one word on stdout:
#   ready               - eligible Dependabot, non-draft, open, "Merge Ready" green,
#                         and all branch-protection required checks green.
#   not-dependabot      - authored by someone other than dependabot[bot]
#   draft               - PR is a draft
#   not-open            - PR is not open
#   merge-ready-pending - the "Merge Ready" check has not concluded success yet
#   checks-pending      - a branch-protection required check is pending or failing
#
set -euo pipefail

REPO="${1:?owner/repo required}"
PR="${2:?pr number required}"

pr_json=$(gh api "repos/${REPO}/pulls/${PR}")
author=$(printf '%s' "$pr_json" | jq -r '.user.login')
draft=$(printf '%s' "$pr_json" | jq -r '.draft')
state=$(printf '%s' "$pr_json" | jq -r '.state')

if [ "$author" != "dependabot[bot]" ]; then echo "not-dependabot"; exit 0; fi
if [ "$draft" = "true" ]; then echo "draft"; exit 0; fi
if [ "$state" != "open" ]; then echo "not-open"; exit 0; fi

head_sha=$(gh pr view "$PR" --repo "$REPO" --json headRefOid --jq '.headRefOid')

# The "Merge Ready" job only runs on pull_request-triggered CI, and only there can it
# conclude "success". The push-triggered CI run reports the same check name as
# "skipped", so we must require an actual "success" conclusion rather than trusting
# whichever check happened to be reported last by start time.
merge_ready_success=$(gh api "repos/${REPO}/commits/${head_sha}/check-runs" \
  -H "Accept: application/vnd.github+json" \
  --jq '[.check_runs[] | select(.name == "Merge Ready" and .conclusion == "success")] | length')

if [ "$merge_ready_success" -lt 1 ]; then
  echo "merge-ready-pending"
  exit 0
fi

# Branch-protection required checks must all be green (or none configured).
if checks_out="$(gh pr checks "$PR" --repo "$REPO" --required 2>&1)"; then
  :
elif printf '%s' "$checks_out" | grep -q "no required checks reported"; then
  :
else
  echo "checks-pending"
  exit 0
fi

echo "ready"
