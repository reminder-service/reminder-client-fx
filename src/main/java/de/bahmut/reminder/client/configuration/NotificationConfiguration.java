package de.bahmut.reminder.client.configuration;

import de.bahmut.reminder.client.ReminderClient;
import fr.jcgay.notification.Application;
import fr.jcgay.notification.Icon;
import fr.jcgay.notification.Notifier;
import fr.jcgay.notification.notifier.executor.RuntimeExecutor;
import fr.jcgay.notification.notifier.notifysend.NotifySendConfiguration;
import fr.jcgay.notification.notifier.notifysend.NotifySendNotifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfiguration {

    @Bean
    public Icon icon() {
        return Icon.create(ReminderClient.class.getResource("icon.png"), ReminderClient.NAME);
    }

    @Bean
    public Application application(final Icon icon) {
        return Application.builder()
                .name(ReminderClient.NAME)
                .id(ReminderClient.NAME)
                .icon(icon)
                .build();
    }

    @Bean
    public NotifySendConfiguration configuration() {
        return NotifySendConfiguration.byDefault();
    }

    @Bean
    public Notifier notifier(final Application application, final NotifySendConfiguration configuration) {
        return new NotifySendNotifier(application, configuration, new RuntimeExecutor(3000)).init();
    }

}
