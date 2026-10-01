# Hyauth

Hyauth is a server-side Fabric mod for Minecraft that allows multiple authentication providers to coexist on the same server.

The current 26.3 release supports:

- Microsoft/Minecraft accounts
- Ely.by accounts
- Ely.by player skins
- Configurable authentication provider order
- Provider fallback authentication
- Whitelist support across configured providers

Hyauth is designed to run entirely on the server. **Players do not need the Hyauth mod installed on their client.**

## Requirements

- Minecraft `26.3`
- Fabric Loader `0.19.5` or compatible
- Fabric API
- `online-mode=true` in `server.properties`

For servers accepting third-party authentication providers, you may also need to configure:

```properties
enforce-secure-profile=false
```

This depends on the rest of your server configuration and the clients connecting to it.

## How It Works

When a player joins, Hyauth intercepts the normal session verification process and checks the configured authentication providers in order.

For example, with the default configuration:

1. Hyauth checks Microsoft/Minecraft authentication.
2. If that provider does not authenticate the player, Hyauth checks Ely.by.
3. If a provider successfully authenticates the player, the returned profile is used for the Minecraft session.
4. If no configured provider authenticates the player, the connection is rejected.

The provider order is controlled by the `providers` list in `config/hyauth.json`.

## Features

### Multiple Authentication Providers

Hyauth supports a configurable list of authentication providers.

The default configuration includes:

- Microsoft/Minecraft
- Ely.by

Additional providers can be configured using the provider system.

### Provider Fallback

Providers are checked sequentially.

This means a server can accept Microsoft/Minecraft accounts and Ely.by accounts without requiring players to use separate server configurations.

### Fallback Protection

The `preventFallbackIfPlayerExists` option can prevent authentication from continuing to later providers when a username exists on the current provider but authentication fails.

This can help reduce username impersonation between authentication providers.

### Player Skins

Hyauth supports provider-supplied player properties.

The default Ely.by configuration includes support for Ely.by's skin system, allowing Ely.by players to receive their configured skin when joining the server.

### Whitelist Support

Hyauth supports resolving players through the configured providers when handling whitelist operations.

If the same username exists on multiple providers, provider order determines which provider is resolved first.

## Configuration

Hyauth automatically creates its configuration at:

```text
config/hyauth.json
```

The default configuration is:

```json
{
    "configVersion": 1,
    "debugMode": false,
    "preventFallbackIfPlayerExists": false,
    "providers": [
        {
            "name": "Mojang",
            "checkUrl": "https://sessionserver.mojang.com/session/minecraft/hasJoined",
            "profileUrl": "https://api.minecraftservices.com/minecraft/profile/lookup/name/",
            "profilesUrl": "https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname"
        },
        {
            "name": "Ely.by",
            "checkUrl": "https://authserver.ely.by/session/hasJoined",
            "profileUrl": "https://authserver.ely.by/api/users/profiles/minecraft/",
            "profilesUrl": "https://authserver.ely.by/api/profiles/minecraft",
            "propertyUrl": "http://skinsystem.ely.by/textures/signed/{0}"
        }
    ]
}
```

### `configVersion`

Internal configuration schema version.

Do not normally change this manually.

### `debugMode`

Controls additional authentication logging.

```json
"debugMode": false
```

Keep this disabled during normal operation unless additional authentication information is required for troubleshooting.

### `preventFallbackIfPlayerExists`

Controls whether the provider fallback chain stops when a username exists on the current provider but authentication fails.

```json
"preventFallbackIfPlayerExists": false
```

When enabled, Hyauth will not continue to later providers in this situation.

### `providers`

The ordered list of authentication providers.

Each provider supports the following fields:

| Field | Required | Description |
|---|---|---|
| `name` | Yes | Display name of the provider |
| `checkUrl` | Yes | Session authentication endpoint |
| `profileUrl` | Yes | Endpoint used to resolve an individual username |
| `profilesUrl` | Yes | Endpoint used for batch username resolution |
| `propertyUrl` | No | Endpoint used to retrieve player properties such as skins |

The `{0}` placeholder in `propertyUrl` is replaced with the player's username.

## Installation

1. Install Fabric Loader for Minecraft `26.3`.
2. Install the required Fabric API version.
3. Download the Hyauth `.jar`.
4. Place the Hyauth `.jar` in the server's `mods` directory.
5. Make sure `online-mode=true` is enabled.
6. Start the server.
7. Hyauth will create `config/hyauth.json` automatically.

Do **not** keep an old `alternative-auth` jar alongside Hyauth.

If you are upgrading from the original Alternative Authentication mod, back up your configuration before migrating.

## Server-Side Only

Hyauth does not require players to install a Hyauth client mod.

The authentication process is handled by the server.

Players using a third-party authentication provider still need an appropriate account/client authentication method for that provider.

## Current Compatibility

| Component | Version |
|---|---|
| Minecraft | `26.3` |
| Fabric Loader | `0.19.5` |
| Fabric API | `0.160.7+26.3` |
| Hyauth | `1.0.0+26.3` |

This release has been tested with:

- Microsoft/Minecraft authentication
- Ely.by authentication
- Ely.by player skins

Testing was performed on an isolated Minecraft 26.3 Fabric server before deployment.

## Known Limitations

The current release is focused on authentication compatibility and the initial Hyauth rebrand.

The following areas are planned for future versions:

- Provider-aware player identity handling
- Improved username collision handling
- Provider-aware join messages
- Dedicated Hyauth authentication logging
- Improved authentication failure reporting
- Authentication timeouts and rate limiting
- Configuration migration and validation
- Hyauth administration commands
- Automated GitHub Actions builds and tests
- Broader Minecraft version support

## Development

Hyauth is currently developed against Minecraft `26.3`.

Build the project with:

```bash
./gradlew build
```

On Windows:

```cmd
gradlew.bat build
```

The resulting JAR will be available in:

```text
build/libs/
```

## Attribution

Hyauth is based on the original **Alternative Authentication** project by **GGSkyOne**.

Original project:

https://github.com/GGSkyOne/alternative-authentication

Hyauth continues the project with a Minecraft 26.3-focused codebase and updated package, resource, and mod identifiers.

The original project is licensed under the MIT License.

## License

Hyauth is distributed under the MIT License.

See the included license file for the complete license text.

## Issues and Contributions

If you find a bug, encounter an authentication problem, or have a feature request, please open an issue on GitHub:

https://github.com/Abhiram-p/hyauth/issues

Pull requests are welcome.

## Repository

Hyauth:

https://github.com/Abhiram-p/hyauth
