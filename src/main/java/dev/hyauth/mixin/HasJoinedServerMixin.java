package dev.hyauth.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpDiscoveryService;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.services.MinecraftServicesSessionService;
import com.mojang.authlib.services.ProfileActionType;
import com.mojang.authlib.services.ProfileResult;
import com.mojang.authlib.services.response.HasJoinedMinecraftServerResponse;
import com.mojang.authlib.services.response.ProfileAction;

import dev.hyauth.config.AlternativeAuthConfig;
import dev.hyauth.config.AlternativeAuthConfigManager;
import dev.hyauth.config.AlternativeAuthProvider;
import dev.hyauth.logger.AlternativeAuthLogger;
import dev.hyauth.logger.AlternativeAuthLoggerManager;
import dev.hyauth.util.AlternativeAuthUtils;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetAddress;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mixin(MinecraftServicesSessionService.class)
public abstract class HasJoinedServerMixin {

    @Unique
    private static final AlternativeAuthLogger LOGGER =
        AlternativeAuthLoggerManager.getLogger();

    @Unique
    private static final AlternativeAuthConfig CONFIG =
        AlternativeAuthConfigManager.getConfig();

    @Shadow @Final
    private MinecraftClient client;

    @Shadow
    private static Set<ProfileActionType> extractProfileActionTypes(
        Set<ProfileAction> response
    ) {
        return null;
    }

    @Inject(
        at = @At("HEAD"),
        method = "hasJoinedServer",
        remap = false,
        cancellable = true
    )
    public void hasJoinedServer(
        String profileName,
        String serverId,
        InetAddress address,
        CallbackInfoReturnable<ProfileResult> cir
    ) throws AuthenticationUnavailableException {

        Map<String, Object> arguments = new HashMap<>();

        arguments.put("username", profileName);
        arguments.put("serverId", serverId);

        if (address != null) {
            arguments.put("ip", address.getHostAddress());
        }

        for (AlternativeAuthProvider provider : CONFIG.getProviders()) {

            LOGGER.debug(
                "Trying to authenticate player via " + provider.getName()
            );

            LOGGER.debug(
                "Using " + provider.getCheckUrl()
            );

            URL url = HttpDiscoveryService.concatenateURL(
                HttpDiscoveryService.constantURL(provider.getCheckUrl()),
                HttpDiscoveryService.buildQuery(arguments)
            );

            try {

                HasJoinedMinecraftServerResponse response =
                    client.get(
                        url,
                        HasJoinedMinecraftServerResponse.class
                    );

                LOGGER.debug(
                    provider.getName()
                    + " session response: "
                    + (
                        response == null
                            ? "null"
                            : AlternativeAuthUtils.GSON.toJson(response)
                    )
                );

                if (response == null || response.id() == null) {

                    if (playerExistsOnProvider(provider, profileName)) {

                        LOGGER.warn(
                            "Player '"
                            + profileName
                            + "' exists on "
                            + provider.getName()
                            + " but failed authentication, fallback prevented"
                        );

                        cir.setReturnValue(null);
                        break;
                    }

                    continue;
                }

                PropertyMap properties =
                    resolveProperties(
                        provider,
                        profileName,
                        response
                    );

                GameProfile profile =
                    properties != null
                        ? new GameProfile(
                            response.id(),
                            profileName,
                            properties
                        )
                        : new GameProfile(
                            response.id(),
                            profileName
                        );

                Set<ProfileActionType> profileActions =
                    extractProfileActionTypes(
                        response.profileActions()
                    );

                LOGGER.debug(
                    "Authentication successful for "
                    + profileName
                    + " (UUID: "
                    + response.id()
                    + ")"
                );

                LOGGER.info(
                    "Authenticating player via "
                    + provider.getName()
                );

                cir.setReturnValue(
                    new ProfileResult(
                        profile,
                        profileActions
                    )
                );

                break;

            } catch (com.mojang.authlib.exceptions.MinecraftClientException exception) {

                LOGGER.debug(
                    provider.getName()
                    + " threw during session check: "
                    + exception.getMessage()
                );

                if (
                    exception.toAuthenticationException()
                        instanceof AuthenticationUnavailableException unavailable
                ) {
                    throw unavailable;
                }

                if (playerExistsOnProvider(provider, profileName)) {

                    LOGGER.warn(
                        "Player '"
                        + profileName
                        + "' exists on "
                        + provider.getName()
                        + " but failed authentication, fallback prevented"
                    );

                    cir.setReturnValue(null);
                    break;
                }
            }
        }

        cir.cancel();
    }

    @Unique
    private boolean playerExistsOnProvider(
        AlternativeAuthProvider provider,
        String profileName
    ) {

        if (!CONFIG.isPreventFallbackIfPlayerExists()) {
            return false;
        }

        String profileUrl = provider.getProfileUrl();

        if (profileUrl == null) {
            return false;
        }

        try {

            var profile =
                client.get(
                    HttpDiscoveryService.constantURL(
                        profileUrl
                            + AlternativeAuthUtils.normalizeName(profileName)
                    ),
                    com.mojang.authlib.services.response.NameAndId.class
                );

            return profile != null;

        } catch (
            com.mojang.authlib.exceptions.MinecraftClientException e
        ) {

            LOGGER.debug(
                "Could not verify player existence on "
                + provider.getName()
                + ": "
                + e.getMessage()
            );

            return false;
        }
    }

    @Unique
    private PropertyMap resolveProperties(
        AlternativeAuthProvider provider,
        String profileName,
        HasJoinedMinecraftServerResponse response
    ) {

        PropertyMap fallback = response.properties();

        if (fallback == null) {
            return null;
        }

        String propertyUrlTemplate =
            provider.getPropertyUrl();

        if (propertyUrlTemplate == null) {
            return fallback;
        }

        String resolvedUrl =
            MessageFormat.format(
                propertyUrlTemplate,
                profileName,
                response.id()
            );

        URL propertyUrl =
            HttpDiscoveryService.concatenateURL(
                HttpDiscoveryService.constantURL(resolvedUrl),
                null
            );

        HasJoinedMinecraftServerResponse propertyResponse =
            client.get(
                propertyUrl,
                HasJoinedMinecraftServerResponse.class
            );

        if (propertyResponse != null) {
            return propertyResponse.properties();
        }

        return fallback;
    }
}
