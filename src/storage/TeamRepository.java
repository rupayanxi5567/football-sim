package storage;

import models.Team;

import java.sql.SQLException;
import java.util.List;

public interface TeamRepository {
    Team loadClub(String name) throws SQLException;
    Team loadNationalTeam(String name) throws SQLException;
    List<Team> loadLeague(String leagueName) throws SQLException;
}