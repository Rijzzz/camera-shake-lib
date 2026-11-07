Welcome to the Camera Shake Lib wiki! 

Here you will find quick setup guide and code examples for adding camera shake to your Fabric mods.

## Gradle Setup

Add the Modrinth Maven repository and the dependency in `build.gradle`

```groovy
repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = "https://api.modrinth.com/maven"
            }
        }
        filter {
            includeGroup "maven.modrinth"
        }
    }
}

dependencies {
    implementation "maven.modrinth:camera-shake-lib:x.x.x"

    // optional: bundle inside your jar (skip this if you want users to download it separately)
    include(implementation("maven.modrinth:camera-shake-lib:x.x.x"))
}
```

Add the dependency in `fabric.mod.json`

```json
"depends": {
    "camera-shake-lib": ">=x.x.x"
}
```

## Parameters

All shake methods share these parameters.

* `intensity` shake strength. Use 0.2 to 0.5 for small hits, 1.0 to 2.0 for big explosions.
* `duration` length in game ticks (20 ticks = 1 second). Max is `MAX_TICKS` (1200 ticks, 60 seconds).
* `frequency` wobble speed multiplier. Default is 1.0f. Minimum is 0.1f.

The methods on `CameraShake` ignore calls if intensity or frequency is not finite (such as NaN or infinite), or if intensity or duration is zero or negative.

When creating a `ShakeInstance`, passing non finite values for intensity or frequency throws an `IllegalArgumentException`. Finite frequencies below 0.1f are clamped up to 0.1f, and duration is clamped between 1 and `MAX_TICKS`.

The client plays up to 64 active shakes at once and skips extra requests while full to prevent lag.

## Client Usage

Call `CameraShake.shake(...)` on the client when an effect should play, such as weapon recoil or an impact.

```java
import com.rijz.camerashakelib.CameraShake;
import com.rijz.camerashakelib.ShakeInstance;

// Play a shake with a custom frequency.
CameraShake.shake(0.6f, 25, 1.5f);

// Reuse one instance across separate events.
ShakeInstance impact = new ShakeInstance(0.8f, 30);
CameraShake.shake(impact);
// On a later impact:
CameraShake.shake(impact);

ShakeInstance rumble = new ShakeInstance(0.5f, 40).noDecay();
CameraShake.shake(rumble);

boolean active = CameraShake.isShaking();
CameraShake.stopAll();
```

By default, shakes fade out over their duration. But by calling .noDecay() you can keep the shake at full strength for the entire duration, and then it ends immediately.

Calling `shake(instance)` copies the instance’s settings and the remaining ticks, so the original shake object can be reused later if you want. You can also call `instance.copy()` if you want to take a snapshot yourself. All active shakes automatically stop when the player disconnects.

## Server Usage

Call this on server side, like during explosions or boss attacks.

```java
import com.rijz.camerashakelib.CameraShake;

// one player
CameraShake.send(serverPlayer, 0.5f, 20);

// nearby players, gets weaker further away
CameraShake.sendNearby(serverLevel, explosionPos, 16.0, 1.2f, 30);

// everyone on server
CameraShake.sendAll(server, 0.8f, 60);

// stop shake for a player
CameraShake.stop(serverPlayer);

// check if player has the mod installed
if (CameraShake.isSupported(serverPlayer)) {
    // custom client logic if needed
}
```

Calling `CameraShake.stop(player)` clears all active shakes on that player.

Players without the mod still get a vanilla hurt tilt so they feel the hit.

## In Game Commands

Test shakes directly in game.

Accepted value ranges:
* intensity: 0.01 to 10.0
* duration: 1 to 1200 ticks
* frequency: 0.1 to 10.0

```
/camerashake <intensity> <duration> [frequency]
/camerashake <targets> <intensity> <duration> [frequency]
/camerashake stop [targets]
```

Examples

```
/camerashake 0.8 25
/camerashake PlayerName 1.0 30
/camerashake @a 1.2 40 2.0
/camerashake stop @a
```
===

### Navigation

* [Home](Home)
* [Gradle Setup](Home#gradle-setup)
* [Parameters](Home#parameters)
* [Client Usage](Home#client-usage)
* [Server Usage](Home#server-usage)
* [Commands](Home#in-game-commands)

### Links

* [GitHub Source](https://github.com/Rijzzz/camera-shake-lib)
* [License](https://github.com/Rijzzz/camera-shake-lib/blob/master/LICENSE)
* [Modrinth](https://modrinth.com/mod/camera-shake-lib)
* [CurseForge](https://www.curseforge.com/minecraft/mc-mods/camera-shake-lib)
* [Discord Server](https://discord.com/invite/asPJ4qgs8q)
* [Issue Tracker](https://github.com/Rijzzz/camera-shake-lib/issues)

===

Camera Shake Lib is open source under the [MIT License](https://github.com/Rijzzz/camera-shake-lib/blob/master/LICENSE).

Need help or found an issue? Join our [Discord](https://discord.com/invite/asPJ4qgs8q) or submit an [Issue](https://github.com/Rijzzz/camera-shake-lib/issues).
