# Issue #139: published v1.4 supplemental alpha evidence

Operator: Codex acting for Kaiwen. Method: Java agent UI automation, not personal/manual keyboard testing. No personal course credit or five-report completion is claimed.

Artifact: published v1.4, source b848e8465f484a1e147609af4ed94b90144129b7; addressbook.jar 12,919,205 bytes; SHA-256 cc5a5cc8becf94178d7ff780ba4c8a5b0028d55f58a5eb9db4c0640763912f5b. The copied JAR's checksum was verified before and after testing; it was not rebuilt or modified. Only fictional data was used.

Environment: macOS 26.5.1 (25F80), arm64 Apple M5; plain Oracle JDK 25.0.4.1+1-LTS-5. Displays: built-in 3024×1964 Retina, external LG 3840×2160 with UI looking like 1920×1080. Actual JavaFX screen bounds/output scales are recorded in every transcript. Window set to 740×600. No physical scaling configuration was changed or certified.

Launched in an empty writable application folder using Java 25 with `-javaagent:...=seed -jar addressbook.jar`. Subsequent restart/failure/recovery phases were separate production JVM launches of the same JAR. Agent source is included; it submits actual command-box ActionEvents and records screenshots/feedback. It does not replace production storage, models or commands.

## Executed outcomes

- Seed: empty startup created no roster. Created a student with mixed-case NUS email and both contacts. Duplicate with canonical-equivalent email rejected; file bytes unchanged. Missing email and invalid-domain input rejected; bytes unchanged.
- Boundary/profile: a 100-character name was accepted and wrapped in the list/profile screenshots. Contact edit updated Telegram and GitHub and selected the intended profile.
- Deletion: preview saved nothing; `find Alpha` cancelled it. Confirmation without a preview was rejected and saved nothing. Fresh preview + matching confirmation removed exactly the intended profile. Surviving long-name profile remained visible.
- Restart: a separate JVM loaded the survivor; the confirmed-deleted profile was absent from JSON and UI.
- Real failed save: copied the valid roster, used `chmod a-w data/addressbook.json`, launched a separate JVM and attempted student creation. Actual result: `The student could not be saved. No data was changed.` The prior selected survivor stayed visible and saved bytes were unchanged. Restored owner write permission afterwards. This was an actual filesystem-permission failure, not an injected storage mock.
- Invalid-file recovery: replaced only the disposable roster with malformed JSON and launched a separate JVM. Production opened an empty read-only recovery view, rejected mutation with the read-only message, and allowed `list`. Malformed file bytes were unchanged after normal exit.

All four phase transcripts ended PASS. Valid file before/after failed save: SHA-256 0903c53011be252b91d504cd08c76cd82cf85a63c8e080285cf54094f88cde7c. Malformed file before/after recovery/exit: SHA-256 7425dae32a5b01e9fbc982b06a93a6ee16aa9d685fc5872a80e2c5c3960abf81.

## Limits and remaining criteria

The automated driver recorded a null scene focus owner during this run. It did not establish physical keyboard/focus behavior, Tab traversal or personal manual participation. A screenshot can show rendered text but does not certify all resolutions/platforms. Windows/native Linux were not tested here.

GitHub currently shows zero alpha-bug reports authored by XieKaiwen. The duplicate-feedback and narrow-window recovery-text observations overlap existing reports; no redundant reports were filed. This evidence does not satisfy the five genuine personal-report requirement. Spencer must review the evidence and the remaining criteria/shortfall. #139 stays open; #135 is not unblocked by this supplemental report.


## Broadened assistant-operated exploration — 28 October

Following the user's instruction to test it ourselves, ran another clean-folder published-v1.4 exploration plus a real second-JVM restart. Same artifact/checksum and plain JDK; no production classes were rebuilt or patched. See `explore-transcript.txt`, `explore-restart-transcript.txt` and screenshots 11–14. This remains assistant-operated Java-agent UI testing.

Executed 65 commands across the two launches, including 35 invalid input cases and 52 byte-preservation assertions:

- Missing/empty/101-character/slash-containing names; malformed, wrong-domain, double-dot, bad-edge and 65-character-local-part emails.
- Telegram below 5/above 32 characters, bad leading digit, repeated @ and empty supplied value; GitHub above 39 characters, bad edges, consecutive hyphens, leading @, underscore and empty value.
- Repeated/unknown prefixes and unexpected preamble; missing/invalid/extra-argument view/delete requests.
- Valid 32-character Telegram and 39-character GitHub, both explicit contact clears, and literal `@clear`/`Clear` values.
- Case-only canonical-email no-op without save; duplicate identity editing rejected atomically without applying a simultaneously supplied name; successful email/name change retires old identity and normalises ordinary spaces.
- Pending deletion cancelled by unknown, blank, view and find commands; each following confirmation rejected. Mismatched confirmation also consumed the pending action without deletion.
- Valid 64-character email local part, 5-character Telegram and 1-character GitHub.
- Second-JVM restart retained the new identity, literal contacts and unaffected second profile; old identity absent.

Every scripted expectation passed. No new non-duplicate defect emerged. Existing observations (duplicate feedback, read-only-message layout, command argument handling) were not split into redundant reports. Five genuine new reports are not claimed.

An additional uninstrumented `java -jar` launch was attempted for direct desktop input. The native computer-control API could not bind to Java by process name or executable path; no keyboard or mouse actions reached that window and no direct-keyboard pass is claimed. The unused disposable launch was stopped. The automated phases above did execute the real production window and storage.
