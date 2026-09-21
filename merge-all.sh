#!/bin/bash
set -e

branches=(
  kelvin-backend
  kansamba-backend
  edwin-backend
  katanga-ui
  mbasela-ui
  ben-ui
  agrippa-arch
  mainza-arch
  chibesa-arch
  abel-arch
  collins-sync
  mordecai-sync
  mapalo-sync
  salima-testing
  lamin-testing
  racheal-testing
)

git checkout main
git pull origin main

for b in "${branches[@]}"; do
  echo "=== Merging $b ==="
  git merge "$b" -m "Merge $b into main" || {
    echo "CONFLICT on $b — resolve manually"
    break
  }
done

git push origin main
echo "All merged and pushed"
