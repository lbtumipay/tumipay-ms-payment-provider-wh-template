package com.tumipay.microservice.infrastructure.component.enums;

import lombok.Getter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * BanksCoCodeEnum
 * <p>
 * BanksCoCodeEnum enum.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 13/04/2026
 */
@Getter
public enum BanksCoCodeEnum {

    /**
     * Enum representing Bancolombia bank.
     */
    BANCOLOMBIA("BANCOLOMBIA"),

    /**
     * Enum representing Nequi digital wallet.
     */
    NEQUI("NEQUI"),

    /**
     * Enum representing Davivienda bank.
     */
    DAVIVIENDA("DAVIVIENDA"),

    /**
     * Enum representing Daviplata digital wallet.
     */
    DAVIPLATA("DAVIPLATA"),

    /**
     * Enum representing Banco de Bogotá.
     */
    BANCO_BOGOTA("BANCO_BOGOTA"),

    /**
     * Enum representing Banco Popular.
     */
    BANCO_POPULAR("BANCO_POPULAR"),

    /**
     * Enum representing Itaú bank.
     */
    ITAU("ITAU"),

    /**
     * Enum representing Citibank Colombia.
     */
    CITIBANK("CITIBANK"),

    /**
     * Enum representing Banco GNB Sudameris.
     */
    BANCO_GNB_SUDAMERIS("BANCO_GNB_SUDAMERIS"),

    /**
     * Enum representing BBVA Colombia.
     */
    BBVA_COLOMBIA("BBVA_COLOMBIA"),

    /**
     * Enum representing Scotiabank Colpatria.
     */
    SCOTIABANK_COLPATRIA("SCOTIABANK_COLPATRIA"),

    /**
     * Enum representing Banco de Occidente.
     */
    BANCO_DE_OCCIDENTE("BANCO_DE_OCCIDENTE"),

    /**
     * Enum representing Bancoldex.
     */
    BANCOLDEX("BANCOLDEX"),

    /**
     * Enum representing Banco Caja Social.
     */
    BANCO_CAJA_SOCIAL("BANCO_CAJA_SOCIAL"),

    /**
     * Enum representing Banco Agrario de Colombia.
     */
    BANCO_AGRARIO("BANCO_AGRARIO"),

    /**
     * Enum representing Banco Mundo Mujer.
     */
    BANCO_MUNDO_MUJER("BANCO_MUNDO_MUJER"),

    /**
     * Enum representing Banco AV Villas.
     */
    BANCO_AV_VILLAS("BANCO_AV_VILLAS"),

    /**
     * Enum representing Banco W.
     */
    BANCO_W("BANCO_W"),

    /**
     * Enum representing Banco ProCredit Colombia.
     */
    BANCO_PROCREDIT("BANCO_PROCREDIT"),

    /**
     * Enum representing Bancamía.
     */
    BANCAMIA("BANCAMIA"),

    /**
     * Enum representing Banco Pichincha Colombia.
     */
    BANCO_PICHINCHA("BANCO_PICHINCHA"),

    /**
     * Enum representing Bancoomeva.
     */
    BANCOOMEVA("BANCOOMEVA"),

    /**
     * Enum representing Banco Falabella Colombia.
     */
    BANCO_FALABELLA("BANCO_FALABELLA"),

    /**
     * Enum representing Banco Finandina.
     */
    BANCO_FINANDINA("BANCO_FINANDINA"),

    /**
     * Enum representing Banco Multibank Colombia.
     */
    BANCO_MULTIBANK("BANCO_MULTIBANK"),

    /**
     * Enum representing Banco Santander de Negocios Colombia.
     */
    BANCO_SANTANDER_DE_NEGOCIOS("BANCO_SANTANDER_DE_NEGOCIOS"),

    /**
     * Enum representing Banco Cooperativo Coopcentral.
     */
    BANCO_COOPERATIVO_COOPCENTRAL("BANCO_COOPERATIVO_COOPCENTRAL"),

    /**
     * Enum representing Banco Compartir.
     */
    BANCO_COMPARTIR("BANCO_COMPARTIR"),

    /**
     * Enum representing Banco Serfinanza.
     */
    BANCO_SERFINANZA("BANCO_SERFINANZA"),

    /**
     * Enum representing Financiera Juriscoop.
     */
    FINANCIERA_JURISCOOP("FINANCIERA_JURISCOOP"),

    /**
     * Enum representing Cooperativa Financiera de Antioquia.
     */
    COOPERATIVA_FINANCIERA_DE_ANTIOQUIA("COOPERATIVA_FINANCIERA_DE_ANTIOQUIA"),

    /**
     * Enum representing Cootrafa Cooperativa Financiera.
     */
    COOTRAFA_COOPERATIVA_FINANCIERA("COOTRAFA_COOPERATIVA_FINANCIERA"),

    /**
     * Enum representing Confiar Cooperativa Financiera.
     */
    CONFIAR_COOPERATIVA_FINANCIERA("CONFIAR_COOPERATIVA_FINANCIERA"),

    /**
     * Enum representing Coltefinanciera.
     */
    COLTEFINANCIERA("COLTEFINANCIERA"),

    /**
     * Enum representing Iris financial institution.
     */
    IRIS("IRIS"),

    /**
     * Enum representing Lulo Bank.
     */
    LULO_BANK("LULO_BANK"),

    /**
     * Enum representing Movii digital wallet.
     */
    MOVII("MOVII");

    private final String code;

    private static final Map<String, BanksCoCodeEnum> banksCoCodeEnumMap = new HashMap<>();

    static {
        for (final BanksCoCodeEnum bank : EnumSet.allOf(BanksCoCodeEnum.class)) {
            banksCoCodeEnumMap.put(bank.getCode(), bank);
        }
    }

    BanksCoCodeEnum(final String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }

    public static BanksCoCodeEnum getBankByCode(final String code) {
        return (code != null && !code.isEmpty())
            ? banksCoCodeEnumMap.get(code.toUpperCase(Locale.ROOT))
            : null;
    }

    public static boolean exists(final String code) {
        return code != null
            && !code.isEmpty()
            && banksCoCodeEnumMap.containsKey(code.toUpperCase(Locale.ROOT));
    }
}