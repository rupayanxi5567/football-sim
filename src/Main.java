import engine.MatchEngine;
import models.*;

import java.util.*;

public class Main {

    static int passed = 0;
    static int failed = 0;
    static final int LEVEL = 5;

    // ---------- সহায়ক মেথড ----------
    static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("  [PASS] " + name);
        } else {
            failed++;
            System.out.println("  [FAIL] " + name);
        }
    }

    static boolean between(double v, double lo, double hi) {
        return v >= lo && v <= hi;
    }

    static void title(String t) {
        System.out.println("\n========== " + t + " ==========");
    }

    // ছাপা বন্ধ রাখা ইঞ্জিন
    static MatchEngine quiet(Team h, Team a, Random rng) {
        MatchEngine m = new MatchEngine(h, a, rng);
        m.setVerbose(false);
        return m;
    }

    // হুবহু একই শক্তির আলাদা অবজেক্ট (নাম থেকে সিড আসে বলে রেটিং একই হয়)
    static Team twin(Team t, Formation f) {
        Team copy = Team.random(t.getName(), LEVEL);
        copy.setFormation(f);
        return copy;
    }

    // ---------- main ----------
    public static void main(String[] args) {
        Team barca = Team.random("FC BARCELONA", LEVEL);
        Team real = Team.random("REAL MADRID FC", LEVEL);
        barca.setFormation(Formation.F_4321);
        real.setFormation(Formation.F_4321);

        testTeam(barca);
        testTeam(real);
        testOneMatch(barca, real);
        testPenaltyMatchCommentary(barca, real);
        testDeterminism(barca, real);
        testInvariants(barca, real, 5000);
        testRegularTime(barca, real, 10000);
        testRegularTime(real, barca, 10000);
        testHomeAdvantage(barca, 10000);
        testStrength();
        testKnockout(barca, real, 10000);
        testSuddenDeath(barca, real, 10000);

        System.out.println("\n===== সারাংশ: " + passed + " PASS, " + failed + " FAIL =====");
    }

    // ---------- ১. Team ----------
    static void testTeam(Team t) {
        title("Team: " + t.getName());
        Formation original = t.getFormation();
        Formation[] all = {Formation.F_4312, Formation.F_4321, Formation.F_3421, Formation.F_3412};

        for (Formation f : all) {
            t.setFormation(f);
            List<Player> xi = t.getStartingEleven();
            long gk = xi.stream().filter(p -> p.getPosition().isGoalkeeper()).count();
            check(f.getLabel() + ": একাদশে ১১ জন", xi.size() == 11);
            check(f.getLabel() + ": ঠিক ১ জন গোলকিপার", gk == 1);
            check(f.getLabel() + ": কেউ দুবার নেই", new HashSet<>(xi).size() == xi.size());
        }
        t.setFormation(original);

        check("attack ১–৯৯", between(t.getAttack(), 1, 99));
        check("midfield ১–৯৯", between(t.getMidField(), 1, 99));
        check("defense ১–৯৯", between(t.getDefense(), 1, 99));
        check("গড় স্ট্যামিনা ১–৯৯", between(t.getAverageStamina(), 1, 99));
        check("গোলকিপার রেটিং ১–৯৯", between(t.getGoalKeeperRating(), 1, 99));

        List<Player> takers = t.getPenaltyTakers();
        boolean noGk = takers.stream().noneMatch(p -> p.getPosition().isGoalkeeper());
        boolean sorted = true;
        for (int i = 1; i < takers.size(); i++) {
            if (takers.get(i - 1).getShooting() < takers.get(i).getShooting()) sorted = false;
        }
        check("পেনাল্টি নেওয়ার তালিকায় ১০ জন", takers.size() == 10);
        check("তালিকায় গোলকিপার নেই", noGk);
        check("সেরা শুটার আগে (শুটিং কমতে কমতে)", sorted);

        System.out.println(t.getLineupText());
    }

    // ---------- ২. একটা ম্যাচ চোখে দেখা ----------
    static void testOneMatch(Team home, Team away) {
        title("একটা ম্যাচ চোখে দেখো (সিড ৪২)");
        MatchEngine m = new MatchEngine(home, away, new Random(42));
        m.play();
        printSummary(home, away, m);
    }

    static void printSummary(Team home, Team away, MatchEngine m) {
        System.out.println(home.getName() + " " + m.getHomeGoals() + " - " + m.getAwayGoals()
                + " " + away.getName());
        System.out.printf("শট: %d - %d | লক্ষ্যে: %d - %d | দখল: %.0f%% - %.0f%%%n",
                m.getHomeShots(), m.getAwayShots(),
                m.getHomeOnTarget(), m.getAwayOnTarget(),
                m.getHomePossessionPercent(), m.getAwayPossessionPercent());
    }

    // ---------- ৩. পেনাল্টি পর্যন্ত গড়ানো ম্যাচের কমেন্ট্রি ----------
    static void testPenaltyMatchCommentary(Team home, Team away) {
        title("পেনাল্টিতে গড়ানো একটা ম্যাচ (নিজের চোখে মিলিয়ে দেখো)");
        int seed = -1;
        for (int s = 0; s < 20000 && seed < 0; s++) {
            MatchEngine m = quiet(home, away, new Random(s));
            m.play();
            if (m.getHomeGoals() == m.getAwayGoals()) {
                m.result();
                if (m.getHomeGoals() == m.getAwayGoals()) {
                    seed = s;
                }
            }
        }
        if (seed < 0) {
            System.out.println("এমন সিড পাওয়া যায়নি");
            return;
        }
        System.out.println("সিড " + seed + " দিয়ে আবার চালাচ্ছি (verbose চালু)");
        MatchEngine m = new MatchEngine(home, away, new Random(seed));
        m.playKnockOut();
        System.out.println("বিজয়ী: " + (m.getWinner() == null ? "null" : m.getWinner().getName()));
    }

    // ---------- ৪. একই সিডে একই ফল ----------
    static void testDeterminism(Team home, Team away) {
        title("একই সিডে একই ফল");
        MatchEngine a = quiet(home, away, new Random(7));
        MatchEngine b = quiet(home, away, new Random(7));
        a.play();
        b.play();
        check("গোল একই", a.getHomeGoals() == b.getHomeGoals() && a.getAwayGoals() == b.getAwayGoals());
        check("শট একই", a.getHomeShots() == b.getHomeShots() && a.getAwayShots() == b.getAwayShots());
        check("লক্ষ্যে শট একই", a.getHomeOnTarget() == b.getHomeOnTarget()
                && a.getAwayOnTarget() == b.getAwayOnTarget());
        check("দখল একই", a.getHomePossessionPercent() == b.getHomePossessionPercent());
    }

    // ---------- ৫. নিয়ম যা কখনো ভাঙা চলবে না ----------
    static void testInvariants(Team home, Team away, int n) {
        title("অবিচ্ছেদ্য নিয়ম (" + n + " ম্যাচ)");
        Random rng = new Random(1);
        int badFunnel = 0, badPossession = 0;

        for (int i = 0; i < n; i++) {
            MatchEngine m = quiet(home, away, rng);
            m.play();

            boolean funnelOk = m.getHomeShots() >= m.getHomeOnTarget()
                    && m.getHomeOnTarget() >= m.getHomeGoals()
                    && m.getAwayShots() >= m.getAwayOnTarget()
                    && m.getAwayOnTarget() >= m.getAwayGoals();
            if (!funnelOk) badFunnel++;

            double sum = m.getHomePossessionPercent() + m.getAwayPossessionPercent();
            if (Math.abs(sum - 100.0) > 0.001) badPossession++;
        }
        check("শট ≥ লক্ষ্যে শট ≥ গোল (প্রতিটা ম্যাচে, দুই দলের)", badFunnel == 0);
        check("দখল দুই দল মিলিয়ে ১০০%", badPossession == 0);
    }

    // ---------- ৬. ৯০ মিনিটের পরিসংখ্যান ----------
    static void testRegularTime(Team home, Team away, int n) {
        title(home.getName() + " (হোম) বনাম " + away.getName() + ", " + n + " ম্যাচ");
        Random rng = new Random(2);

        long homeGoals = 0, awayGoals = 0, homeShots = 0, awayShots = 0;
        long homeOT = 0, awayOT = 0;
        double homePoss = 0;
        int homeWins = 0, draws = 0, awayWins = 0, fivePlus = 0;
        Map<String, Integer> scorelines = new HashMap<>();

        for (int i = 0; i < n; i++) {
            MatchEngine m = quiet(home, away, rng);
            m.play();
            int hg = m.getHomeGoals(), ag = m.getAwayGoals();

            homeGoals += hg;
            awayGoals += ag;
            homeShots += m.getHomeShots();
            awayShots += m.getAwayShots();
            homeOT += m.getHomeOnTarget();
            awayOT += m.getAwayOnTarget();
            homePoss += m.getHomePossessionPercent();

            if (hg > ag) homeWins++;
            else if (hg == ag) draws++;
            else awayWins++;
            if (hg + ag >= 5) fivePlus++;
            scorelines.merge(hg + "-" + ag, 1, Integer::sum);
        }

        double avgTotal = (double) (homeGoals + awayGoals) / n;
        double shotsPerTeam = (double) (homeShots + awayShots) / (2.0 * n);
        double otPerTeam = (double) (homeOT + awayOT) / (2.0 * n);
        double drawPct = 100.0 * draws / n;
        double avgHomePoss = homePoss / n;
        double zeroZeroPct = 100.0 * scorelines.getOrDefault("0-0", 0) / n;
        double fivePlusPct = 100.0 * fivePlus / n;

        System.out.printf("গড় গোল: হোম %.2f, অ্যাওয়ে %.2f, মোট %.2f%n",
                (double) homeGoals / n, (double) awayGoals / n, avgTotal);
        System.out.printf("প্রতি দলে গড় শট %.1f, লক্ষ্যে %.1f%n", shotsPerTeam, otPerTeam);
        System.out.printf("হোম জয় %.1f%%, ড্র %.1f%%, অ্যাওয়ে জয় %.1f%%%n",
                100.0 * homeWins / n, drawPct, 100.0 * awayWins / n);
        System.out.printf("হোমের গড় দখল %.1f%%, ০-০ %.1f%%, ৫+ গোলের ম্যাচ %.1f%%%n",
                avgHomePoss, zeroZeroPct, fivePlusPct);

        List<Map.Entry<String, Integer>> top = new ArrayList<>(scorelines.entrySet());
        top.sort((x, y) -> y.getValue() - x.getValue());
        System.out.print("সবচেয়ে বেশি স্কোরলাইন: ");
        for (int i = 0; i < Math.min(6, top.size()); i++) {
            System.out.printf("%s (%.1f%%)  ", top.get(i).getKey(), 100.0 * top.get(i).getValue() / n);
        }
        System.out.println();

        check("গড় মোট গোল ২.৩–৩.২", between(avgTotal, 2.3, 3.2));
        check("প্রতি দলে শট ৯–১৫", between(shotsPerTeam, 9, 15));
        check("প্রতি দলে লক্ষ্যে শট ৩–৬.৫", between(otPerTeam, 3, 6.5));
        check("ড্র ১৮–৩০%", between(drawPct, 18, 30));
        check("হোমের দখল ৪০–৬০%", between(avgHomePoss, 40, 60));
        check("০-০ ম্যাচ ৩–১৩%", between(zeroZeroPct, 3, 13));
        check("৫+ গোলের ম্যাচ ১৬%-এর নিচে", fivePlusPct < 16);
    }

    // ---------- ৭. ঘরের মাঠের সুবিধা (হুবহু সমান দুই দল) ----------
    static void testHomeAdvantage(Team t, int n) {
        title("ঘরের মাঠের সুবিধা (সমান শক্তির দুই দল)");
        Team twin = twin(t, t.getFormation());
        Random rng = new Random(3);

        long hg = 0, ag = 0;
        int homeWins = 0, awayWins = 0;
        for (int i = 0; i < n; i++) {
            MatchEngine m = quiet(t, twin, rng);
            m.play();
            hg += m.getHomeGoals();
            ag += m.getAwayGoals();
            if (m.getHomeGoals() > m.getAwayGoals()) homeWins++;
            else if (m.getHomeGoals() < m.getAwayGoals()) awayWins++;
        }
        double diff = (double) (hg - ag) / n;
        System.out.printf("গড় গোল: হোম %.2f, অ্যাওয়ে %.2f, পার্থক্য %.2f%n",
                (double) hg / n, (double) ag / n, diff);
        System.out.printf("হোম জয় %.1f%%, অ্যাওয়ে জয় %.1f%%%n", 100.0 * homeWins / n, 100.0 * awayWins / n);

        check("হোম গড়ে বেশি গোল করছে", diff > 0);
        check("পার্থক্য ০.০৫–০.৫", between(diff, 0.05, 0.5));
        check("হোম জয় অ্যাওয়ের চেয়ে বেশি", homeWins > awayWins);
    }

    // ---------- ৮. দলের শক্তি ফলে প্রভাব ফেলছে কিনা ----------
    static double[] favRates(int favLevel, int dogLevel, int pairs, int perPair) {
        Random rng = new Random(99);
        int fav = 0, draw = 0, dog = 0, total = 0;
        for (int p = 0; p < pairs; p++) {
            Team f = Team.random("FAV" + favLevel + "_" + p, favLevel);
            Team d = Team.random("DOG" + dogLevel + "_" + p, dogLevel);
            for (int i = 0; i < perPair; i++) {
                boolean favHome = (i % 2 == 0);          // একবার হোমে, একবার অ্যাওয়েতে
                MatchEngine m = favHome ? quiet(f, d, rng) : quiet(d, f, rng);
                m.play();
                int fg = favHome ? m.getHomeGoals() : m.getAwayGoals();
                int dg = favHome ? m.getAwayGoals() : m.getHomeGoals();
                if (fg > dg) fav++;
                else if (fg == dg) draw++;
                else dog++;
                total++;
            }
        }
        return new double[]{100.0 * fav / total, 100.0 * draw / total, 100.0 * dog / total};
    }

    static void testStrength() {
        title("দলের শক্তি বনাম ফল (২০ জোড়া × ৫০০ ম্যাচ প্রতিটা)");
        int[] dogLevels = {5, 4, 3, 1};
        double[] favWin = new double[dogLevels.length];

        for (int i = 0; i < dogLevels.length; i++) {
            double[] r = favRates(5, dogLevels[i], 20, 500);
            favWin[i] = r[0];
            System.out.printf("লেভেল ৫ বনাম %d: জয় %.1f%%, ড্র %.1f%%, হার %.1f%%%n",
                    dogLevels[i], r[0], r[1], r[2]);
        }
        check("ব্যবধান বাড়লে ফেভারিটের জয় বাড়ে (৫-৫ < ৫-৪ < ৫-৩ < ৫-১)",
                favWin[0] < favWin[1] && favWin[1] < favWin[2] && favWin[2] < favWin[3]);
        check("লেভেল ৫ বনাম ১: ফেভারিট ৭৫%+ জেতে", favWin[3] >= 75);
        check("লেভেল ৫ বনাম ১: তবু ১০০% নয় (অঘটন সম্ভব)", favWin[3] < 100);
    }

    // ---------- ৯. নকআউট ----------
    static void testKnockout(Team home, Team away, int n) {
        title("নকআউট (" + n + " ম্যাচ)");
        Random rng = new Random(5);

        // ক) playKnockOut(): বিজয়ী সবসময় আছে কিনা
        int nulls = 0, stranger = 0, homeWins = 0;
        for (int i = 0; i < n; i++) {
            MatchEngine m = quiet(home, away, rng);
            m.playKnockOut();
            Team w = m.getWinner();
            if (w == null) nulls++;
            else if (w != home && w != away) stranger++;
            else if (w == home) homeWins++;
        }
        System.out.printf("%s জয় %d, %s জয় %d%n", home.getName(), homeWins,
                away.getName(), n - homeWins - nulls - stranger);
        check("বিজয়ী null নয় (প্রতিটা ম্যাচে)", nulls == 0);
        check("বিজয়ী সবসময় দুই দলের একটা", stranger == 0);

        // খ) ধাপে ধাপে: কোথায় ফয়সালা হলো
        int in90 = 0, inET = 0, inPens = 0;
        for (int i = 0; i < n; i++) {
            MatchEngine m = quiet(home, away, rng);
            m.play();
            boolean level90 = m.getHomeGoals() == m.getAwayGoals();
            m.result();
            if (!level90) in90++;
            else if (m.getHomeGoals() != m.getAwayGoals()) inET++;
            else inPens++;
        }
        double p90 = 100.0 * in90 / n, pET = 100.0 * inET / n, pPen = 100.0 * inPens / n;
        System.out.printf("৯০ মিনিটে ফয়সালা %.1f%%, এক্সট্রা টাইমে %.1f%%, পেনাল্টিতে %.1f%%%n", p90, pET, pPen);
        check("৯০ মিনিটেই ফয়সালা ৬৮–৮২%", between(p90, 68, 82));
        check("পেনাল্টি পর্যন্ত যাওয়া ম্যাচ ৫–১৮%", between(pPen, 5, 18));
        check("তিনটা শতাংশের যোগফল ১০০", Math.abs(p90 + pET + pPen - 100) < 0.001);
    }

    // ---------- ১০. সাডেন ডেথ ----------
    static void testSuddenDeath(Team home, Team away, int n) {
        title("সাডেন ডেথ (" + n + " বার)");
        Random rng = new Random(11);

        // ক) আলাদা দল
        int homeWins = 0, stranger = 0;
        for (int i = 0; i < n; i++) {
            Team w = quiet(home, away, rng).suddenDeath();
            if (w == home) homeWins++;
            else if (w != away) stranger++;
        }
        double share = 100.0 * homeWins / n;
        System.out.printf("%s জয় %.1f%%%n", home.getName(), share);
        check("বিজয়ী সবসময় দুই দলের একটা", stranger == 0);
        check("কোনো দল একতরফা নয় (৩০–৭০%)", between(share, 30, 70));

        // খ) হুবহু সমান দল: ৫০-৫০ হতে হবে
        Team twin = twin(home, home.getFormation());
        int twinHome = 0;
        for (int i = 0; i < n; i++) {
            if (quiet(home, twin, rng).suddenDeath() == home) twinHome++;
        }
        double twinShare = 100.0 * twinHome / n;
        System.out.printf("সমান দল: হোম জয় %.1f%%%n", twinShare);
        check("সমান দলে হোম ৪৭–৫৩%", between(twinShare, 47, 53));
    }
}