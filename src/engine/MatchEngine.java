package engine;

import models.Player;
import models.Team;

import java.util.List;
import java.util.Random;

public class MatchEngine {
    private static final double SHOT_RATE = 0.27;
    private static final double EXTRA_TIME_FACTOR = 0.92;
    private static final double POSSESSION_RESET_CHANCE = 0.30;
    private static final double BASE_ON_TARGET = 0.35;
    private static final double MIN_ON_TARGET = 0.15;
    private static final double MAX_ON_TARGET = 0.60;
    private static final double BASE_SAVE = 0.73;
    private static final double GK_DIVISOR = 300.0;
    private static final double MIN_SAVE = 0.40;
    private static final double MAX_SAVE = 0.85;
    private static final double FATIGUE_STRENGTH = 0.75;
    private static final double PENALTY_BASE = 0.75;
    private static final double PENALTY_DIVISOR = 400.0;
    private static final double HOME_ADVANTAGE = 0.04;
    private static final double RATING_DIVISOR = 400.0;

//    private static final double ATTACK_RATE = 0.22;
//    private static final double EXTRA_TIME_ATTACK_RATE = 0.15;
//    private static final double BASE_GOAL_CHANCE = 0.12;
//    private static final double MIN_GOAL_CHANCE = 0.03;
//    private static final double MAX_GOAL_CHANCE = 0.40;

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

    private Team homeTeam;
    private Team awayTeam;
    private int homeGoals = 0;
    private int awayGoals = 0;
    private int homePenaltyScore = 0;
    private int awayPenaltyScore = 0;
    private Team winner;
    private static final double PENALTY_POSSIBILITY = 0.75;
    private final Random rand;
    private boolean verbose = true;

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private void log(String text) {
        if (verbose) {
            System.out.println(text);
        }
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
    }

    private double fatigue(int avgStamina, int minutes){
        double tiredness = 1.0 - avgStamina/100.0;
        return 1.0 - FATIGUE_STRENGTH * (minutes / 90.0) * tiredness;
    }

    public void playMinute(int minute, String label){
        double homeMidNow = homeMid * fatigue(homeStamina,minute);
        double awayMidNow = awayMid * fatigue(awayStamina,minute);

        if(POSSESSION_RESET_CHANCE > rand.nextDouble()){
            homeHasBall = rand.nextDouble() < (homeMidNow / (homeMidNow+awayMidNow));
        }

        if(homeHasBall){
            homePossessionMinutes++;
        }else{
            awayPossessionMinutes++;
        }

        Team attacker = homeHasBall ? homeTeam : awayTeam;
        double attackFatigue = fatigue(homeHasBall ? homeStamina : awayStamina,minute);
        double defendFatigue = fatigue(homeHasBall ? awayStamina : homeStamina,minute);

        double shotRate = ((minute>90) ? SHOT_RATE * EXTRA_TIME_FACTOR : SHOT_RATE);
        shotRate = shotRate * attackFatigue;
        if(rand.nextDouble() > shotRate){
            return;
        }

        double attackerPower = (homeHasBall ? homeAttack + homeMid : awayAttack + awayMid) * attackFatigue;
        double defenderPower = (homeHasBall ? awayMid + awayDef : homeMid + homeDef) * defendFatigue;
        int keeper = homeHasBall ? awayGk : homeGk;
        int attackerAttack = homeHasBall ? homeAttack : awayAttack;

        if(homeHasBall){
            homeShots++;
        }else{
            awayShots++;
        }

        double onTarget = BASE_ON_TARGET + (attackerPower - defenderPower) / RATING_DIVISOR;
        if(homeHasBall){
            onTarget = onTarget + HOME_ADVANTAGE;
        }
        onTarget = Math.clamp(onTarget,MIN_ON_TARGET,MAX_ON_TARGET);
        if(rand.nextDouble() > onTarget){
            log(label + "' shot off target (" + attacker.getName() + ")");
            return;
        }

        if(homeHasBall){
            homeOnTarget++;
        }else{
            awayOnTarget++;
        }

        double save = BASE_SAVE + (keeper - attackerAttack) / GK_DIVISOR;
        save = Math.clamp(save,MIN_SAVE,MAX_SAVE);
        if(save > rand.nextDouble()){
            log(label + "' saved by the keeper");
            return;
        }

        if(homeHasBall){
            homeGoals++;
        }else{
            awayGoals++;
        }
        log(label + "' GOAL! " + attacker.getName() + " (" + homeGoals + "-" + awayGoals + ")");
    }

    public void play() {
        boolean firstKickOffHome = homeHasBall;
        playPeriod(1,45,5);
        log("HALF-TIME");
        homeHasBall = !firstKickOffHome;
        playPeriod(46,90,5);
        log("FULL-TIME");
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
        if (homeGoals > awayGoals) {
            log(homeTeam.getName() + " won");
            winner = homeTeam;
        } else if (homeGoals < awayGoals) {
            log(awayTeam.getName() + " won");
            winner = awayTeam;
        } else {
            extraTime();
            if (homeGoals == awayGoals) {
                penalty();
                if (homePenaltyScore > awayPenaltyScore) {
                    log(homeTeam.getName() + " won in Penalty");
                    winner = homeTeam;
                } else if (homePenaltyScore < awayPenaltyScore) {
                    log(awayTeam.getName() + " won in Penalty");
                    winner = awayTeam;
                } else {
                    if (suddenDeath() == homeTeam) {
                        log(homeTeam.getName() + " won in Sudden Death");
                        winner = homeTeam;
                    } else {
                        log(awayTeam.getName() + " won in Sudden Death");
                        winner = awayTeam;
                    }
                }
            } else {
                if (homeGoals > awayGoals) {
                    log(homeTeam.getName() + " won in Extra Time");
                    winner = homeTeam;
                } else if (homeGoals < awayGoals) {
                    log(awayTeam.getName() + " won in Extra Time");
                    winner = awayTeam;
                }
            }
        }
    }

    private void extraTime() {
        playPeriod(91,105,2);
        log("HALF-TIME OF EXTRA TIME");
        playPeriod(106,120,2);
        log("FULL-TIME OF EXTRA TIME");
    }

    private boolean takeKick(Team team,int teamKickIndex) {
        boolean isHome = (team == homeTeam);
        List<Player> takers = isHome ? homePenaltyTakers : awayPenaltyTakers;
        Player kicker = takers.get(teamKickIndex % takers.size());
        int keeper = isHome ? awayGk : homeGk;

        double chance = PENALTY_BASE + (kicker.getShooting() - keeper)/PENALTY_DIVISOR;
        chance = Math.clamp(chance,0.50,0.92);

        boolean scored = chance > rand.nextDouble();
        log(kicker.getName() + (scored ? " Scores a Goal !!!" : "Misses the Goal !!!"));
        return scored;
    }

    private void penalty() {
        for(int kick=1;kick<=10;kick++){
            boolean homeTurn = (kick%2==1);
            int teamKickIndex = (kick - 1) / 2;
            if(takeKick(homeTurn ? homeTeam : awayTeam, teamKickIndex)){
                if(homeTurn){
                    homePenaltyScore++;
                }else{
                    awayPenaltyScore++;
                }
            }
            if(shootoutDecided(kick)){
                break;
            }
        }


    }

    private boolean shootoutDecided(int kick) {
        int homeLeft = 5 - (kick + 1) / 2;
        int awayLeft = 5 - kick / 2;
        return homePenaltyScore > awayPenaltyScore + awayLeft
                || awayPenaltyScore > homePenaltyScore + homeLeft;
    }

    public Team suddenDeath() {
        int round = 5;
        while (true) {
            boolean homeScore = takeKick(homeTeam, round);
            boolean awayScore = takeKick(awayTeam, round);
            if(homeScore != awayScore){
                return homeScore ? homeTeam : awayTeam;
            }
            round++;
        }
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
}