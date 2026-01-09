package com.xk.truck.tom.infra.sequence;

import com.xk.base.infra.sequence.SequenceRepository;
import com.xk.truck.tom.application.port.out.OrderNoGeneratorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ===============================================================
 * Adapter: TomOrderNoGenerator
 * Layer  : Infrastructure
 * Purpose: 產生 TOM 訂單編號（使用 DB Sequence）
 * ===============================================================
 */
@Component
@RequiredArgsConstructor
public class TomOrderNoGenerator implements OrderNoGeneratorPort {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String SEQ_KEY = "TOM_ORDER_NO";

    private final SequenceRepository sequenceRepository;

    @Override
    public String nextTomOrderNo() {

        long seq = sequenceRepository.nextVal(SEQ_KEY);

        String date = LocalDate.now().format(DATE_FMT);

        // 範例格式：TOM-20260106-00000123
        return String.format("TOM-%s-%08d", date, seq);
    }
}
