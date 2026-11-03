import engine.Commentary;
import engine.EventType;
import engine.MatchEngine;
import engine.MatchEvent;
import models.Formation;
import models.Player;
import models.Position;
import models.Team;

import java.util.Random;

class LiveMatch {

    // ---------- সেটিংস (এখানেই বদলাও) ----------
    static final boolean FORCE_SUDDEN_DEATH = true;  // true: সাডেন ডেথ পর্যন্ত গড়ানো ম্যাচ খুঁজে দেখাবে
    static final int DELAY_MS = 2000;                   // প্রতি ঘটনার পর বিরতি (মিলিসেকেন্ড), 0 দিলে সব একসাথে ছাপা হবে
    static final int MAX_SEEDS = 200000;             // খোঁজার সর্বোচ্চ চেষ্টা

    // Team.random-এর layout-এর সাথে হুবহু একই ক্রম, নামের তালিকাও এই ক্রমে
    static final Position[] LAYOUT = {
            Position.GK, Position.GK,
            Position.CB, Position.CB, Position.CB, Position.LB, Position.RB, Position.CB,
            Position.DMF, Position.CMF, Position.CMF,
            Position.AMF, Position.AMF, Position.AMF,
            Position.LMF, Position.RMF,
            Position.CF, Position.CF, Position.LWF, Position.RWF
    };

    // নাম আমার জানা স্কোয়াড থেকে, বদলে গিয়ে থাকতে পারে। নিজের ইচ্ছামতো বদলাও।
    // রেটিং আসল নয়, তোমার Player.randomPlayer নাম থেকে বানাচ্ছে।
    static final String[] REAL_NAMES = {
            "Courtois", "Lunin",
            "Militao", "Rudiger", "Huijsen", "Carreras", "Carvajal", "Asencio",
            "Tchouameni", "Valverde", "Camavinga",
            "Bellingham", "Guler", "Brahim Diaz",
            "Ceballos", "Mastantuono",
            "Mbappe", "Endrick", "Vinicius Junior", "Rodrygo"
    };

    static final String[] BARCA_NAMES = {
            "Szczesny", "Joan Garcia",
            "Cubarsi", "Araujo", "Christensen", "Balde", "Kounde", "Gerard Martin",
            "Casado", "Pedri", "De Jong",
            "Olmo", "Fermin Lopez", "Gavi",
            "Bernal", "Bardghji",
            "Lewandowski", "Ferran Torres", "Raphinha", "Lamine Yamal"
    };

    // ---------- main ----------
    public static void main(String[] args) {
        Team real = namedTeam("REAL MADRID", REAL_NAMES);
        Team barca = namedTeam("FC BARCELONA", BARCA_NAMES);
        real.setFormation(Formation.F_4231);
        barca.setFormation(Formation.F_433);

        long seed;
        if (FORCE_SUDDEN_DEATH) {
            System.out.println("সাডেন ডেথ পর্যন্ত গড়ানো একটা ম্যাচ খুঁজছি...");
            seed = findSuddenDeathSeed(real, barca);
            if (seed < 0) {
                System.out.println("এই সীমার মধ্যে এমন ম্যাচ পাওয়া যায়নি, MAX_SEEDS বাড়াও।");
                return;
            }
        } else {
            seed = System.nanoTime();
        }

        // ম্যাচটা ইঞ্জিনে চালাই (ছাপা বন্ধ), তারপর ইভেন্টগুলো ধীরে ধীরে দেখাই
        MatchEngine match = new MatchEngine(real, barca, new Random(seed));
        match.setVerbose(false);
        match.playKnockOut();

        printPreMatch(real, barca, seed);
        broadcast(match);
        printSummary(match, real, barca);
    }

    // ---------- দল বানানো ----------
    static Team namedTeam(String teamName, String[] names) {
        if (names.length != LAYOUT.length) {
            throw new IllegalArgumentException(teamName + ": " + LAYOUT.length
                    + "টা নাম লাগবে, আছে " + names.length);
        }
        Team t = new Team(teamName);
        for (int i = 0; i < LAYOUT.length; i++) {
            t.addPlayer(Player.randomPlayer(names[i], LAYOUT[i], 5));
        }
        return t;
    }

    // ---------- সাডেন ডেথের সিড খোঁজা ----------
    static long findSuddenDeathSeed(Team home, Team away) {
        for (long s = 1; s <= MAX_SEEDS; s++) {
            MatchEngine m = new MatchEngine(home, away, new Random(s));
            m.setVerbose(false);
            m.playKnockOut();
            if (wentToSuddenDeath(m)) {
                return s;
            }
        }
        return -1;
    }

    // শুটআউটে সর্বোচ্চ ১০টা সাধারণ কিক, তার বেশি কিক মানে সাডেন ডেথ হয়েছে
    static boolean wentToSuddenDeath(MatchEngine m) {
        int kicks = 0;
        for (MatchEvent e : m.getMatchEvents()) {
            if (isKick(e)) kicks++;
        }
        return kicks > 10;
    }

    static boolean isKick(MatchEvent e) {
        return e.getType() == EventType.PENALTY_SCORED || e.getType() == EventType.PENALTY_MISSED;
    }

    // ---------- ম্যাচের আগের অংশ ----------
    static void printPreMatch(Team home, Team away, long seed) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("  " + home.getName() + "  vs  " + away.getName());
        System.out.println("  Knockout tie: extra time and penalties if needed");
        System.out.println("  (match seed: " + seed + ")");
        System.out.println("==================================================");
        System.out.println();
        System.out.println(home.getLineupText());
        System.out.println();
        System.out.println(away.getLineupText());
        System.out.println();
        System.out.println("--------------------------------------------------");
        pause(DELAY_MS * 4);
    }

    // ---------- সরাসরি সম্প্রচার ----------
    static void broadcast(MatchEngine match) {
        int kicks = 0;
        for (MatchEvent e : match.getMatchEvents()) {
            if (isKick(e)) {
                kicks++;
                if (kicks == 11) {
                    System.out.println();
                    System.out.println("========== SUDDEN DEATH ==========");
                    pause(DELAY_MS * 3);
                }
            }
            System.out.println(Commentary.describe(e));
            pause(delayFor(e));
        }
    }

    static int delayFor(MatchEvent e) {
        switch (e.getType()) {
            case GOAL: return DELAY_MS * 3;
            case HALF_TIME: case FULL_TIME: case EXTRA_TIME_START:
            case EXTRA_TIME_HALF: case EXTRA_TIME_END: case PENALTY_SHOOTOUT_START:
                return DELAY_MS * 3;
            case PENALTY_SCORED: case PENALTY_MISSED:
                return DELAY_MS * 2;
            default:
                return DELAY_MS;
        }
    }

    static void pause(int ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------- ম্যাচ শেষের সারাংশ ----------
    static void printSummary(MatchEngine m, Team home, Team away) {
        System.out.println();
        System.out.println("================ MATCH SUMMARY ================");
        System.out.println(home.getName() + " " + m.getHomeGoals() + " - " + m.getAwayGoals()
                + " " + away.getName() + "   (including extra time)");

        MatchEvent lastKick = null;
        for (MatchEvent e : m.getMatchEvents()) {
            if (isKick(e)) lastKick = e;
        }
        if (lastKick != null) {
            System.out.println("Penalty shoot-out: " + home.getName() + " " + lastKick.getHomeScore()
                    + " - " + lastKick.getAwayScore() + " " + away.getName());
        }
        System.out.println("Winner: " + m.getWinner().getName());

        System.out.printf("Shots: %d - %d | On target: %d - %d | Possession: %.0f%% - %.0f%%%n",
                m.getHomeShots(), m.getAwayShots(),
                m.getHomeOnTarget(), m.getAwayOnTarget(),
                m.getHomePossessionPercent(), m.getAwayPossessionPercent());

        System.out.println();
        System.out.println("Goals:");
        boolean any = false;
        for (MatchEvent e : m.getMatchEvents()) {
            if (e.getType() == EventType.GOAL) {
                any = true;
                String assist = (e.getSecondPlayer() != null)
                        ? "  (assist: " + e.getSecondPlayer().getName() + ")" : "";
                System.out.println("  " + stamp(e) + "  " + e.getPlayer().getName()
                        + " [" + e.getTeam().getName() + "]" + assist);
            }
        }
        if (!any) {
            System.out.println("  (no goals in open play)");
        }
        System.out.println("===============================================");
    }

    static String stamp(MatchEvent e) {
        String l = e.getLabel();
        return (l == null || l.isEmpty() ? String.valueOf(e.getMinute()) : l) + "'";
    }
}