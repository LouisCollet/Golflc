
package dao;

import entite.Club;

/**
 * DAO spécifique pour Club, CRUD complet + delete en cascade
 * public interface ClubDAO extends CrudDAO<Club> {
 */
public interface ClubDAO extends CrudDAO<Club>{
/* pas reprises car se trouvent dans CrudDao
    @Override
    boolean create(Club club) throws Exception;
     @Override
    Club read(Club club) throws Exception;
     @Override
    boolean update(Club club) throws Exception;
     @Override
    boolean delete(Club club) throws Exception;
*/
    /**
     * Supprime le club et toutes les entités liées (cascade)
     */
    boolean deleteClubAndChilds(Club club) throws Exception;

    /**
     * Liste tous les clubs
     */
 //   List<Club> findAll() throws Exception;
} // end interface