
package googlemaps;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import static interfaces.Log.LOG;

import java.util.Arrays;
import java.util.Optional;

public final class GoogleRegionResolver {

    private GoogleRegionResolver() {
        // utility class
    }

    public static Optional<String> resolveRegion(GeocodingResult result) {
        LOG.debug("entering resolveRegion");
    if (result == null || result.addressComponents == null) {
        LOG.debug("resolver - result is null");
        return Optional.empty();
    }

    return findByType(result, AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1);
        //    .or(() -> findByType(result, AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_2))
        //    .or(() -> findByType(result, AddressComponentType.LOCALITY));
}

private static Optional<String> findByType(
        GeocodingResult result,
        AddressComponentType expectedType
) {
    return Arrays.stream(result.addressComponents)
            .filter(c -> hasType(c, expectedType))
            .map(c -> c.longName)
            .findFirst();
}

private static boolean hasType(
        AddressComponent component,
        AddressComponentType expectedType
) {
    LOG.debug("hasType component = " + component);
    LOG.debug("hasType expectedType = " + expectedType);
    if (component.types == null) {
        return false;
    }

    for (AddressComponentType type : component.types) {
        if (expectedType.name().equals(type.name())) {
            LOG.debug("equals for expecedType = " + expectedType.name());
            LOG.debug("equals for type.name = " + type.name());
            return true;
        }
    }
    return false;
}
} // end class