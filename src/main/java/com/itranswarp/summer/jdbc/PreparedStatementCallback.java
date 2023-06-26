package com.itranswarp.summer.jdbc;

import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;


@FunctionalInterface
public interface PreparedStatementCallback<T> {

    @Nullable
    T doInPreparedStatement(PreparedStatement ps) throws SQLException;

}
