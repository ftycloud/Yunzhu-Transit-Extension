# Task for worker

You are a delegated subagent running from a fork of the parent session. Treat the inherited conversation as reference-only context, not a live thread to continue. Do not continue or answer prior messages as if they are waiting for a reply. Your sole job is to execute the task below and return a focused result for that task using your tools.

Task:
You need to modify 11 Java render files in the project at E:/Projects/Yunzhu-Transit-Extension. Each file has an old complex lantern logic block that needs to be replaced with a new unified API call.

**Context**: The project already has `LiftButtonsBase.LanternState` and `LiftButtonsBase.LanternPhase` types, and `BlockEntityBase.getLanternState(World, BlockPos)` method. The render class calls `blockEntity.getLanternState(world, trackPosition)` to get lantern state.

**Replacement template** (adapt per file):

```java
LiftButtonsBase.LanternState state = blockEntity.getLanternState(world, trackPosition);
// If file uses flash:
final boolean useFlash = (state.phase == LiftButtonsBase.LanternPhase.CALL_REGISTERED && enableCallFlash)
        || ((state.phase == LiftButtonsBase.LanternPhase.APPROACHING
             || state.phase == LiftButtonsBase.LanternPhase.ARRIVED) && enableApproachFlash);
if (state.downActive && (!useFlash || flash)) { DOWN_LIGHTS.activate(); }
if (state.upActive && (!useFlash || flash)) { UP_LIGHTS.activate(); }
if (state.justTriggered) { /* SOUND if any */ }
```

**Files to process (all paths relative to E:/Projects/Yunzhu-Transit-Extension):**

Group A: Simple - no flash, no sound, has doorValue+pressedButtonDirection check
1. fabric/src/main/java/top/xfunny/mod/client/render/RenderTestLiftHallLanterns.java
   - hasButtonsClient class: TestLiftHallLanternsEven
   - down light: buttonDownLight.activate();
   - up light: buttonUpLight.activate();
   - enableCallFlash=false, enableApproachFlash=false, no sound

2-5. fabric/src/main/java/top/xfunny/mod/client/render/RenderOtisSeries1Lantern{1,1Horizontal,2,2Horizontal}.java
   - All 4 have same structure: downLantern/middleLantern for DOWN, upLantern/middleLantern for UP
   - hasButtonsClient class: OtisSeries1Lantern{1,1Horizontal,2,2Horizontal}Even
   - enableCallFlash=false, enableApproachFlash=false, no sound

Group B: Complex - has flash but no active sound (sound is commented out)
6-9. fabric/src/main/java/top/xfunny/mod/client/render/RenderOtisSeries1LanternScreen{1,1Horizontal,2,2Horizontal}.java
   - Read each file to determine hasButtonsClient class, light names, flash config
   - Sound is likely commented out

10. fabric/src/main/java/top/xfunny/mod/client/render/RenderOtisSeries3Lantern1Arrow.java
    - Read file for exact config

11. fabric/src/main/java/top/xfunny/mod/client/render/RenderSchindlerMSeriesRoundLantern1.java
    - Read file for exact config

**For EACH file:**
1. Read the file to find the old code block (starts around "final boolean flash" or "instructionDirections" area, ends after the closing "});" of the callback)
2. Determine: hasButtonsClient class name, down/up light variable names, flash config (check if flash=true for CALL_REGISTERED only or all phases), sound ID (if active - not commented out)
3. Construct the new code using the template
4. Use the edit tool to replace oldText with newText
5. Report what you changed

**IMPORTANT**: Read each file first before editing. The oldText must match exactly. Make SURE you read the full old code block including all switch/case statements.

## Acceptance Contract
Acceptance level: checked
Completion is not accepted from prose alone. End with a structured acceptance report.

Criteria:
- criterion-1: Implement the requested change without widening scope

Required evidence: changed-files, tests-added, commands-run, residual-risks, no-staged-files

Finish with a fenced JSON block tagged `acceptance-report` in this shape:
Use empty arrays when no items apply; array fields contain strings unless object entries are shown.
`criteriaSatisfied[].status` must be exactly one of: satisfied, not-satisfied, not-applicable.
`commandsRun[].result` must be exactly one of: passed, failed, not-run.
`manualNotes` and `notes` are optional strings; an empty string means no note and does not satisfy `manual-notes` evidence.
```acceptance-report
{
  "criteriaSatisfied": [
    {
      "id": "criterion-1",
      "status": "satisfied",
      "evidence": "specific proof"
    }
  ],
  "changedFiles": [
    "src/file.ts"
  ],
  "testsAddedOrUpdated": [
    "test/file.test.ts"
  ],
  "commandsRun": [
    {
      "command": "command",
      "result": "passed",
      "summary": "short result"
    }
  ],
  "validationOutput": [
    "validation output or concise summary"
  ],
  "residualRisks": [
    "none"
  ],
  "noStagedFiles": true,
  "diffSummary": "short description of the diff",
  "reviewFindings": [
    "blocker: file.ts:12 - issue found, or no blockers"
  ],
  "manualNotes": "anything else the parent should know"
}
```