Witch SMP Anticheat
====================

Overview
--------
This document describes the integrated Witch SMP Anticheat subsystem: design, checks, configuration, and operational guidance.

Architecture
------------
- Located in `relicbound-paper` under `com.relicbound.paper.anticheat`.
- Core components:
  - `AnticheatService` — lifecycle, config, scheduler, check runner.
  - `tracking` — ring-buffered snapshots for movement, clicks, combat, velocity.
  - `checks` — modular checks (Speed, Reach, etc.) following a shared `Check` interface.
  - `violations` — per-player violation state with decay and recent evidence.
  - `confidence` — multi-signal confidence accumulation to avoid single-signal accusations.
  - `alerts` / `announcements` — staff alerts and public announcements.
  - `api` — `WitchAnticheatAPI` for external integrations.
  - `logging` — async file logger under `plugins/WitchSMP/anticheat/logs/`.

Integration
-----------
- The anticheat registers listeners in `RelicboundPaperPlugin` and is accessible via `/ac`.
- Config lives in `plugins/WitchSMP/anticheat.yml` (auto-created on first run).

Operational Notes
-----------------
- Checks run on a bounded schedule to limit CPU usage.
- Violations decay over time; confidence increases with corroborating signals.
- Punishments are conservative: require both VL and confidence thresholds.

Files
-----
- `relicbound-paper/src/main/java/com/relicbound/paper/anticheat` — main implementation.
- `plugins/WitchSMP/anticheat.yml` — configuration (created at runtime).
- `plugins/WitchSMP/anticheat/logs/` — persisted logs.

Next Steps
----------
- Add additional checks (KillAura, AimAssist, Timer, Phase, world checks).
- Tune thresholds using live server replay or test server with controlled clients.

*** End of file
