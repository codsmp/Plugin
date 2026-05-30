False-Positive Risk Analysis

High-risk checks
- Timer/Packet-rate: may flag players using VPNs, laggy connections, or aggressive client-side optimization.
- Phase/Clip: pistons, moving blocks, and boats can produce edge cases.

Mitigations
- Use confidence model requiring multiple signals.
- Decay and buffer violations to avoid single-event punishments.
- Add world/role exemptions via config (`anticheat.general.op-bypass` and future world lists).
- Provide `/ac debug` and verbose logs to investigate and adjust thresholds.

Operational guidance
- Never enable aggressive punishments immediately; collect logs first.
- Use staff alerts with verbose evidence to manually confirm before taking punitive actions.

*** End of file
