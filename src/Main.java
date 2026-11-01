import models.*;
import engine.MatchEngine;

public class Main {
    public static void main(String[] args) {
        Team homeTeam = Team.random("FC BARCELONA",5);
        Team awayTeam = Team.random("REAL MADRID FC",5);

        homeTeam.setFormation(Formation.F_4321);
        awayTeam.setFormation(Formation.F_4231);

        MatchEngine theMatch = new MatchEngine(homeTeam,awayTeam);

        int h=0;
        int a=0;
        for (int i=1;i<=10000;i++){
            if(theMatch.suddenDeath()==homeTeam){
                h++;
            }else {
                a++;
            }
        }
        System.out.println(h);
        System.out.println(a);
    }
}