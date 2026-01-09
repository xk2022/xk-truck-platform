package com.xk.base.infra.sequence;

public interface SequenceRepository {
    long nextVal(String key);
}
