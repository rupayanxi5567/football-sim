package engine;

import models.Team;

import java.io.StreamTokenizer;
import java.util.Random;

public class MatchEngine {
    private Team homeTeam;
    private Team awayTeam;
    private int homeGoals = 0;
    private int awayGoals = 0;
    private int homePenaltyScore = 0;
    private int awayPenaltyScore = 0;
    private Team winner;
    private final double PENALTY_POSSIBILITY = 0.75;
    private final Random rand;

    public MatchEngine(Team homeTeam, Team awayTeam) {
        this(homeTeam, awayTeam, new Random());
    }

    public MatchEngine(Team homeTeam, Team awayTeam, Random rand) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.rand = rand;
    }

    public void play(){
        for (int minute = 1; minute <=90 ; minute++) {
            playMinute(minute);
        }
    }

    public void playMinute(int minute){
        double homeShare = (double) homeTeam.getMidField() / (homeTeam.getMidField()+awayTeam.getMidField());
        boolean homeHasBall = homeShare > rand.nextDouble();
        if(rand.nextDouble() > 0.25){
            return;
        }

        Team attacker = homeHasBall ? homeTeam : awayTeam;
        Team defender = homeHasBall ? awayTeam : homeTeam;

        double goalChance = 0.12 + (attacker.getAttack() + attacker.getMidField() - defender.getDefense()
                - defender.getMidField()) / 200.0 ;

        if(attacker == homeTeam){
            goalChance += 0.02;
        }

        goalChance = Math.clamp(goalChance, 0.03, 0.40);

        if(goalChance > rand.nextDouble()){
            if(attacker==homeTeam){
                homeGoals++;
            }else {
                awayGoals++;
            }
            System.out.println(minute + "' GOAL! " + attacker.getName() + " (" + homeGoals + "-" + awayGoals + ")");
        }
    }

    public void result(){
        if(homeGoals>awayGoals) {
            System.out.println("Home won");
        }else if(homeGoals<awayGoals){
            System.out.println("Away won");
        }else{

            extraTime();

            if(homeGoals==awayGoals){
                penalty();
                if(homePenaltyScore > awayPenaltyScore){
                    System.out.println("Home won in Penalty");
                }else if(homePenaltyScore < awayPenaltyScore){
                    System.out.println("Away won in Penalty");
                }else{
                    if(suddenDeath()==homeTeam){
                        System.out.println("Home won in Sudden Death");
                    }else{
                        System.out.println("Away won in Sudden Death");
                    }
                }
            }else {
                if(homeGoals > awayGoals){
                    System.out.println("Home won in Extra Time");
                }else if(homeGoals < awayGoals){
                    System.out.println("Away won in Extra Time");
                }
            }
        }
    }

    public Team suddenDeath() {

        while (true){

            int homeScore=0;
            int awayScore=0;

            System.out.println("Home Player Shoots");
            if(isPenaltyGoal()){
                System.out.println("Home Player Scores a Goal in Sudden Death Penalty");
                homeScore++;
            }else{
                System.out.println("Home Player Misses a Goal in Sudden Death Penalty");
            }

            System.out.println("Away Player Shoots");
            if(isPenaltyGoal()){
                System.out.println("Away Player Scores a Goal in Sudden Death Penalty");
                awayScore++;
            }else{
                System.out.println("Away Player Misses a Goal in Sudden Death Penalty");
            }

            if(homeScore!=awayScore){
                if(homeScore > awayScore){
                    return homeTeam;
                }else if(homeScore < awayScore) {
                    return awayTeam;
                }
            }
        }
    }

    private void extraTime() {
        for (int minutes=91;minutes<=120;minutes++){
            playMinute(minutes);
        }
    }

    private void penalty() {
        for(int i=1;i<=10;i++){
            if(i%2==1){
                System.out.println("Home Team Shoots");
                if(isPenaltyGoal()){
                    System.out.println("Home Team Player Scores a Goal !!!");
                    homePenaltyScore++;
                }else{
                    System.out.println("Home Team Player Misses a Goal !!!");
                }
            }else{
                System.out.println("Away Team Shoots");
                if(isPenaltyGoal()){
                    System.out.println("Away Team Player Scores a Goal !!!");
                    awayPenaltyScore++;
                }else{
                    System.out.println("Away Team Player Misses a Goal !!!");
                }
            }
        }


    }

    public boolean isPenaltyGoal(){
        return PENALTY_POSSIBILITY > rand.nextDouble();
    }

    public int getHomeGoals() {
        return homeGoals;
    }
    public int getAwayGoals() {
        return awayGoals;
    }
}