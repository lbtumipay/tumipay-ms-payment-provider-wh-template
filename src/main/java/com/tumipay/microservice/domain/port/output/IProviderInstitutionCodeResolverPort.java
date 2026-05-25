package com.tumipay.microservice.domain.port.output;

import java.util.Optional;

/**
 * ICobreInstitutionCodeResolver
 * <p>
 * ICobreInstitutionCodeResolver interface.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 22/04/2026
 */
public interface IProviderInstitutionCodeResolverPort {

    Optional<String> resolveInstitutionCode(String countryIso2, String standardBankCode);
}
