package be.isach.ultracosmetics;

import java.sql.SQLException;

/**
 * Created on 17-2-22.
 */
public class $ {

    public static boolean nil(Object any) {
        return any == null;
    }

    public static int valid(Integer i, int def) {
        return nil(i) ? def : i;
    }

    public static void log(Throwable i) {
        i.printStackTrace();
    }

}
