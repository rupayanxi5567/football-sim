import models.*;

public class Main {
    public static void main(String[] args) {
        Team a = Team.random("FC BARCELONA", 3);

        System.out.println(a.getLineupText());
        System.out.println();

        System.out.println("****************************************");

        a.setFormation(Formation.F_3412);
        System.out.println(a.getLineupText());

        System.out.println("****************************************");

        a.setFormation(Formation.F_4321);
        System.out.println(a.getLineupText());
    }
}