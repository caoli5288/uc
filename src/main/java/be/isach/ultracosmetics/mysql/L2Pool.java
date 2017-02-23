package be.isach.ultracosmetics.mysql;

import be.isach.ultracosmetics.$;

import java.util.HashMap;
import java.util.Map;

import static be.isach.ultracosmetics.mysql.L2Pool.Hold.POOL;

/**
 * Created on 17-2-23.
 */
public class L2Pool {

    private final Map<Integer, L2> pool = new HashMap<>();

    static class Hold {
        public static final L2Pool POOL = new L2Pool();

    }

    private L2Pool() {
    }

    public static L2 get(int index) {
        L2 l2 = POOL.pool.get(index);
        if ($.nil(l2)) {
            l2 = new L2(index);
            POOL.pool.put(index, l2);
        }
        return l2;
    }

    public static void del(L2 l2) {
        POOL.pool.remove(l2.getId());
    }

}
