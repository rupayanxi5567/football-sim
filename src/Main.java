import models.Player;
import models.Position;

public class Main {
    public static void main(String[] args) {
        Player Mbappe = new Player("Kyllian Mbappe", Position.CF,906,50,95,60,90);
        System.out.println(Mbappe.getRatingsText());

        Player MikheilAkhonozov = Player.randomPlayer("Mikheil Akhonozov", Position.CF,1);
        System.out.println(MikheilAkhonozov.getRatingsText());

        Player SunilChetri = Player.randomPlayer("Sunil Chetri", Position.CF,4);
        System.out.println(SunilChetri.getRatingsText());
    }
}