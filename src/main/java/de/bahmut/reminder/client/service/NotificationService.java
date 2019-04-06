package de.bahmut.reminder.client.service;

import java.awt.TrayIcon;
import java.time.format.DateTimeFormatter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import de.bahmut.reminder.client.ReminderClient;
import de.bahmut.reminder.client.model.ApiMessage;

@SuppressWarnings("WeakerAccess")
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void notify(final ApiMessage message) {
        final String content = String.format("%s%n%s", message.getMessage(), message.getTimestamp().format(OUTPUT_FORMAT));
        waitForTrayIcon();
        ReminderClient.getTrayIcon().displayMessage("Reminder", content, TrayIcon.MessageType.NONE);
    }

    private void waitForTrayIcon() {
        while (ReminderClient.getTrayIcon() == null) {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
