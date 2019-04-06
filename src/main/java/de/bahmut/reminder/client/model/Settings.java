package de.bahmut.reminder.client.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
@RequiredArgsConstructor
public class Settings {

    final String url;
    final String identifier;
    final String username;
    final String password;
    final long updateRate;

    public static Settings of(
            final String url,
            final String identifier,
            final String username,
            final String password,
            final String updateRate
    ) {
        checkArgument(StringUtils.isNotBlank(url), "Url may not be blank");
        checkArgument(StringUtils.isNotBlank(identifier), "Identifier may not be blank");
        checkArgument(StringUtils.isNotBlank(username), "Username may not be blank");
        checkArgument(StringUtils.isNotBlank(password), "Password may not be blank");
        checkArgument(StringUtils.isNumeric(updateRate), "Update rate needs to be a number");
        return new Settings(
                url,
                identifier,
                username,
                password,
                Long.valueOf(updateRate)
        );
    }

}
