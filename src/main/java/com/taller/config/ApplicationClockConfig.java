package com.taller.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationClockConfig {

    @Bean
    public Clock applicationClock(@Value("${app.time-zone:America/Argentina/Buenos_Aires}") String timeZone) {
        return Clock.system(ZoneId.of(timeZone));
    }
}
