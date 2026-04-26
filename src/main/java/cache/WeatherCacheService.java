
package cache;

import dto.WeatherDTO;
import entite.Club;
import find.FindOpenWeather2;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class WeatherCacheService {

    private static final Duration CACHE_DURATION = Duration.ofMinutes(5);

    private final Map<WeatherKey, CachedWeather> cache = new ConcurrentHashMap<>();

    @Inject private FindOpenWeather2 weatherService;

    public WeatherDTO getWeather(Club club, String language) {

        WeatherKey key = new WeatherKey(club.getIdclub(), language);
        CachedWeather cached = cache.get(key);
        if (cached != null && !cached.isExpired()) {
            return cached.weather();
        }

        WeatherDTO weather = weatherService.find(club, language);

        if (weather != null) {
            cache.put(key, new CachedWeather(weather, Instant.now()));
        }

        return weather;
    }

  //  private record WeatherKey(Long clubId, String language) {}
    private static record WeatherKey(Integer clubId, String language) {}
    private record CachedWeather(
            WeatherDTO weather,
            Instant timestamp) {

        boolean isExpired() {
            return Instant.now()
                    .isAfter(timestamp.plus(CACHE_DURATION));
        }
    }
}