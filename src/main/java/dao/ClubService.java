
package dao;

import entite.Club;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import static interfaces.Log.LOG;

@ApplicationScoped
public class ClubService {

    @Inject
    private ClubDAO clubDAO; // CDI injectera ClubDAOImpl automatiquement
    

public ClubService() {} // constructeur public obligatoire
    @Transactional
    public boolean createClub(Club club) throws Exception {
        boolean result = clubDAO.create(club);;
        LOG.debug("CREATE Club result = " + result + ", ID = " + club.getIdclub());
        return result;
    }
    @Transactional
   // public Club getClubById(Integer clubId) throws Exception {
    public Club ReadClub(Club club) throws Exception {    
      //  Club club = new Club();
        club.setIdclub(101);
        return clubDAO.read(club);
    }

    @Transactional
    public boolean updateClub(Club club) throws Exception {
        boolean result = clubDAO.update(club);
        LOG.debug("UPDATE Club result = " + result);
        return result;
    }

    @Transactional
    public boolean deleteClub(Integer clubId) throws Exception {
        Club club = new Club();
        club.setIdclub(clubId);
        boolean result = clubDAO.delete(club);
        LOG.debug("DELETE Club result = " + result);
        return result;
    }

    @Transactional
    public boolean deleteClubWithChildren(Integer clubId) throws Exception {
        Club club = new Club();
        club.setIdclub(clubId);
        boolean result = clubDAO.deleteClubAndChilds(club);
        LOG.debug("DELETE Club with children result = " + result);
        return result;
    }
}
/*
code claude : 

*/
