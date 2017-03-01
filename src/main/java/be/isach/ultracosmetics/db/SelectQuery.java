package be.isach.ultracosmetics.db;

import be.isach.ultracosmetics.$;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SelectQuery extends Query {
    private boolean and;

    private final List<Object> values;

    public SelectQuery(Connection connection, String sql) {
        super(connection, sql);

        and = false;

        values = new ArrayList<>();
    }

    public SelectQuery where(String key, Object value) {
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

    public class Binding implements AutoCloseable {

        final Statement statement;
        final ResultSet result;

        public Binding(Statement statement, ResultSet result) {
            this.statement = statement;
            this.result = result;
        }

        public ResultSet getResult() {
            return result;
        }

        @Override
        public void close() throws SQLException {
            try {
                result.close();
            } catch (SQLException e) {
                $.log(e);
            }
            statement.close();
        }

    }

    public Binding execute() {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            int i = 1;
            for (Object object : values) {
                statement.setObject(i++, object);
            }
            return new Binding(statement, statement.executeQuery());
        } catch (SQLException e) {
            $.log(e);
        }
        return null;
    }

}
