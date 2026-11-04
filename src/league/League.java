package league;

import engine.MatchEngine;
import models.Team;

import java.util.*;

public class League {
    private final List<Team> teams;
    private final List<Fixture> fixtures = new ArrayList<>();
    private final Map<String, TableEntry> table = new LinkedHashMap<>();
    private final Random rng;

    public League(List<Team> teams, Random rng) {
        this(teams, rng, true);
    }

    private League(List<Team> teams, Random rng, boolean generate) {
        if (teams.size() % 2 == 1) {
            throw new IllegalArgumentException("Even number of teams needed");
        }
        this.teams = teams;
        this.rng = rng;
        for (Team t : teams) {
            table.put(t.getName(), new TableEntry(t.getName()));
        }
        if (generate) {
            generateFixtures();
        }
    }

    // সেভ করা ফিক্সচার থেকে লিগ ফিরিয়ে আনে; পয়েন্ট টেবিল ফল থেকে আবার হিসাব হয়
    public static League restore(List<Team> teams, List<Fixture> savedFixtures, Random rng) {
        League league = new League(teams, rng, false);
        for (Fixture f : savedFixtures) {
            league.fixtures.add(f);
            if (f.isPlayed()) {
                league.record(f);
            }
        }
        return league;
    }

    private void generateFixtures() {
        List<Team> rot = new ArrayList<>(teams);
        int n = rot.size();
        int legRounds = n - 1;
        List<Fixture> firstLeg = new ArrayList<>();
        for (int r = 0; r < legRounds; r++) {
            for (int i = 0; i < n / 2; i++) {
                Team a = rot.get(i);
                Team b = rot.get(n - i - 1);
                firstLeg.add(r % 2 == 0 ? new Fixture(r + 1, a, b) : new Fixture(r + 1, b, a));
            }
            rot.add(1, rot.remove(n - 1));
        }
        fixtures.addAll(firstLeg);
        for (Fixture f : firstLeg) {
            fixtures.add(new Fixture(f.getRound() + legRounds, f.getAway(), f.getHome()));
        }
    }

    private void record(Fixture f) {
        table.get(f.getHome().getName()).record(f.getHomeGoals(), f.getAwayGoals());
        table.get(f.getAway().getName()).record(f.getAwayGoals(), f.getHomeGoals());
    }

    public void playRound(int round) {
        for (Fixture f : fixtures) {
            if (f.getRound() != round || f.isPlayed()) {
                continue;
            }
            MatchEngine m = new MatchEngine(f.getHome(), f.getAway(), rng);
            m.setVerbose(false);
            m.play();
            f.setResult(m.getHomeGoals(), m.getAwayGoals());
            record(f);
        }
    }

    public void playAllRounds() {
        for (int r = 1; r <= getTotalRounds(); r++) {
            playRound(r);
        }
    }

    public int getTotalRounds() {
        return 2 * (teams.size() - 1);
    }

    public List<Team> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    public List<Fixture> getFixtures() {
        return Collections.unmodifiableList(fixtures);
    }

    public List<TableEntry> getSortedTable() {
        List<TableEntry> list = new ArrayList<>(table.values());
        list.sort(Comparator.comparingInt(TableEntry::getPoints)
                .thenComparingInt(TableEntry::getGoalDiff)
                .thenComparingInt(t -> t.goalsFor)
                .reversed()
                .thenComparing(t -> t.teamName));
        return list;
    }
}