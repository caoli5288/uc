package be.isach.ultracosmetics.mysql;

import be.isach.ultracosmetics.Main;
import lombok.val;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UpdateQuery extends Query {

    private boolean comma;
    private boolean and;

    private final List<Object> values;

    public UpdateQuery(Connection connection, String sql) {
        super(connection, sql);

        comma = false;

        and = false;

        values = new ArrayList<>();
    }

    public UpdateQuery set(String field, Object value) {
        if (comma) {
            sql += ",";
        }

        values.add(value);

        sql += " " + field + "=?";

        comma = true;

        return this;
    }

    public UpdateQuery where(String key, Object value) {
        if (and) {
            sql += " AND";
        } else {
            sql += " WHERE";
        }

        sql += " " + key + "=";

        values.add(value);

        sql += "?";

        and = true;

        return this;
    }

    public void execute() throws SQLException {
        if (Bukkit.isPrimaryThread()) {// Not block main thread
            Main.exec(this::exec);
            Main.debug("DEBUG #1 ASYNC " + sql.toUpperCase());
        } else {
            try {
                exec();
            } catch (RuntimeException e) {
                throw new SQLException(e);
            }
        }
    }

    public void exec() {
        try (val statement = connection.prepareStatement(sql)) {
            int i = 1;
            for (Object object : values) {
                statement.setObject(i++, object);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
