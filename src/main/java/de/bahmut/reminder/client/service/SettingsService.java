package de.bahmut.reminder.client.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Properties;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import de.bahmut.reminder.client.model.Settings;

@Log4j2
@Service
public class SettingsService {

    private static final Path SETTINGS_FILE = Paths.get(System.getProperty("user.home"), ".reminder", "settings.properties");

    private static final String KEY_URL = "url";
    private static final String KEY_IDENTIFIER = "identifier";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_UPDATE_RATE = "updateRate";

    @Getter
    private Settings settings;

    public void save(final Settings settings) throws IOException {
        Files.createDirectories(SETTINGS_FILE.getParent());
        final Properties properties = new Properties();
        properties.setProperty(KEY_URL, settings.getUrl());
        properties.setProperty(KEY_IDENTIFIER, settings.getIdentifier());
        properties.setProperty(KEY_USERNAME, settings.getUsername());
        properties.setProperty(KEY_PASSWORD, settings.getPassword());
        properties.setProperty(KEY_UPDATE_RATE, String.valueOf(settings.getUpdateRate()));
        properties.store(Files.newOutputStream(SETTINGS_FILE, StandardOpenOption.CREATE), "Reminder Center");
        this.settings = settings;
    }

    public Optional<Settings> load() {
        if (Files.notExists(SETTINGS_FILE)) {
            return Optional.empty();
        }
        try {
            final Properties properties = new Properties();
            properties.load(Files.newInputStream(SETTINGS_FILE));
            this.settings = Settings.of(
                    properties.getProperty(KEY_URL),
                    properties.getProperty(KEY_IDENTIFIER),
                    properties.getProperty(KEY_USERNAME),
                    properties.getProperty(KEY_PASSWORD),
                    properties.getProperty(KEY_UPDATE_RATE)
            );
            return Optional.of(this.settings);
        } catch (final Exception e) {
            log.error("Could not load settings file", e);
            return Optional.empty();
        }
    }

}
