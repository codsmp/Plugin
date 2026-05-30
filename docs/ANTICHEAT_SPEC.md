Witch SMP Anticheat - Detection Specification

Included Checks (initial):
- Speed (movement)
  - Detects ground/air speed exceeding a conservative allowed speed after TPS/lag compensation.
  - Uses movement snapshots and per-tick delta distances.
  - Adds VL proportionally to the excess distance; contributes a confidence signal.

- Reach (combat)
  - Uses combat snapshots (historical interpolated positions) to measure attack distance.
  - Considers sprinting bonus and configured buffer.
  - Adds VL proportional to distance over allowed range; contributes confidence.

Design principles for future checks:
- Use historical snapshots and interpolation; avoid single-packet heuristics.
- Apply TPS and ping compensation before making comparisons.
- Buffer and decay VL to prevent flapping.
- Confidence requires multiple independent signals before public actions.

Evidence and staff logs:
- Each VL entry stores a small evidence map (numeric values and small descriptors) for staff review.
- `/ac debug <player>` exposes recent snapshot counts, VL, and confidence for triage.

*** End of file
