package dev.hyauth.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.hyauth.Hyauth;
import dev.hyauth.logger.AlternativeAuthLoggerManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class AlternativeAuthConfigManager {

    private static final String CONFIG_FILE_NAME = "hyauth.json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static AlternativeAuthConfig config;

    public static void loadConfig() {
        File configFile = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(CONFIG_FILE_NAME)
                .toFile();

        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }

        try (Reader reader = new InputStreamReader(
                new FileInputStream(configFile),
                StandardCharsets.UTF_8)) {

            config = GSON.fromJson(reader, AlternativeAuthConfig.class);

            if (config == null) {
                throw new IOException("Configuration file is empty or invalid.");
            }

            AlternativeAuthLoggerManager.getLogger().info(
                    "Loaded Hyauth configuration from " + configFile.getAbsolutePath()
            );

        } catch (Exception e) {
            AlternativeAuthLoggerManager.getLogger().warn(
                    "Failed to load Hyauth configuration: " + e.getMessage()
            );

            config = new AlternativeAuthConfig();
        }
    }

    public static AlternativeAuthConfig getConfig() {
        if (config == null) {
            loadConfig();
        }

        return config;
    }

    public static void reloadConfig() {
        loadConfig();
    }

    private static void createDefaultConfig(File configFile) {
        try {
            File parent = configFile.getParentFile();

            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException(
                        "Failed to create config directory: " + parent.getAbsolutePath()
                );
            }

            InputStream stream = Hyauth.class
                    .getClassLoader()
                    .getResourceAsStream(CONFIG_FILE_NAME);

            if (stream == null) {
                throw new IOException(
                        "Default Hyauth configuration resource '" +
                                CONFIG_FILE_NAME +
                                "' was not found."
                );
            }

            try (InputStream input = stream;
                 OutputStream output = new FileOutputStream(configFile)) {

                byte[] buffer = new byte[8192];
                int length;

                while ((length = input.read(buffer)) != -1) {
                    output.write(buffer, 0, length);
                }
            }

            AlternativeAuthLoggerManager.getLogger().info(
                    "Created default Hyauth configuration at " +
                            configFile.getAbsolutePath()
            );

        } catch (Exception e) {
            AlternativeAuthLoggerManager.getLogger().warn(
                    "Failed to create default Hyauth configuration: " +
                            e.getMessage()
            );
        }
    }
}