package com.xk.truck.tom.infra.persistence.jpa;

import com.xk.truck.tom.application.usecase.qry.FindOrderQry;
import com.xk.truck.tom.infra.persistence.entity.OrderEntity;
import com.xk.truck.tom.infra.persistence.jpa.projection.OrderListItemProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.UUID;

/**
 * ===============================================================
 * Repository: JpaOrderRepository
 * Layer    : Infrastructure / Persistence (Spring Data JPA)
 * Purpose  : OrderEntity CRUD 與常用查詢
 *
 * Notes:
 * - 主鍵型別依 OrderEntity.uuid = UUID
 * - orderNo 為唯一識別（對外查詢常用）
 * ===============================================================
 */
public interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {

    boolean existsByOrderNo(String orderNo);

    // 常用：依 orderNo 查詢
    Optional<OrderEntity> findByOrderNo(String orderNo);

    @Query(
            value = """
            select
                o.uuid as uuid,
                o.orderNo as orderNo,
                o.orderType as orderType,
                o.orderStatus as orderStatus,
                o.customerUuid as customerUuid,
                o.pickupAddress as pickupAddress,
                o.deliveryAddress as deliveryAddress,
                o.shippingCompany as shippingCompany,
                o.vesselVoyage as vesselVoyage,
                o.containerNo as containerNo,
                o.containerType as containerType,
                o.createdTime as createdTime,

                i.importDeclNo as importDeclNo,
                i.deliveryOrderLocation as deliveryOrderLocation,
                i.blNo as blNo,

                e.exportDeclNo as exportDeclNo,
                e.bookingNo as bookingNo,
                e.soNo as soNo
            from OrderEntity o
            left join o.importDetail i
            left join o.exportDetail e
            where 1=1
              and (:#{#q.orderType} is null or o.orderType = :#{#q.orderType})
              and (:#{#q.orderStatus} is null or o.orderStatus = :#{#q.orderStatus})
              and (:#{#q.customerUuid} is null or o.customerUuid = :#{#q.customerUuid})

              and (:#{#q.orderNoLike} is null or o.orderNo like concat('%', :#{#q.orderNoLike}, '%'))
              and (:#{#q.containerNoLike} is null or o.containerNo like concat('%', :#{#q.containerNoLike}, '%'))

              and (:#{#q.createdFrom} is null or o.createdTime >= :#{#q.createdFrom})
              and (:#{#q.createdTo} is null or o.createdTime <= :#{#q.createdTo})
            """,
            countQuery = """
            select count(o.uuid)
            from OrderEntity o
            where 1=1
              and (:#{#q.orderType} is null or o.orderType = :#{#q.orderType})
              and (:#{#q.orderStatus} is null or o.orderStatus = :#{#q.orderStatus})
              and (:#{#q.customerUuid} is null or o.customerUuid = :#{#q.customerUuid})

              and (:#{#q.orderNoLike} is null or o.orderNo like concat('%', :#{#q.orderNoLike}, '%'))
              and (:#{#q.containerNoLike} is null or o.containerNo like concat('%', :#{#q.containerNoLike}, '%'))

              and (:#{#q.createdFrom} is null or o.createdTime >= :#{#q.createdFrom})
              and (:#{#q.createdTo} is null or o.createdTime <= :#{#q.createdTo})
            """
    )
    Page<OrderListItemProjection> pageForListProjection(FindOrderQry q, Pageable pageable);

    @EntityGraph(attributePaths = {
            "importDetail",
            "exportDetail"
            // 需要再加： "statusLogs", "assignments" 也可以，但注意資料量
    })
    Optional<OrderEntity> findDetailByUuid(UUID uuid);
}
