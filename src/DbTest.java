import engine.MatchEngine;
import models.Team;
import storage.MySqlTeamRepository;
import storage.TeamRepository;

import java.util.Random;

public class DbTest {
    public static void main(String[] args) throws Exception {
        TeamRepository repo = new MySqlTeamRepository();

        Team madrid = repo.loadClub("Real Madrid");
        Team fcb = repo.loadClub("FC Barcelona");

        System.out.println("Squad size: " + madrid.getSquad().size() + " / " + fcb.getSquad().size());
        System.out.println();
        System.out.println(madrid.getLineupText());
        System.out.println();
        System.out.println(fcb.getLineupText());
        System.out.println();

        MatchEngine m = new MatchEngine(madrid, fcb, new Random(42));
        m.playKnockOut();
        System.out.println("Final: " + m.getHomeGoals() + "-" + m.getAwayGoals());
    }
}