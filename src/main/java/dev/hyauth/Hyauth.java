package dev.hyauth;

import net.fabricmc.api.DedicatedServerModInitializer;
import dev.hyauth.config.AlternativeAuthConfigManager;
import dev.hyauth.logger.AlternativeAuthLoggerManager;

public class Hyauth implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		AlternativeAuthConfigManager.loadConfig();
        AlternativeAuthLoggerManager.configureLogger(AlternativeAuthConfigManager.getConfig().isDebugModeEnabled());

		AlternativeAuthLoggerManager.getLogger().info("Alternative Authentication is now powering your Minecraft server! \uD83D\uDD10");
	}
}

