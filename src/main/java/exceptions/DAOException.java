
package exceptions;
public class DAOException extends RuntimeException {
    /*
     * https://openclassrooms.com/fr/courses/626954-creez-votre-application-web-avec-java-ee/624784-le-modele-dao
     */
    public DAOException( String message ) {
        super( message );
    }

    public DAOException( String message, Throwable cause ) {
        super( message, cause );
    }

    public DAOException( Throwable cause ) {
        super( cause );
    }
}