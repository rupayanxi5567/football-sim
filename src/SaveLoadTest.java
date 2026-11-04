import league.Fixture;
import league.League;
import league.TableEntry;
import storage.LeagueRepository;
import storage.MySqlTeamRepository;
import storage.TeamRepository;

import java.util.List;
import java.util.Random;

public class SaveLoadTest {
    public static void main(String[] args) throws Exception {
        TeamRepository teamRepo = new MySqlTeamRepository();
        LeagueRepository leagueRepo = new LeagueRepository(teamRepo);

        long seed = 428286;
        League league = new League(teamRepo.loadLeague("Premier League"), new Random(seed));

        // অর্ধেক সিজন খেলে সেভ
        int half = league.getTotalRounds() / 2;
        for (int r = 1; r <= half; r++) {
            league.playRound(r);
        }
        int saveId = leagueRepo.save("test-save", seed, league);
        System.out.println("Saved with id " + saveId);

        // লোড করে মেলানো
        League loaded = leagueRepo.load(saveId);
        System.out.println("Fixtures loaded = " + loaded.getFixtures().size()
                + (loaded.getFixtures().size() == league.getFixtures().size() ? "  OK" : "  WRONG"));
        System.out.println("Table identical after load = " + (sameTable(league, loaded) ? "OK" : "WRONG"));

        // লোড করা লিগে বাকি অর্ধেক খেলা যায় কিনা
        for (int r = half + 1; r <= loaded.getTotalRounds(); r++) {
            loaded.playRound(r);
        }
        boolean allPlayed = true;
        for (Fixture f : loaded.getFixtures()) {
            if (!f.isPlayed()) allPlayed = false;
        }
        System.out.println("Season finished after load = " + (allPlayed ? "OK" : "WRONG"));
        System.out.println("Champion: " + loaded.getSortedTable().get(0).teamName);
    }

    private static boolean sameTable(League a, League b) {
        List<TableEntry> x = a.getSortedTable();
        List<TableEntry> y = b.getSortedTable();
        if (x.size() != y.size()) return false;
        for (int i = 0; i < x.size(); i++) {
            TableEntry p = x.get(i), q = y.get(i);
            if (!p.teamName.equals(q.teamName) || p.played != q.played || p.won != q.won
                    || p.drawn != q.drawn || p.lost != q.lost
                    || p.goalsFor != q.goalsFor || p.goalsAgainst != q.goalsAgainst) {
                return false;
            }
        }
        return true;
    }
}