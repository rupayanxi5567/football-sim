package league;

import models.Team;

public class Fixture {
    private final int round;
    private final Team home, away;
    private boolean played = false;
    private int homeGoals, awayGoals;

    public Fixture(int round, Team home, Team away) {
        this.round = round;
        this.home = home;
        this.away = away;
    }

    public void setResult(int homeGoals, int awayGoals) {
        if (played) {
            throw new IllegalStateException("Fixture already played");
        }
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.played = true;
    }

    public int getRound() {
        return round;
    }

    public Team getHome() {
        return home;
    }

    public Team getAway() {
        return away;
    }

    public boolean isPlayed() {
        return played;
    }

    public int getHomeGoals() {
        return homeGoals;
    }

    public int getAwayGoals() {
        return awayGoals;
    }
}
