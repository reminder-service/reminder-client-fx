package de.bahmut.reminder.client.service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.bahmut.reminder.client.model.ApiMessage;
import de.bahmut.reminder.client.model.Settings;

@Log4j2
@Service
@RequiredArgsConstructor
public class MessageService {

    private static final String KEY_HEADER = "parameters";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_IDENTIFIER = "identifier";

    private final SettingsService settingsService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public void start() {
        executorService.shutdownNow();
        executorService = Executors.newSingleThreadScheduledExecutor();
        final Settings settings = settingsService.getSettings();
        final Runnable messageReceiver = createMessageReceiver(settings);
        executorService.scheduleAtFixedRate(messageReceiver, 0, settings.getUpdateRate(), TimeUnit.MINUTES);
    }

    private List<ApiMessage> receive(final Settings settings) {
        final URI url = UriComponentsBuilder.fromHttpUrl(settings.getUrl())
                .pathSegment(KEY_MESSAGE).path("/")
                .queryParam(KEY_IDENTIFIER, settings.getIdentifier())
                .build().toUri();
        final HttpEntity<String> entity = new HttpEntity<>(KEY_HEADER, createAuthorizationHeader(settings));
        final String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        try {
            return objectMapper.readValue(response, new TypeReference<List<ApiMessage>>() {});
        } catch (final IOException e) {
            log.error("Could not parse api response", e);
            return List.of();
        }
    }

    private void acknowledge(final Settings settings) {
        final URI url = UriComponentsBuilder.fromHttpUrl(settings.getUrl())
                .pathSegment(KEY_IDENTIFIER)
                .queryParam(KEY_IDENTIFIER, settings.getIdentifier())
                .build().toUri();
        final HttpEntity<String> entity = new HttpEntity<>(KEY_HEADER, createAuthorizationHeader(settings));
        restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
    }

    private Runnable createMessageReceiver(final Settings settings) {
        return () -> {
            final List<ApiMessage> messages;
            try {
                messages = receive(settings);
                acknowledge(settings);
            } catch (final Exception e) {
                log.error("Could not receive messages", e);
                return;
            }
            messages.forEach(notificationService::notify);
        };
    }

    private HttpHeaders createAuthorizationHeader(final Settings settings) {
        final HttpHeaders headers = new HttpHeaders();
        final String authorization = settings.getUsername() + ":" + settings.getPassword();
        final byte[] encoding = Base64.getEncoder().encode(authorization.getBytes(StandardCharsets.US_ASCII));
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + new String(encoding));
        return headers;
    }

}
