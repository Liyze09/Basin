package net.liyze.basin.jdbc;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;


@FunctionalInterface
public interface ConnectionCallback<T> {

    @Nullable
    T doInConnection(Connection con) throws SQLException;

}
