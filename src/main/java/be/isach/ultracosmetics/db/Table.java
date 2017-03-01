package be.isach.ultracosmetics.db;

import java.sql.Connection;

public class Table {

    private final Connection connection;
    private final String table;

    public Table(Connection connection, String table) {
        this.connection = connection;
        this.table = table;
    }

    public SelectQuery select(String col) {
        return new SelectQuery(connection, "SELECT " + col + " FROM " + table);
    }

    public UpdateQuery update() {
        return new UpdateQuery(connection, "UPDATE " + table + " SET");
    }

}
