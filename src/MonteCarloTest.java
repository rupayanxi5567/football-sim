import engine.MatchEngine;
import models.Team;
import storage.MySqlTeamRepository;
import storage.TeamRepository;

import java.util.Random;

public class MonteCarloTest {
    public static void main(String[] args) throws Exception {
        TeamRepository repo = new MySqlTeamRepository();
        Team strong = repo.loadClub("Liverpool");
        Team weak = repo.loadClub("Brentford");
        Team chelsea = repo.loadClub("Chelsea");

        System.out.println("== Strong (home) vs Weak ==");
        run(strong, weak, 3000);
        System.out.println("== Weak (home) vs Strong ==");
        run(weak, strong, 3000);
        System.out.println("== Strong (home) vs Mid ==");
        run(strong, chelsea, 3000);
        System.out.println("== Mid (home) vs Strong ==");
        run(chelsea, strong, 3000);
        System.out.println("== Mid (home) vs Weak ==");
        run(chelsea, weak, 3000);
        System.out.println("== Mirror (Chelsea vs Chelsea) ==");
        run(chelsea, chelsea, 3000);
    }

    private static void run(Team home, Team away, int runs) {
        Random rng = new Random(1);
        int hw = 0, d = 0, aw = 0;
        long hg = 0, ag = 0, hs = 0, as = 0;
        double hp = 0;

        for (int i = 0; i < runs; i++) {
            MatchEngine m = new MatchEngine(home, away, rng);
            m.setVerbose(false);
            m.play();
            hg += m.getHomeGoals();
            ag += m.getAwayGoals();
            hs += m.getHomeShots();
            as += m.getAwayShots();
            hp += m.getHomePossessionPercent();
            if (m.getHomeGoals() > m.getAwayGoals()) hw++;
            else if (m.getHomeGoals() < m.getAwayGoals()) aw++;
            else d++;
        }
        System.out.printf("%s (ATT %d MID %d DEF %d)  vs  %s (ATT %d MID %d DEF %d)%n",
                home.getName(), home.getAttack(), home.getMidField(), home.getDefense(),
                away.getName(), away.getAttack(), away.getMidField(), away.getDefense());
        System.out.printf("Home wins %.1f%%  Draws %.1f%%  Away wins %.1f%%%n",
                100.0 * hw / runs, 100.0 * d / runs, 100.0 * aw / runs);
        System.out.printf("Avg goals %.2f - %.2f  (total %.2f)  | shots %.1f - %.1f  | home possession %.1f%%%n%n",
                (double) hg / runs, (double) ag / runs, (double) (hg + ag) / runs,
                (double) hs / runs, (double) as / runs, hp / runs);
    }
}