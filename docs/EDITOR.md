# GafiScript editor

The editor lives inside Minecraft.

## Current features

- multi-line editing;
- line numbers;
- Java keyword highlighting;
- indentation;
- cursor movement;
- backspace/delete;
- copy/paste;
- Ctrl+S to save;
- F5 to run;
- Ctrl+Space for completion;
- basic API completion;
- problem panel;
- block coordinates shown in the editor.

## Completion

Typing Gafi. offers the currently indexed Gafi API.

Typing Gafi.scheduler(). offers scheduler operations.

## Diagnostics

The first diagnostic layer is intentionally lightweight. It catches common mistakes locally while the real Java compiler remains the authority for compilation errors.

## Shortcuts

- Ctrl+S: save
- F5: run
- Ctrl+Space: completion

Real text selections, semantic Java completion, hover documentation, go-to-definition and a complete formatter remain future editor modules.
