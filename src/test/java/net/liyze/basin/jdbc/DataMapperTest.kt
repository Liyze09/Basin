package net.liyze.basin.jdbc

import org.junit.jupiter.api.Test

class DataMapperTest {
    @Test
    fun test() {
        val jdbcPool = JdbcPool()
        jdbcPool.connect("jdbc:hsqldb:data/db", "sa", "")
        jdbcPool execute {
            it.createStatement().execute("DROP TABLE IF EXISTS classes")
            it.createStatement().execute(
                "CREATE TABLE classes (" +
                        "id BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY," +
                        "name VARCHAR(100) NOT NULL," +
                        "count INT NOT NULL," +
                        "PRIMARY KEY (id)" +
                        ")"
            )
            //it.createStatement().execute("SHUTDOWN")
        }
        val dataMapper = DataMapper("classes", jdbcPool, Class::class.java)
        val rs = dataMapper.put(Class("c77", 44))
        println(dataMapper.get(rs).count)
    }

    data class Class(@JvmField val name: String, @JvmField val count: Int)
}