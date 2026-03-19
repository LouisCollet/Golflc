package dao;

import entite.Club;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static interfaces.Log.LOG;
import java.util.List;
import connection_package.ConnectionProvider;
import java.sql.SQLException;

@ApplicationScoped
public class ClubDAOImpl implements ClubDAO {
 //   private ConnectionProvider connectionProvider;
    
 //   @Inject
 //   public void setConnectionProvider(ConnectionProvider provider) {
 //       this.connectionProvider = provider;
 //   }

    // ✅ Injection manuelle (pour lancer les tests via main / tests simples)
    public void setConnectionProviderManually(ConnectionProvider provider) {
          LOG.debug(">>> Provider utilisé : " + provider.getClass().getName());
        this.connectionProvider = provider;
    }

    @Inject
    private ConnectionProvider connectionProvider; // injection CDI via ConnectionProducer
    
    // ✅ Méthode sécurisée pour récupérer la connexion
    private Connection getConnection() {
        if (connectionProvider == null) {
            throw new IllegalStateException("JdbcConnectionProvider not initialized");
        }
        try {
            LOG.debug(">>> Provider utilisé : " + connectionProvider.getClass().getName());
            return connectionProvider.getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Unable to obtain Connection", e);
        }
    }

    
    @Override
    public boolean create(Club club) throws SQLException {
        String query = "INSERT INTO club (clubName, clubWebsite) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, club.getClubName());
            ps.setString(2, club.getClubWebsite());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) club.setIdclub(rs.getInt(1));
                }
                LOG.debug("Club created: " + club);
                return true;
            }
            return false;
        }
    }
    
    
    
 /*   @Override
    public boolean create(Club club) throws Exception {
       // checkConnection();
        String query = "INSERT INTO club (clubName, clubWebsite) VALUES (?, ?)";
        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, club.getClubName());
            ps.setString(2, club.getClubWebsite());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        club.setIdclub(rs.getInt(1));
                    }
                }
                LOG.debug("Club created: " + club);
                return true;
            }
            return false;
        }
    }
*/
    
    
    
    
    @Override
    public Club read(Club club) throws Exception {
        String query = "SELECT * FROM club WHERE idclub = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setInt(1, club.getIdclub());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    club.setClubName(rs.getString("clubName"));
                    club.setClubWebsite(rs.getString("clubWebsite"));
                    return club;
                }
            }
        }
        return null;
    }

    @Override
    public boolean update(Club club) throws Exception {
        String query = "UPDATE club SET clubName = ?, clubWebsite = ? WHERE idclub = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setString(1, club.getClubName());
            ps.setString(2, club.getClubWebsite());
            ps.setInt(3, club.getIdclub());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Club club) throws Exception {
        String query = "DELETE FROM club WHERE idclub = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(query)) {
            ps.setInt(1, club.getIdclub());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteClubAndChilds(Club club) throws Exception {
        // Exemple simple : supprimer club et ses cours
        String deleteCourses = "DELETE FROM course WHERE club_idclub = ?";
        String deleteClub = "DELETE FROM club WHERE idclub = ?";
        try (PreparedStatement ps1 = getConnection().prepareStatement(deleteCourses);
             PreparedStatement ps2 = getConnection().prepareStatement(deleteClub)) {
            ps1.setInt(1, club.getIdclub());
            ps1.executeUpdate();
            ps2.setInt(1, club.getIdclub());
            return ps2.executeUpdate() > 0;
        }
    }

    @Override
    public List<Club> findAll() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
