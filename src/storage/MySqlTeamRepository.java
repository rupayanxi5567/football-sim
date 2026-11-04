package storage;

import models.Player;
import models.Position;
import models.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MySqlTeamRepository implements TeamRepository {

    private static final String CLUB_SQL = """
            SELECT p.name, p.position, p.pace, p.passing, p.shooting, p.defending, p.stamina
            FROM players p JOIN teams t ON t.id = p.club_id
            WHERE t.name = ? AND t.type = 'CLUB'
            """;

    private static final String NATIONAL_SQL = """
            SELECT p.name, p.position, p.pace, p.passing, p.shooting, p.defending, p.stamina
            FROM players p JOIN teams t ON t.id = p.national_team_id
            WHERE t.name = ? AND t.type = 'NATIONAL'
            """;

    private static final String LEAGUE_SQL = """
            SELECT t.name AS team_name, p.name, p.position, p.pace, p.passing,
                   p.shooting, p.defending, p.stamina
            FROM players p JOIN teams t ON t.id = p.club_id
            WHERE t.type = 'CLUB' AND t.league_name = ?
            ORDER BY t.name
            """;

    @Override
    public Team loadClub(String name) throws SQLException {
        return load(CLUB_SQL, name);
    }

    @Override
    public Team loadNationalTeam(String name) throws SQLException {
        return load(NATIONAL_SQL, name);
    }

    @Override
    public List<Team> loadLeague(String leagueName) throws SQLException {
        Map<String, Team> teams = new LinkedHashMap<>();
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(LEAGUE_SQL)) {
            ps.setString(1, leagueName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String teamName = rs.getString("team_name");
                    Team team = teams.computeIfAbsent(teamName, Team::new);
                    team.addPlayer(readPlayer(rs));
                }
            }
        }
        if (teams.isEmpty()) {
            throw new IllegalArgumentException("এই নামে কোনো লিগ নেই: " + leagueName);
        }
        return new ArrayList<>(teams.values());
    }

    private Team load(String sql, String teamName) throws SQLException {
        Team team = new Team(teamName);
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, teamName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    team.addPlayer(readPlayer(rs));
                }
            }
        }
        if (team.getSquad().isEmpty()) {
            throw new IllegalArgumentException("No team or player found with this name: " + teamName);
        }
        return team;
    }

    private Player readPlayer(ResultSet rs) throws SQLException {
        return new Player(
                rs.getString("name"),
                Position.valueOf(rs.getString("position")),
                rs.getInt("pace"),
                rs.getInt("passing"),
                rs.getInt("shooting"),
                rs.getInt("defending"),
                rs.getInt("stamina"));
    }
}