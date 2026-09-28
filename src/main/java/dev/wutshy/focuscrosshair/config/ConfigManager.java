package dev.wutshy.focuscrosshair.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("focuscrosshair");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path;
    public FocusConfig config = new FocusConfig();

    public ConfigManager(Path path) { this.path = path; }

    public void load() {
        if (Files.exists(path)) {
            try {
                config = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), FocusConfig.class);
                if (config == null) throw new IllegalArgumentException("Empty configuration");
                config.validate();
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("Could not load Focus Crosshair configuration; using defaults", exception);
                config = new FocusConfig();
                try {
                    Files.move(path, path.resolveSibling(path.getFileName() + ".broken-" + System.currentTimeMillis()));
                } catch (IOException backupFailure) {
                    LOGGER.warn("Could not preserve damaged configuration", backupFailure);
                    return;
                }
            }
        }
        save();
    }

    public void save() {
        config.validate();
        try {
            Files.createDirectories(path.toAbsolutePath().getParent());
            Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(temporary, GSON.toJson(config) + System.lineSeparator(), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            LOGGER.warn("Could not save Focus Crosshair configuration", exception);
        }
    }
}
