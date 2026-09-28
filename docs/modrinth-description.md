# Focus Crosshair

**A little motion. A clearer focus.**

Focus Crosshair replaces the vanilla crosshair with a compact, smoothly animated reticle that responds to what you look at and what you do. Four clean segments, an optional center dot, soft neutral colors, and subtle motion that stays out of the way.

![Focus Crosshair in game](https://raw.githubusercontent.com/wuttashi1/focus-crosshair/main/docs/assets/gameplay.gif)

*Other visual mods and resource packs shown in the screenshot are not included.*

## Small details that make a difference

- **Contextual focus:** the crosshair gently tightens over blocks and entities, with a distinct, restrained state for supported interactable blocks.
- **Spring animation:** scale, gap, opacity and visual effects respond smoothly over time.
- **Movement response:** subtle reactions to sprinting, sneaking, jumping, landing, swimming and elytra flight.
- **Combat feedback:** short attack and damage pulses, plus a small confirmation pulse when the server reports damage attributed to you.
- **Mining progress:** a thin, 16-segment indicator follows actual client block-breaking progress.
- **Item use:** gentle responses to eating, drinking, bow and crossbow charging, shields and other use actions.
- **Your preferred look:** adjust colors, opacity, size, line thickness, spacing and animation strength. Disable individual context effects or the center dot.

Optional low-health animation is deliberately subtle. Idle breathing is off by default.

## Purely visual

Focus Crosshair does not change your aim, camera, mouse input, reach, hitboxes or targeting. It sends no packets and needs no server-side installation. Visual focus uses segment compression; the center dot stays at the true aiming point.

The vanilla attack-cooldown indicator is preserved. The crosshair respects first-person and spectator visibility, F1, and the debug crosshair, and hides in menus.

## Installation

Requires **Minecraft Java 26.2**, **Fabric Loader 0.19.5+**, **Fabric API for 26.2** and **Java 25+**.

Place the mod JAR in your `mods` folder. Install **Mod Menu** optionally to access the settings screen. The mod works without it.

## Configuration

Open the mod's settings through Mod Menu, or edit `config/focuscrosshair.json` while Minecraft is closed. In-game settings apply immediately and save when you leave the screen.

Assign **Toggle Focus Crosshair** under **Controls → Key Binds → Focus Crosshair**. It is unbound by default. Disable the mod to restore the previous crosshair renderer.

English, German and Russian UI translations are included.

## Compatibility notes

Built and launched on **26.2**. Later 26.x versions are not yet verified. Rendering uses Minecraft's GUI abstractions without direct OpenGL calls; Vulkan and combinations with Sodium, Iris, Spatial GUI or other HUD mods still need separate testing. Another crosshair replacement may take priority.

Some modded block interactions may use ordinary block focus. Hit confirmation requires server damage events attributed to the local player.

[Source code](https://github.com/wuttashi1/focus-crosshair) · [Report an issue](https://github.com/wuttashi1/focus-crosshair/issues)
