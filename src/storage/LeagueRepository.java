package storage;

import league.Fixture;
import league.League;
import models.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class LeagueRepository {
    private final TeamRepository teamRepo;

    public LeagueRepository(TeamRepository teamRepo) {
        this.teamRepo = teamRepo;
    }

    public int save(String saveName, long seed, League league) throws SQLException {
        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);          // সব লেখা একসঙ্গে সফল হবে, নইলে কিছুই না
            try {
                Map<String, Integer> ids = clubIds(c, league.getTeams());
                int saveId = insertSave(c, saveName, seed);
                insertSaveTeams(c, saveId, ids.values());
                insertFixtures(c, saveId, league.getFixtures(), ids);
                c.commit();
                return saveId;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private Map<String, Integer> clubIds(Connection c, List<Team> teams) throws SQLException {
        String marks = String.join(",", Collections.nCopies(teams.size(), "?"));
        String sql = "SELECT id, name FROM teams WHERE type = 'CLUB' AND name IN (" + marks + ")";
        Map<String, Integer> ids = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < teams.size(); i++) {
                ps.setString(i + 1, teams.get(i).getName());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.put(rs.getString("name"), rs.getInt("id"));
                }
            }
        }
        if (ids.size() != teams.size()) {
            throw new IllegalStateException("কিছু ক্লাব ডেটাবেসে পাওয়া যায়নি");
        }
        return ids;
    }

    private int insertSave(Connection c, String name, long seed) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO saves (name, seed) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setLong(2, seed);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void insertSaveTeams(Connection c, int saveId, Collection<Integer> teamIds) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO save_teams (save_id, team_id) VALUES (?, ?)")) {
            for (int teamId : teamIds) {
                ps.setInt(1, saveId);
                ps.setInt(2, teamId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertFixtures(Connection c, int saveId, List<Fixture> fixtures, Map<String, Integer> ids) throws SQLException {
        String sql = "INSERT INTO fixtures (save_id, round, home_team_id, away_team_id, played, home_goals, away_goals) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Fixture f : fixtures) {
                ps.setInt(1, saveId);
                ps.setInt(2, f.getRound());
                ps.setInt(3, ids.get(f.getHome().getName()));
                ps.setInt(4, ids.get(f.getAway().getName()));
                ps.setBoolean(5, f.isPlayed());
                ps.setInt(6, f.getHomeGoals());
                ps.setInt(7, f.getAwayGoals());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public League load(int saveId) throws SQLException {
        try (Connection c = Db.getConnection()) {
            long seed = readSeed(c, saveId);

            Map<Integer, Team> byId = new LinkedHashMap<>();
            for (Map.Entry<Integer, String> e : readTeamNames(c, saveId).entrySet()) {
                byId.put(e.getKey(), teamRepo.loadClub(e.getValue()));
            }

            List<Fixture> fixtures = new ArrayList<>();
            String sql = "SELECT round, home_team_id, away_team_id, played, home_goals, away_goals "
                    + "FROM fixtures WHERE save_id = ? ORDER BY id";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, saveId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Fixture f = new Fixture(rs.getInt("round"),
                                byId.get(rs.getInt("home_team_id")),
                                byId.get(rs.getInt("away_team_id")));
                        if (rs.getBoolean("played")) {
                            f.setResult(rs.getInt("home_goals"), rs.getInt("away_goals"));
                        }
                        fixtures.add(f);
                    }
                }
            }
            return League.restore(new ArrayList<>(byId.values()), fixtures, new Random(seed));
        }
    }

    private long readSeed(Connection c, int saveId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT seed FROM saves WHERE id = ?")) {
            ps.setInt(1, saveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("এই id-র কোনো সেভ নেই: " + saveId);
                }
                return rs.getLong("seed");
            }
        }
    }

    private Map<Integer, String> readTeamNames(Connection c, int saveId) throws SQLException {
        String sql = "SELECT t.id, t.name FROM save_teams st JOIN teams t ON t.id = st.team_id "
                + "WHERE st.save_id = ? ORDER BY t.name";
        Map<Integer, String> names = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.put(rs.getInt("id"), rs.getString("name"));
                }
            }
        }
        return names;
    }
}