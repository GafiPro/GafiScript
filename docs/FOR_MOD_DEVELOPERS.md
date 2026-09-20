# For mod developers

GafiScript is a Fabric mod and its public Java API is deliberately plain Java.

Third-party Fabric mods can integrate by:
- importing GafiScript API classes;
- registering custom events;
- wrapping their own objects in script-facing adapters;
- exposing optional integrations through reflection when a dependency should remain optional.

Do not depend on internal runtime classes unless you are extending GafiScript itself.
