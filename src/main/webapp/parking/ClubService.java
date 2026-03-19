package dao;

import entite.Club;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.Serializable;

@ApplicationScoped
public class ClubService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClubDAO clubDAO;

    public ClubService() { }

    @Transactional
    public boolean createClub(Club club) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        boolean result = clubDAO.create(club);
        LOG.debug(methodName + " - CREATE Club result = " + result + ", ID = " + club.getIdclub());
        return result;
    } // end method

    @Transactional
    public Club ReadClub(Club club) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        club.setIdclub(101);
        return clubDAO.read(club);
    } // end method

    @Transactional
    public boolean updateClub(Club club) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        boolean result = clubDAO.update(club);
        LOG.debug(methodName + " - UPDATE Club result = " + result);
        return result;
    } // end method

    @Transactional
    public boolean deleteClub(Integer clubId) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Club club = new Club();
        club.setIdclub(clubId);
        boolean result = clubDAO.delete(club);
        LOG.debug(methodName + " - DELETE Club result = " + result);
        return result;
    } // end method

    @Transactional
    public boolean deleteClubWithChildren(Integer clubId) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Club club = new Club();
        club.setIdclub(clubId);
        boolean result = clubDAO.deleteClubAndChilds(club);
        LOG.debug(methodName + " - DELETE Club with children result = " + result);
        return result;
    } // end method

} // end class
