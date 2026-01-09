package com.xk.truck.tom.domain.model.vo;

import org.hibernate.sql.results.DomainResultCreationException;

public record ContainerInfo(String containerNo, String size) {

    public ContainerInfo {
        if (containerNo.length() != 11)
            throw new DomainResultCreationException("櫃號格式錯誤");
    }
}
