Witch SMP Anticheat - Tuning Notes

Tuning strategy
- Start with checks disabled or very permissive thresholds on a live server.
- Enable logging (`anticheat.logging.log-violations`) and staff alerts to collect real-world data.
- Use a small staff-only testing group to exercise edge cases (vehicles, pistons, elytra, laggy links).
- Gradually tighten thresholds where false-positive rate is near zero and detection rate rises.

Key parameters
- Per-check `threshold` and `punish-threshold` in `anticheat.checks.<name>`.
- `decay-per-second` controls how quickly VL disappears; higher values reduce long-term accumulation.
- `confidence-weight` controls how much a check contributes to the confidence model.
- Global `punishments.global-vl-threshold` and `punishments.confidence-threshold` combine for action.

Common adjustments
- Speed: increase `buffer` for servers with low TPS or network jitter.
- Reach: increase `buffer` for servers with higher latency or heavy entity movement.
- Timer: be conservative; these checks are high FPs on proxy setups.

*** End of file
