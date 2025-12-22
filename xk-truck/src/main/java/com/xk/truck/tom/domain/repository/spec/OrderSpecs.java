package com.xk.truck.tom.domain.repository.spec;

import com.xk.truck.tom.controller.api.dto.OrderQuery;
import com.xk.truck.tom.domain.model.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

public class OrderSpecs {

    public static Specification<Order> byQuery(OrderQuery q) {
        return (root, query, cb) -> {
            var ps = new ArrayList<Predicate>();

            if (q.getOrderType() != null) ps.add(cb.equal(root.get("orderType"), q.getOrderType()));
            if (q.getOrderStatus() != null) ps.add(cb.equal(root.get("status"), q.getOrderStatus()));

            if (hasText(q.getCustomerUuid())) {
                ps.add(cb.like(cb.lower(root.get("customerName")), like(q.getCustomerUuid())));
            }
            if (hasText(q.getContainerNoLike())) {
                ps.add(cb.like(cb.lower(root.get("containerNo")), like(q.getContainerNoLike())));
            }

            if (q.getCreatedFrom() != null) ps.add(cb.greaterThanOrEqualTo(root.get("createdTime"), q.getCreatedFrom()));
            if (q.getCreatedTo() != null) ps.add(cb.lessThanOrEqualTo(root.get("createdTime"), q.getCreatedTo()));

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String like(String s) {
        return "%" + s.trim().toLowerCase() + "%";
    }
}
