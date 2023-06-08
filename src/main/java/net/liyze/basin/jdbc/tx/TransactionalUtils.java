package net.liyze.basin.jdbc.tx;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;


public class TransactionalUtils {

    @Nullable
    public static Connection getCurrentConnection() {
        TransactionStatus ts = DataSourceTransactionManager.transactionStatus.get();
        return ts == null ? null : ts.connection;
    }
}
