package de.bahmut.reminder.client.service;

import java.awt.TrayIcon;
import java.time.format.DateTimeFormatter;

import fr.jcgay.notification.Icon;
import fr.jcgay.notification.Notification;
import fr.jcgay.notification.Notifier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Service;

import de.bahmut.reminder.client.ReminderClient;
import de.bahmut.reminder.client.model.ApiMessage;

@SuppressWarnings("WeakerAccess")
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String TITLE = "Reminder";
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Icon icon;
    private final Notifier notifier;


    public void notify(final ApiMessage message) {
        if (SystemUtils.IS_OS_WINDOWS) {
            windowsNotification(message);
        } else {
            linuxNotification(message);
        }
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

    private void windowsNotification(final ApiMessage message) {
        final String content = String.format("%s%n%s", message.getMessage(), message.getTimestamp().format(OUTPUT_FORMAT));
        waitForTrayIcon();
        ReminderClient.getTrayIcon().displayMessage(TITLE, content, TrayIcon.MessageType.NONE);
    }

    private void linuxNotification(final ApiMessage message) {
        final Notification notification = Notification.builder()
                .message(message.getMessage())
                .subtitle(message.getTimestamp().format(OUTPUT_FORMAT))
                .icon(icon)
                .level(Notification.Level.INFO)
                .title(TITLE)
                .build();
        try {
            notifier.send(notification);
        } finally {
            notifier.close();
        }
    }

}
