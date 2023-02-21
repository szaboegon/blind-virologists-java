package graphic;

import koporscho.GameController;

import javax.swing.*;

/** A fő szál megvalósítására szolgáló osztály.*/
public class Main {
    /** A main függvény megvalósítása*/
    public static void main(String[] args) {
        try {
            GameController gc = GameController.getInstance();
            Object[] possibilities = {"map1", "map2-testing"};
            String fname = (String)JOptionPane.showInputDialog(
                    null,
                    "Please choose a map provided below: ",
                    "Map Selection",
                    JOptionPane.PLAIN_MESSAGE, null,
                    possibilities,
                    "map1");
            if ((fname != null) && (fname.length() > 0)) {
                gc.StartGame(fname);
                GUI g = GUI.getInstance();
                g.setVisible(true);
                gc.NotifyViews();
                gc.GetCurrentVirologist().NotifyViews();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
