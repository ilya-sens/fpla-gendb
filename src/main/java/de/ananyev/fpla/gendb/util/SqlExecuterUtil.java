package de.ananyev.fpla.gendb.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;

/**
 * Created by Ilya Ananyev on 16.03.17.
 */
public class SqlExecuterUtil {
    public static Number execute(JdbcTemplate jdbcTemplate, String statement, String idColumnName) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(statement, new String[]{idColumnName});
                    return ps;
                },
                keyHolder);
        return keyHolder.getKey();
    }

    public static Number execute(JdbcTemplate jdbcTemplate, String statement) {
        return execute(jdbcTemplate, statement, "ID");
    }

}
