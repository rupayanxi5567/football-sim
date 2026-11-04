import league.Fixture;
import league.League;
import league.TableEntry;
import models.Team;
import storage.MySqlTeamRepository;
import storage.TeamRepository;

import java.util.List;
import java.util.Random;

public class LeagueTest {
    public static void main(String[] args) throws Exception {
        TeamRepository repo = new MySqlTeamRepository();

        String leagueName = "Premier League";      // "La Liga"
        List<Team> teams = repo.loadLeague(leagueName);
        System.out.println(leagueName + ": " + teams.size() + " clubs loaded");

        League league = new League(teams, new Random(428286));

        long start = System.currentTimeMillis();
        league.playAllRounds();
        long ms = System.currentTimeMillis() - start;
        System.out.println("Season simulated in " + ms + " ms");

        printTable(league);
        runChecks(league, teams.size());
    }

    private static void printTable(League league) {
        System.out.println();
        System.out.printf("%-3s %-26s %2s %2s %2s %2s %3s %3s %4s %4s%n",
                "#", "Club", "P", "W", "D", "L", "GF", "GA", "GD", "Pts");
        System.out.println("-".repeat(62));
        int pos = 1;
        for (TableEntry e : league.getSortedTable()) {
            System.out.printf("%-3d %-26s %2d %2d %2d %2d %3d %3d %4d %4d%n",
                    pos++, e.teamName, e.played, e.won, e.drawn, e.lost,
                    e.goalsFor, e.goalsAgainst, e.getGoalDiff(), e.getPoints());
        }
    }

    private static void runChecks(League league, int n) {
        System.out.println();
        System.out.println("---- Checkpoints ----");

        int expectedFixtures = n * (n - 1);
        int expectedPlayed = 2 * (n - 1);

        int fixtureCount = league.getFixtures().size();
        System.out.println("Fixtures = " + fixtureCount
                + (fixtureCount == expectedFixtures ? "  OK" : "  WRONG (expected " + expectedFixtures + ")"));

        boolean allPlayedOk = true;
        int totalFor = 0, totalAgainst = 0;
        for (TableEntry e : league.getSortedTable()) {
            if (e.played != expectedPlayed) allPlayedOk = false;
            totalFor += e.goalsFor;
            totalAgainst += e.goalsAgainst;
        }
        System.out.println("Every team played " + expectedPlayed + " = " + (allPlayedOk ? "OK" : "WRONG"));
        System.out.println("Total GF (" + totalFor + ") == Total GA (" + totalAgainst + ") = "
                + (totalFor == totalAgainst ? "OK" : "WRONG"));

        boolean allFixturesPlayed = true;
        for (Fixture f : league.getFixtures()) {
            if (!f.isPlayed()) allFixturesPlayed = false;
        }
        System.out.println("All fixtures played = " + (allFixturesPlayed ? "OK" : "WRONG"));

        System.out.printf("Goals per match = %.2f%n", (double) totalFor / fixtureCount);
    }
}