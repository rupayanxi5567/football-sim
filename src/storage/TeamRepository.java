package storage;

import models.Team;

import java.sql.SQLException;

public interface TeamRepository {
    Team loadClub(String name) throws SQLException;
    Team loadNationalTeam(String name) throws SQLException;
}