package com.xk.base.infra.sequence.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xk_sequence")
public class SequenceEntity {

    @Id
    @Column(name = "seq_key", length = 64, nullable = false)
    private String seqKey;

    @Column(name = "next_val", nullable = false)
    private Long nextVal;
}
