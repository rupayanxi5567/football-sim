import engine.MatchEngine;
import league.Fixture;
import league.League;
import league.TableEntry;
import models.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {

        List<Team> teams = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            teams.add(Team.random("Team" + i, 1 + i % 5));
        }


        League league = new League(teams, new Random(42));
        league.playAllRounds();

        printTable(league);
        runChecks(league);
    }

    private static void printTable(League league) {
        System.out.println();
        System.out.printf("%-3s %-10s %2s %2s %2s %2s %3s %3s %4s %4s%n",
                "#", "Team", "P", "W", "D", "L", "GF", "GA", "GD", "Pts");
        System.out.println("-".repeat(48));

        int pos = 1;
        for (TableEntry e : league.getSortedTable()) {
            System.out.printf("%-3d %-10s %2d %2d %2d %2d %3d %3d %4d %4d%n",
                    pos++, e.teamName, e.played, e.won, e.drawn, e.lost,
                    e.goalsFor, e.goalsAgainst, e.getGoalDiff(), e.getPoints());
        }
    }


    private static void runChecks(League league) {
        System.out.println();
        System.out.println("---- Checkpoints ----");

        int fixtureCount = league.getFixtures().size();
        System.out.println("Fixtures = " + fixtureCount + (fixtureCount == 56 ? "  OK" : "  WRONG (expected 56)"));

        boolean allPlayed14 = true;
        int totalFor = 0, totalAgainst = 0;
        for (TableEntry e : league.getSortedTable()) {
            if (e.played != 14) allPlayed14 = false;
            totalFor += e.goalsFor;
            totalAgainst += e.goalsAgainst;
        }
        System.out.println("Every team played 14 = " + (allPlayed14 ? "OK" : "WRONG"));
        System.out.println("Total GF (" + totalFor + ") == Total GA (" + totalAgainst + ") = "
                + (totalFor == totalAgainst ? "OK" : "WRONG"));

        boolean allFixturesPlayed = true;
        for (Fixture f : league.getFixtures()) {
            if (!f.isPlayed()) allFixturesPlayed = false;
        }
        System.out.println("All fixtures played = " + (allFixturesPlayed ? "OK" : "WRONG"));
    }
}