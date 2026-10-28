# Independent #140 review evidence

Reviewer/operator: Codex acting for XieKaiwen. Programmatic real-window Java-agent checks, not physical keyboard verification. Plain Oracle JDK 25.0.4.1+1-LTS-5, macOS 26.5.1 arm64, window 740×600. Actual screen geometry/scales are in transcripts. This does not validate Yujie's separate physical-input claims by reenacting them.

Published v1.4: source b848e8465f484a1e147609af4ed94b90144129b7, JAR SHA-256 cc5a5cc8becf94178d7ff780ba4c8a5b0028d55f58a5eb9db4c0640763912f5b.
Exact v1.5 candidate: source f6750f7f4d320e491507777e269c59a22be34549, JAR SHA-256 39b0db222e95ccbfc9dc882fb2274dd2b4da93aff13f270d879165576df6bcd6.
Both JAR hashes checked before/after. No JAR rebuild, patch or asset replacement.

Two separate production JVM launches per JAR: review140 and review140-restart. All four phases passed. The driver submits actual production command-box actions and reads the production feedback/list/profile. Screenshots, transcripts and the final fictional roster for each version are included.

Results:
- #167: find e948 shows Eve Ong, Zoe Lee, precomposed Émile Ng. Same on both JARs. This is the accepted locale-neutral ordering policy; locale-aware ordering remains an enhancement, not a fixed v1.5 defect.
- #168: added ordinary-space and NBSP Alex Tan profiles. Ordinary find Alex Tan returns only the ordinary-space profile. Composed/decomposed Émile queries select different recorded emails. Survives restart. Same on both JARs; open/unmilestoned, not claimed fixed.
- #169: literal Not assigned section/team accepted and stored; clearing the section removes one stored literal while the UI still displays Not assigned for absence. A later literal affiliation survives restart. Same on both JARs; explicit later compatibility policy, not scheduled v1.5 delivery.
- #170: 65-character local part rejected in add and edit on both JARs, but only v1.5 says 1 to 64. A 64-character local part is accepted. Rejected writes preserve file bytes. Fixed.
- #171: production view shows enrolments; direct live UG retrieval confirms the shipped-presentation sentence and absence of the obsolete separate-increment sentence. Previously verified final-head approval/CI/merge/Pages and live closure evidence remains on #171. Fixed.
- #172: edit-enrol returns only the context success sentence, while profile shows T14 and cleared team. Matches the UG; optional enhancement remains deferred, not fixed.

Review covers original report authorship/labels, linked environment/artifact/method evidence and recorded ownership/priority/reviewer/target dispositions. All six reports are by YujieJWang and labelled alpha-bug; suggestions are handled as suggestions. Related accent-order/search observations were not counted as separate invented defects. #140 report/retest delivery can close; #167/#168/#169/#172 and #142 platform acceptance remain separate. No assertion that all six reports were manually reproduced by a human, or that Windows/native Linux passed.
