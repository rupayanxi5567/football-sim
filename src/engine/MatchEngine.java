package engine;

import models.Player;
import models.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MatchEngine {
    private static final double POSSESSION_POWER = 2.0;
    private static final double SHOT_STRENGTH_POWER = 1.0;
    private static final double MAX_SHOT_RATE = 0.50;
    private static final double RATING_DIVISOR = 400.0;
    private static final double GK_DIVISOR = 400.0;
    private static final double MIN_ON_TARGET = 0.10;
    private static final double MAX_ON_TARGET = 0.65;

    private static final double SHOT_RATE = 0.30;
    private static final double EXTRA_TIME_FACTOR = 0.92;
    private static final double POSSESSION_RESET_CHANCE = 0.30;
    private static final double BASE_ON_TARGET = 0.35;
    private static final double BASE_SAVE = 0.73;
    private static final double MIN_SAVE = 0.40;
    private static final double MAX_SAVE = 0.85;
    private static final double FATIGUE_STRENGTH = 0.75;
    private static final double PENALTY_BASE = 0.75;
    private static final double PENALTY_DIVISOR = 400.0;
    private static final double HOME_ADVANTAGE = 0.07;
    private static final double SHOOTER_POWER = 3.0;
    private static final double SHOOTER_DIVISOR = 150.0;
    private static final double ASSIST_CHANCE = 0.75;

    private final List<Player> homeXI, awayXI;
    private final double homeShootRef, awayShootRef;

    private final int homeAttack, homeMid, homeDef;
    private final int awayAttack, awayMid, awayDef;

    private final int homeStamina;
    private final int awayStamina;

    private final int homeGk;
    private final int awayGk;

    private final List<Player> homePenaltyTakers;
    private final List<Player> awayPenaltyTakers;

    private boolean homeHasBall;
    private int homeShots, awayShots;
    private int homeOnTarget, awayOnTarget;
    private int homePossessionMinutes, awayPossessionMinutes;

    private final Team homeTeam;
    private final Team awayTeam;
    private int homeGoals = 0;
    private int awayGoals = 0;
    private int homePenaltyScore = 0;
    private int awayPenaltyScore = 0;
    private Team winner;
    private final Random rand;
    private boolean verbose = true;
    private final List<MatchEvent> matchEvents = new ArrayList<>();

    private void record(int minute, String label, EventType type, Team team, Player player, Player second, int hs, int as) {
        MatchEvent e = new MatchEvent(minute, type, team, player, label, hs, as, second);
        matchEvents.add(e);
        if (verbose) {
            System.out.println(Commentary.describe(e));
        }
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public MatchEngine(Team homeTeam, Team awayTeam) {
        this(homeTeam, awayTeam, new Random());
    }

    public MatchEngine(Team homeTeam, Team awayTeam, Random rand) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.rand = rand;

        this.homeAttack = homeTeam.getAttack();
        this.homeMid = homeTeam.getMidField();
        this.homeDef = homeTeam.getDefense();
        this.awayAttack = awayTeam.getAttack();
        this.awayMid = awayTeam.getMidField();
        this.awayDef = awayTeam.getDefense();

        this.homeHasBall = rand.nextBoolean();

        this.homeStamina = homeTeam.getAverageStamina();
        this.awayStamina = awayTeam.getAverageStamina();
        this.homeGk = homeTeam.getGoalKeeperRating();
        this.awayGk = awayTeam.getGoalKeeperRating();
        this.homePenaltyTakers = homeTeam.getPenaltyTakers();
        this.awayPenaltyTakers = awayTeam.getPenaltyTakers();

        this.homeXI = homeTeam.getStartingEleven();
        this.awayXI = awayTeam.getStartingEleven();
        this.homeShootRef = expectedShooting(homeXI);
        this.awayShootRef = expectedShooting(awayXI);
    }

    private double fatigue(int avgStamina, int minutes){
        double tiredness = 1.0 - avgStamina/100.0;
        return 1.0 - FATIGUE_STRENGTH * (minutes / 90.0) * tiredness;
    }

    private void playMinute(int minute, String label) {
        double homeMidNow = homeMid * fatigue(homeStamina, minute);
        double awayMidNow = awayMid * fatigue(awayStamina, minute);

        if (POSSESSION_RESET_CHANCE > rand.nextDouble()) {
            double h = Math.pow(homeMidNow, POSSESSION_POWER);
            double a = Math.pow(awayMidNow, POSSESSION_POWER);
            homeHasBall = rand.nextDouble() < h / (h + a);
        }

        if (homeHasBall) {
            homePossessionMinutes++;
        } else {
            awayPossessionMinutes++;
        }

        Team attacker = homeHasBall ? homeTeam : awayTeam;
        double attackFatigue = fatigue(homeHasBall ? homeStamina : awayStamina, minute);
        double defendFatigue = fatigue(homeHasBall ? awayStamina : homeStamina, minute);

        double attackerPower = (homeHasBall ? homeAttack + homeMid : awayAttack + awayMid) * attackFatigue;
        double defenderPower = (homeHasBall ? awayMid + awayDef : homeMid + homeDef) * defendFatigue;

        double shotRate = (minute > 90) ? SHOT_RATE * EXTRA_TIME_FACTOR : SHOT_RATE;
        shotRate = shotRate * attackFatigue;
        shotRate = shotRate * Math.pow(attackerPower / defenderPower, SHOT_STRENGTH_POWER);
        shotRate = Math.min(shotRate, MAX_SHOT_RATE);
        if (rand.nextDouble() > shotRate) {
            return;
        }

        int keeper = homeHasBall ? awayGk : homeGk;

        if (homeHasBall) {
            homeShots++;
        } else {
            awayShots++;
        }

        List<Player> xi = homeHasBall ? homeXI : awayXI;
        Player shooter = pickPlayer(xi, null, false);
        double ref = homeHasBall ? homeShootRef : awayShootRef;

        double onTarget = BASE_ON_TARGET
                + (attackerPower - defenderPower) / RATING_DIVISOR
                + (shooter.getShooting() - ref) / SHOOTER_DIVISOR;
        if (homeHasBall) {
            onTarget = onTarget + HOME_ADVANTAGE;
        }
        onTarget = Math.clamp(onTarget, MIN_ON_TARGET, MAX_ON_TARGET);
        if (rand.nextDouble() > onTarget) {
            record(minute, label, EventType.SHOT_MISSED, attacker, shooter, null, homeGoals, awayGoals);
            return;
        }

        if (homeHasBall) {
            homeOnTarget++;
        } else {
            awayOnTarget++;
        }

        double save = BASE_SAVE + (keeper - shooter.getShooting()) / GK_DIVISOR;
        save = Math.clamp(save, MIN_SAVE, MAX_SAVE);
        if (save > rand.nextDouble()) {
            record(minute, label, EventType.SHOT_SAVED, attacker, shooter, null, homeGoals, awayGoals);
            return;
        }

        if (homeHasBall) {
            homeGoals++;
        } else {
            awayGoals++;
        }
        Player assister = (rand.nextDouble() < ASSIST_CHANCE) ? pickPlayer(xi, shooter, true) : null;
        record(minute, label, EventType.GOAL, attacker, shooter, assister, homeGoals, awayGoals);
    }

    private double shooterWeight(Player p) {
        return p.getPosition().attackWeight() * Math.pow(p.getShooting() / 100.0, SHOOTER_POWER);
    }

    private double assistWeight(Player p) {
        return (0.2 + p.getPosition().attackWeight()) * Math.pow(p.getPassing() / 100.0, 2);
    }

    private Player pickPlayer(List<Player> xi, Player exclude, boolean forAssist) {
        double[] w = new double[xi.size()];
        double total = 0;
        for (int i = 0; i < xi.size(); i++) {
            Player p = xi.get(i);
            if (p == exclude || p.getPosition().isGoalkeeper()) continue;
            w[i] = forAssist ? assistWeight(p) : shooterWeight(p);
            total += w[i];
        }
        double r = rand.nextDouble() * total;
        int last = -1;
        for (int i = 0; i < xi.size(); i++) {
            if (w[i] > 0) {
                last = i;
                r -= w[i];
                if (r <= 0) return xi.get(i);
            }
        }
        return xi.get(last);
    }

    private double expectedShooting(List<Player> xi) {
        double total = 0, weighted = 0;
        for (Player p : xi) {
            double w = shooterWeight(p);
            total += w;
            weighted += w * p.getShooting();
        }
        return total == 0 ? 0 : weighted / total;
    }

    public void play() {
        boolean firstKickOffHome = homeHasBall;
        record(0, "0", EventType.KICKOFF, null, null, null, 0, 0);
        playPeriod(1, 45, 5);
        record(45, "45", EventType.HALF_TIME, null, null, null, homeGoals, awayGoals);
        homeHasBall = !firstKickOffHome;
        playPeriod(46, 90, 5);
        record(90, "90", EventType.FULL_TIME, null, null, null, homeGoals, awayGoals);
    }

    private void playPeriod(int start, int end, int maxAdded) {
        for(int minute=start;minute<=end;minute++){
            playMinute(minute,String.valueOf(minute));
        }
        int added = 1 + rand.nextInt(maxAdded);
        for(int extraMinute=1;extraMinute<=added;extraMinute++){
            playMinute(end,end + "+" + extraMinute);
        }
    }

    public void result() {
        String how = "";
        if (homeGoals > awayGoals) {
            winner = homeTeam;
        } else if (homeGoals < awayGoals) {
            winner = awayTeam;
        } else {
            extraTime();
            if (homeGoals != awayGoals) {
                winner = (homeGoals > awayGoals) ? homeTeam : awayTeam;
                how = "after extra time";
            } else {
                penalty();
                if (homePenaltyScore != awayPenaltyScore) {
                    winner = (homePenaltyScore > awayPenaltyScore) ? homeTeam : awayTeam;
                    how = "on penalties";
                } else {
                    winner = suddenDeath();
                    how = "in sudden death";
                }
            }
        }
        record(how.isEmpty() ? 90 : 120, how, EventType.WINNER, winner, null, null,
                homeGoals, awayGoals);
    }

    private void extraTime() {
        record(90, "90", EventType.EXTRA_TIME_START, null, null, null, homeGoals, awayGoals);
        playPeriod(91, 105, 2);
        record(105, "105", EventType.EXTRA_TIME_HALF, null, null, null, homeGoals, awayGoals);
        playPeriod(106, 120, 2);
        record(120, "120", EventType.EXTRA_TIME_END, null, null, null, homeGoals, awayGoals);
    }

    private boolean takeKick(boolean isHome, int teamKickIndex) {
        Team team = isHome ? homeTeam : awayTeam;
        List<Player> takers = isHome ? homePenaltyTakers : awayPenaltyTakers;
        Player kicker = takers.get(teamKickIndex % takers.size());
        int keeper = isHome ? awayGk : homeGk;

        double chance = PENALTY_BASE + (kicker.getShooting() - keeper) / PENALTY_DIVISOR;
        chance = Math.clamp(chance, 0.50, 0.92);
        boolean scored = chance > rand.nextDouble();

        if (scored) {
            if (isHome) homePenaltyScore++; else awayPenaltyScore++;
        }
        record(120, "120", scored ? EventType.PENALTY_SCORED : EventType.PENALTY_MISSED,
                team, kicker, null, homePenaltyScore, awayPenaltyScore);
        return scored;
    }

    private void penalty() {
        record(120, "120", EventType.PENALTY_SHOOTOUT_START, null, null, null, 0, 0);
        for (int kick = 1; kick <= 10; kick++) {
            boolean homeTurn = (kick % 2 == 1);
            takeKick(homeTurn, (kick - 1) / 2);
            if (shootoutDecided(kick)) {
                break;
            }
        }
    }

    public Team suddenDeath() {
        int round = 5;
        while (true) {
            boolean h = takeKick(true, round);
            boolean a = takeKick(false, round);
            if (h != a) {
                return h ? homeTeam : awayTeam;
            }
            round++;
        }
    }

    private boolean shootoutDecided(int kick) {
        int homeLeft = 5 - (kick + 1) / 2;
        int awayLeft = 5 - kick / 2;
        return homePenaltyScore > awayPenaltyScore + awayLeft
                || awayPenaltyScore > homePenaltyScore + homeLeft;
    }

    public void playKnockOut(){
        play();
        result();
    }

    public double getHomePossessionPercent(){
        int total = homePossessionMinutes + awayPossessionMinutes;
        return total == 0 ? 0 : 100.0 * homePossessionMinutes / total;
    }
    public double getAwayPossessionPercent(){
        int total = homePossessionMinutes + awayPossessionMinutes;
        return total == 0 ? 0 : 100.0 * awayPossessionMinutes / total;
    }
    public int getHomeGoals() {
        return homeGoals;
    }
    public int getAwayGoals() {
        return awayGoals;
    }
    public Team getWinner() {
        return winner;
    }
    public int getHomeShots(){
        return homeShots;
    }
    public int getAwayShots() {
        return awayShots;
    }
    public int getHomeOnTarget() {
        return homeOnTarget;
    }
    public int getAwayOnTarget() {
        return awayOnTarget;
    }
    public List<MatchEvent> getMatchEvents() {
        return matchEvents;
    }
}