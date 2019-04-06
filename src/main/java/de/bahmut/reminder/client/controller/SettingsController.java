package de.bahmut.reminder.client.controller;

import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import de.bahmut.reminder.client.model.Settings;
import de.bahmut.reminder.client.service.MessageService;
import de.bahmut.reminder.client.service.SettingsService;

@Log4j2
@Component
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService service;
    private final MessageService messageService;

    @FXML
    public TextField url;

    @FXML
    public TextField identifier;

    @FXML
    public TextField username;

    @FXML
    public TextField password;

    @FXML
    public TextField updateRate;

    @FXML
    public void initialize() {
        final Optional<Settings> settings = service.load();
        if (settings.isEmpty()) {
            return;
        }
        url.setText(settings.get().getUrl());
        identifier.setText(settings.get().getIdentifier());
        username.setText(settings.get().getUsername());
        password.setText(settings.get().getPassword());
        updateRate.setText(String.valueOf(settings.get().getUpdateRate()));
        messageService.start();
    }

    @FXML
    public void saveSettings() {
        try {
            final Settings settings = Settings.of(
                    url.getText(),
                    identifier.getText(),
                    username.getText(),
                    password.getText(),
                    updateRate.getText()
            );
            service.save(settings);
            messageService.start();
        } catch (final Exception e) {
            log.debug("Could not save settings", e);
            final Alert alert = new Alert(Alert.AlertType.WARNING, String.format("Could not save settings%n%s", e.getMessage()));
            alert.showAndWait();
        }
    }

}
