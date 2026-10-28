# Exact v1.5 candidate retest for #139

Candidate source: f6750f7f4d320e491507777e269c59a22be34549.
JAR SHA-256: 39b0db222e95ccbfc9dc882fb2274dd2b4da93aff13f270d879165576df6bcd6; 12,923,199 bytes.
Used the draft-download-verified JAR, never rebuilt/replaced it. Hash checked again after testing.

Operator/method: Codex-operated Java-agent UI automation using the Alpha139Agent.java in the parent directory. Not personal/physical keyboard evidence. Plain Oracle Java 25.0.4.1+1-LTS-5, macOS 26.5.1 arm64; 740×600 window. Actual screen bounds/scales are recorded in each transcript. No Windows/native-Linux run.

Six distinct production JVM launches: explore, explore-restart, seed, restart, failure, recovery. All six phases passed. 83 command submissions recorded and 61 explicit unchanged-file byte assertions. Captured 14 screenshots.

Coverage: 35 invalid name/email/contact/structure inputs; min/max valid contact and email lengths; atomic rejected edits; contact clearing versus literal clear values; canonical email no-op; email/name replacement and old-identity retirement; pending-deletion cancellation by blank/unknown/view/find and mismatched confirmation; actual confirmed deletion; creation/contact edit and 100-character name rendering; surviving profiles and contacts reload after real JVM restart.

The failure phase used a real read-only disposable roster (chmod 444), not a storage mock. A production add failed with no saved-byte change and the previous selected profile remained visible. Restored owner-write permission afterwards. The recovery phase launched against real malformed JSON: production opened read-only, rejected mutation, allowed list, and retained malformed bytes through normal exit. Screenshots 08 and 09 were visually inspected.

This retest supports the exact artifact but is not #142 sign-off. It does not satisfy missing personal keyboard work or fabricate five alpha reports. #139 remains open for Spencer's review/disposition. No new non-duplicate defect was found in the scripted scenarios. Source changes require a new artifact identity and affected reruns. Draft unchanged and unpublished.
