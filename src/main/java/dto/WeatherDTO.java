
package dto;

import java.time.ZonedDateTime;

public record WeatherDTO( // used in FindOpenWeather2 et WeatherCacheService

        ZonedDateTime time,

        String main,
        String description,
        String icon,

        Double temperature,
        Double feelsLike,
        Integer humidity,

        String windDirection,
        Double windSpeed
) {}