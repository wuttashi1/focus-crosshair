<p align="center"><img src="docs/assets/logo.png" width="128" alt="Focus Crosshair logo"></p>

# Focus Crosshair

**A little motion. A clearer focus.**

[Download JAR](https://github.com/wuttashi1/focus-crosshair/releases/latest) · [Русский](README.ru.md) · [Report an issue](https://github.com/wuttashi1/focus-crosshair/issues)

A small, purely visual, spring-animated crosshair for **Minecraft Java 26.2+**, **Fabric**, and **Java 25**. Client-side only.

![Focus Crosshair gameplay animation](docs/assets/gameplay.gif)

*Gameplay captured by the author. The animation loops automatically. Other visual mods and resource packs visible in the recording are not included.*

## Features

- Compact four-segment crosshair with an optional fixed center dot and restrained configurable colors.
- Block, interactable-block, entity and living-entity focus, combined with movement and item-use influences.
- Damped spring animation, subtle camera-motion inertia, attack, confirmed-hit, interaction and damage pulses.
- Actual client mining progress in a thin 16-segment indicator; bow/crossbow charging, eating, drinking, shields and other use actions.
- Optional subtle low-health pulse; idle breathing is off by default.
- Native settings with live appearance preview, optional Mod Menu integration, and English, German and Russian UI.
- Unbound toggle key in Controls → Key Binds → Focus Crosshair. Disabling restores the previous crosshair renderer.

No targeting, reach, hitbox, camera, mouse-input or gameplay changes. No packets, world scanning, shaders, textures, sound assets or background threads added by this mod.

## Installation

Install Fabric Loader **0.19.5+** and Fabric API **0.161.0+26.2** for Minecraft **26.2**, using Java **25+**. Place `focus-crosshair-1.0.0.jar` in your instance's `mods` directory. The server needs nothing. Mod Menu **20.0.3** is optional.

## Configuration

`config/focuscrosshair.json` is created on first launch. All options can be changed through Mod Menu's configuration button. General, Appearance, Animation and Context Effects sections use vanilla controls; color buttons open alpha/red/green/blue sliders. Changes apply immediately and save on exit. Reset restores all defaults.

Without Mod Menu, edit the JSON while the game is closed. Colors use `#AARRGGBB`. Numeric settings are clamped; malformed files are preserved as `.broken-<timestamp>` and replaced with defaults. The toggle also saves immediately.

Visual focus strength is limited to 0–6; camera inertia is limited to 0–2 GUI units. Visual magnetism uses centered asymmetric segment compression, not world-to-screen projection or displacement of the aiming point. The center dot stays exactly centered. The progress ring uses supported GUI rectangles rather than backend-specific circle rendering.

## Compatibility

Built against unobfuscated Minecraft **26.2**, Fabric Loom **1.18.2**, Gradle **9.7.1**, and Java **25**. Metadata permits `>=26.2 <27-`; later 26.x releases still require runtime/API compatibility and are not guaranteed by that range.

Rendering uses Minecraft's `GuiGraphicsExtractor` and Fabric's HUD replacement API. No OpenGL-specific calls. Intended for OpenGL and Vulkan, with no Sodium, Iris or shader-pack dependency. Separate testing with Vulkan, Sodium, Iris, Spatial GUI and other HUD mods is still needed. Another mod replacing the crosshair later may take precedence; disabling this mod delegates to the previously registered renderer.

Vanilla first-person, spectator, F1 and debug-crosshair rules are respected. Gameplay reticles are hidden in menus and while sleeping. Vanilla attack-cooldown indicators are preserved.

Interactable detection uses the block's menu-provider API plus a small set of interaction block families. Minecraft has no universal side-effect-free “can interact” query; arbitrary modded or item-dependent interactions may show ordinary block focus. The mod never invokes a block interaction to probe it.

Hit confirmation observes incoming vanilla damage events attributed to the local player, including projectiles. Servers that omit these events cannot provide that confirmation. Small optional client mixins observe successful interactions, actual attack swings and incoming damage, and expose mining progress read-only. They do not cancel or change gameplay methods. There is no HUD mixin.

## Build

Set `JAVA_HOME` to a JDK 25 installation, then run:

```sh
./gradlew clean build
```

Windows: `gradlew.bat clean build`. Output: `build/libs/focus-crosshair-1.0.0.jar`. Minecraft 26.2 is unobfuscated; the standard Loom `jar` output is the production artifact and needs no remapping. The `-sources.jar` is not the installable mod.

Tests cover config creation/parsing/round-trip, corrupt-file recovery, defaults, clamping, spring convergence, variable frame rates and freeze protection. `gradlew.bat runClient` launches the development client without Mod Menu.

Dependency references: [Fabric example 26.2](https://github.com/FabricMC/fabric-example-mod/tree/26.2), [Fabric Maven](https://maven.fabricmc.net/), [Fabric HUD API](https://docs.fabricmc.net/develop/rendering/hud), [Fabric Loom](https://docs.fabricmc.net/develop/loom/).
