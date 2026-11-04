import engine.MatchEngine;
import models.Team;
import storage.MySqlTeamRepository;
import storage.TeamRepository;

import java.util.Random;

public class DbTest {
    public static void main(String[] args) throws Exception {
        TeamRepository repo = new MySqlTeamRepository();

        Team por = repo.loadNationalTeam("Portugal");
        Team arg = repo.loadNationalTeam("Argentina");

        System.out.println("Squad size: " + por.getSquad().size() + " / " + arg.getSquad().size());
        System.out.println();
        System.out.println(por.getLineupText());
        System.out.println();
        System.out.println(arg.getLineupText());
        System.out.println();

        MatchEngine m = new MatchEngine(por, arg, new Random(892));
        m.playKnockOut();
        System.out.println("Final: " + m.getHomeGoals() + "-" + m.getAwayGoals());
    }
}