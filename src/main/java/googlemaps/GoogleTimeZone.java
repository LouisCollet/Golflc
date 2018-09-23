package googlemaps;
//import static interfaces.Log.LOG;
//Geometry class will have location and other details .
//  "dstOffset" : 0,
//   "rawOffset" : -28800,
//   "status" : "OK",
//   "timeZoneId" : "America/Los_Angeles",
//   "timeZoneName" : "Pacific Standard Time"

/*dstOffset — Décalage de l'heure d'été en secondes. Sa valeur est nulle si le fuseau horaire
    ne se trouve pas en période d'heure d'été, d'après la valeur spécifiée pour le paramètretimestamp.
rawOffset — Décalage par rapport à l'heure UTC (en secondes) pour le point géographique donné.
    L'heure d'été n'est pas prise en compte.
timeZoneId — Chaîne contenant l'identifiant « tz » du fuseau horaire (« America/Los_Angeles »
    ou « Australia/Sydney », par exemple). Ce
timeZoneName — Chaîne contenant le nom complet du fuseau horaire.
    Ce champ est localisé si le paramètre de langue est défini (« Heure d'été du Pacifique »
    ou « Heure d'été de la côte Est de l'Australie », par exemple).
status — Chaîne indiquant le statut de la réponse.
    OK indique que la requête a abouti.
    INVALID_REQUEST indique que la requête a été mal formulée.
    OVER_QUERY_LIMIT indique que le demandeur a dépassé le quota de requêtes autorisées.
    REQUEST_DENIED indique que l'API n'a pas pu exécuter la requête. Vérifiez que la requête a été envoyée via HTTPS (et non via HTTP).
    UNKNOWN_ERROR indique une erreur inconnue.
    ZERO_RESULTS indique qu'aucune donnée de fuseau horaire n'a été identifiée pour l'heure ou la position spécifiée. Vérifiez que la requête concerne un point géographique terrestre (et non marin).
error_message — Informations plus détaillées sur le motif de ce code de statut, s'il est autre que OK.
*/
// pour utilsier le mapper : donner les mêmes noms que dans Json (et String ou integer)

public class GoogleTimeZone {

private Integer dstOffset ;
private double rawOffset;
private String timeZoneId;
private String timeZoneName;
private String status;
private String error_message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDstOffset() {
        return dstOffset;
    }

    public void setDstOffset(Integer dstOffset) {
        this.dstOffset = dstOffset;
    }

    public double getRawOffset() {
        return rawOffset;
    }

    public void setRawOffset(double rawOffset) {
        this.rawOffset = rawOffset;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getTimeZoneName() {
        return timeZoneName;
    }

    public void setTimeZoneName(String timeZoneName) {
        this.timeZoneName = timeZoneName;
    }

    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }
public String toString()
{ return 
        ("from " + getClass().getSimpleName() + " : "
               + " ,dstOffset : "   + this.dstOffset
               + " ,rawOffset : " + this.rawOffset
               + " ,ZoneId : " + this.timeZoneId
               + " ,ZoneName : " + this.timeZoneName
               + " ,status : " + this.status
               + " ,error_message : " + this.error_message
        );
}   
 
} //end class