package com.xk.base.infra.sequence.impl;

import com.xk.base.infra.sequence.SequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class MysqlSequenceRepository implements SequenceRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public long nextVal(String key) {
        jdbcTemplate.update("""
            INSERT INTO xk_sequence (seq_key, next_val)
            VALUES (?, LAST_INSERT_ID(1))
            ON DUPLICATE KEY UPDATE next_val = LAST_INSERT_ID(next_val + 1)
        """, key);

        Long v = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return v == null ? 0L : v;
    }
}
