/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exceptions;

//https://openclassrooms.com/fr/courses/626954-creez-votre-application-web-avec-java-ee/624784-le-modele-dao

public class DAOConfigurationException extends RuntimeException {
    /*
     * Constructeurs
     */
    public DAOConfigurationException( String message ) {
        super( message );
    }

    public DAOConfigurationException( String message, Throwable cause ) {
        super( message, cause );
    }

    public DAOConfigurationException( Throwable cause ) {
        super( cause );
    }
}