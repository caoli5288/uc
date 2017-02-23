package be.isach.ultracosmetics;

import java.util.logging.Level;

/**
 * Created on 17-2-22.
 */
public class $ {

    public static boolean nil(Object any) {
        return any == null;
    }

    public static <T> T valid(T i, T def) {
        return nil(i) ? def : i;
    }

    public static void log(Exception exc) {
        Main.getInstance().getLogger().log(Level.SEVERE, exc.getMessage(), exc);
    }

}
