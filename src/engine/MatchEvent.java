package engine;

import models.*;

public class MatchEvent {
    private final int minute;
    private final EventType type;
    private final Team team;
    private final Player player;
    private final Player secondPlayer;
    private final String label;
    private final int homeScore;
    private final int awayScore;

    public MatchEvent(int minute, EventType type, Team team, Player player,String label,int homeScore,int awayScore,Player secondPlayer) {
        this.minute = minute;
        this.type = type;
        this.homeScore=homeScore;
        this.secondPlayer=secondPlayer;
        this.awayScore=awayScore;
        this.team = team;
        this.player = player;
        this.label = label;
    }
    public MatchEvent(int minute, String label, EventType type, Team team, Player player, int homeScore, int awayScore) {
        this(minute, type, team, player, label, homeScore, awayScore, null);
    }

    @Override
    public String toString() {
        return "MatchEvent{" +
                "minute=" + minute +
                ", type=" + type +
                ", team=" + (team == null ? "-" : team.getName()) +
                ", player=" + player +
                ", secondPlayer=" + secondPlayer +
                ", label='" + label + '\'' +
                ", homeScore=" + homeScore +
                ", awayScore=" + awayScore +
                '}';
    }

    public int getAwayScore() {
        return awayScore;
    }
    public Player getSecondPlayer() {
        return secondPlayer;
    }
    public int getHomeScore() {
        return homeScore;
    }
    public String getLabel() {
        return label;
    }
    public EventType getType() {
        return type;
    }
    public int getMinute() {
        return minute;
    }
    public Team getTeam() {
        return team;
    }
    public Player getPlayer() {
        return player;
    }
}
