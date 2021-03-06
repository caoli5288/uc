package be.isach.ultracosmetics.db;

import java.sql.Connection;

public abstract class Query {

    protected final Connection connection;
    protected String sql;

    public Query(Connection connection, String sql) {
        this.connection = connection;
        this.sql = sql;
    }

    public String toString() {
        return sql;
    }

}
