# Security model

GafiScript executes Java code supplied by users, so security is a first-class concern.

## Current defensive layers

1. source-level validation;
2. restricted class loading.

The source validator rejects obvious access to process execution, filesystem/network packages, reflection, internal JDK packages and selected dangerous runtime APIs.

The class loader also rejects selected dangerous classes and packages.

## Important limitation

These controls are not a formally verified sandbox.

Do not assume the current implementation can safely execute hostile code from an untrusted user.

A stronger isolation model and robust bytecode validation are required before GafiScript should be exposed to arbitrary untrusted scripts on a public server.

## Permissions

The initial Script Block editor and /gafiscript command require operator permission level 2.

## Rule

Never describe the current implementation as 100% secure. Security changes must add or update security tests.
