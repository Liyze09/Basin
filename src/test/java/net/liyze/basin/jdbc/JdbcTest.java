package net.liyze.basin.jdbc;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class JdbcTest {
    @Disabled
    @Test
    public void test() {
        var jdbcPool = new JdbcPool();
        jdbcPool.connect("jdbc:hsqldb:data/db", "sa", "");
        var result = jdbcPool.execute(connection -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(111);
            return new Object();
        });
        System.out.println(222);
        System.out.println(result.get());
    }
}
