package com.tumipay.microservice.domain.service.contract;

import com.tumipay.microservice.domain.model.provider.ProviderIntegrationLog;
import com.tumipay.microservice.shared.dto.DomainOperationResult;
import reactor.core.publisher.Mono;

/**
 * IPaymentProviderLogDomainService
 * <p>
 * IPaymentProviderLogDomainService interface.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 7/03/2026
 */
public interface IProviderIntegrationLogDomainService
    extends
    ISaveDomainEntity<ProviderIntegrationLog, Mono<DomainOperationResult<ProviderIntegrationLog>>>,
    IUpdateDomainEntity<ProviderIntegrationLog, Mono<DomainOperationResult<ProviderIntegrationLog>>>,
    IGetDomainEntityByUuId<String, Mono<DomainOperationResult<ProviderIntegrationLog>>> {


}
