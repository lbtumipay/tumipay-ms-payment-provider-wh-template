package com.tumipay.microservice.infrastructure.adapter.output.persistence.repository;

import com.tumipay.microservice.infrastructure.adapter.output.persistence.entity.CobreCounterpartyEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * ICobreCounterpartyR2dbcRepository
 * <p>
 * ICobreCounterpartyR2dbcRepository interface.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
public interface ICobreCounterpartyR2dbcRepository extends R2dbcRepository<CobreCounterpartyEntity, Long> {

    Mono<CobreCounterpartyEntity> findFirstByGeoAndTypeAndDocumentNumberAndStatus(
        String geo,
        String type,
        String documentNumber,
        String status
    );

    Mono<Boolean> existsByGeoAndAccountNumberAndTypeNotAndStatus(
        String geo,
        String accountNumber,
        String type,
        String status
    );
}
