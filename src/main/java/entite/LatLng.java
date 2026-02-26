package entite;

import java.io.Serializable;
import java.util.Objects;

public class LatLng implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double lat;
    private Double lng;

    public LatLng() {
    }

    public LatLng(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public boolean isValid() {
        return lat != null && lng != null;
    }

    @Override
    public String toString() {
        return "LatLng{" +
               "lat=" + lat +
               ", lng=" + lng +
               '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lng);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LatLng other)) return false;
        return Objects.equals(lat, other.lat)
            && Objects.equals(lng, other.lng);
    }
}
