package com.tumipay.microservice.infrastructure.component.enums;

import lombok.Getter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * BanksMxCodeEnum
 * <p>
 * BanksMxCodeEnum enum.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 13/04/2026
 */
@Getter
public enum BanksMxCodeEnum {

    /**
     * Enum representing Banamex (Citibanamex) bank.
     */
    BANAMEX("BANAMEX"),

    /**
     * Enum representing BBVA México bank.
     */
    BBVA_MEXICO("BBVA_MEXICO"),

    /**
     * Enum representing Santander México bank.
     */
    SANTANDER("SANTANDER"),

    /**
     * Enum representing HSBC México bank.
     */
    HSBC("HSBC"),

    /**
     * Enum representing Banco del Bajío.
     */
    BAJIO("BAJIO"),

    /**
     * Enum representing Inbursa bank.
     */
    INBURSA("INBURSA"),

    /**
     * Enum representing Banca Mifel.
     */
    MIFEL("MIFEL"),

    /**
     * Enum representing Scotiabank México.
     */
    SCOTIABANK("SCOTIABANK"),

    /**
     * Enum representing Banregio bank.
     */
    BANREGIO("BANREGIO"),

    /**
     * Enum representing Invex bank.
     */
    INVEX("INVEX"),

    /**
     * Enum representing Bansi bank.
     */
    BANSI("BANSI"),

    /**
     * Enum representing Afirme bank.
     */
    AFIRME("AFIRME"),

    /**
     * Enum representing Banorte bank.
     */
    BANORTE("BANORTE"),

    /**
     * Enum representing Bank of America México.
     */
    BANK_OF_AMERICA("BANK_OF_AMERICA"),

    /**
     * Enum representing MUFG Bank México.
     */
    MUFG("MUFG"),

    /**
     * Enum representing JP Morgan México.
     */
    JP_MORGAN("JP_MORGAN"),

    /**
     * Enum representing Bmonex bank.
     */
    BMONEX("BMONEX"),

    /**
     * Enum representing Ve por Más bank.
     */
    VE_POR_MAS("VE_POR_MAS"),

    /**
     * Enum representing Banco Azteca.
     */
    AZTECA("AZTECA"),

    /**
     * Enum representing Autofin bank.
     */
    AUTOFIN("AUTOFIN"),

    /**
     * Enum representing Barclays México.
     */
    BARCLAYS("BARCLAYS"),

    /**
     * Enum representing Compartamos Banco.
     */
    COMPARTAMOS("COMPARTAMOS"),

    /**
     * Enum representing Multiva Banco.
     */
    MULTIVA_BANCO("MULTIVA_BANCO"),

    /**
     * Enum representing Actinver bank.
     */
    ACTINVER("ACTINVER"),

    /**
     * Enum representing Intercam Banco.
     */
    INTERCAM_BANCO("INTERCAM_BANCO"),

    /**
     * Enum representing BanCoppel bank.
     */
    BANCOPPEL("BANCOPPEL"),

    /**
     * Enum representing ABC Capital bank.
     */
    ABC_CAPITAL("ABC_CAPITAL"),

    /**
     * Enum representing Consubanco.
     */
    CONSUBANCO("CONSUBANCO"),

    /**
     * Enum representing Volkswagen Bank México.
     */
    VOLKSWAGEN("VOLKSWAGEN"),

    /**
     * Enum representing CIBanco.
     */
    CIBANCO("CIBANCO"),

    /**
     * Enum representing Bbase bank.
     */
    BBASE("BBASE"),

    /**
     * Enum representing Bankaool bank.
     */
    BANKAOOL("BANKAOOL"),

    /**
     * Enum representing Pagatodo financial institution.
     */
    PAGATODO("PAGATODO"),

    /**
     * Enum representing Inmobiliario Mexicano bank.
     */
    INMOBILIARIO("INMOBILIARIO"),

    /**
     * Enum representing Donde bank.
     */
    DONDE("DONDE"),

    /**
     * Enum representing Bancrea bank.
     */
    BANCREA("BANCREA"),

    /**
     * Enum representing Banco Covalto.
     */
    BANCO_COVALTO("BANCO_COVALTO"),

    /**
     * Enum representing ICBC México.
     */
    ICBC("ICBC"),

    /**
     * Enum representing Banco Sabadell México.
     */
    SABADELL("SABADELL"),

    /**
     * Enum representing Shinhan Bank México.
     */
    SHINHAN("SHINHAN"),

    /**
     * Enum representing Mizuho Bank México.
     */
    MIZUHO_BANK("MIZUHO_BANK"),

    /**
     * Enum representing Bank of China México.
     */
    BANK_OF_CHINA("BANK_OF_CHINA"),

    /**
     * Enum representing Banco S3.
     */
    BANCO_S3("BANCO_S3"),

    /**
     * Enum representing Monexcb financial institution.
     */
    MONEXCB("MONEXCB"),

    /**
     * Enum representing GBM financial institution.
     */
    GBM("GBM"),

    /**
     * Enum representing Masari financial institution.
     */
    MASARI("MASARI"),

    /**
     * Enum representing Value financial institution.
     */
    VALUE("VALUE"),

    /**
     * Enum representing Vector financial institution.
     */
    VECTOR("VECTOR"),

    /**
     * Enum representing Finamex financial institution.
     */
    FINAMEX("FINAMEX"),

    /**
     * Enum representing Valmex financial institution.
     */
    VALMEX("VALMEX"),

    /**
     * Enum representing Profuturo financial institution.
     */
    PROFUTURO("PROFUTURO"),

    /**
     * Enum representing CB Intercam financial institution.
     */
    CB_INTERCAM("CB_INTERCAM"),

    /**
     * Enum representing CI Bolsa financial institution.
     */
    CI_BOLSA("CI_BOLSA"),

    /**
     * Enum representing Fincomún financial institution.
     */
    FINCOMUN("FINCOMUN"),

    /**
     * Enum representing Nu México (Nubank) digital bank.
     */
    NU_MEXICO("NU_MEXICO"),

    /**
     * Enum representing Reforma financial institution.
     */
    REFORMA("REFORMA"),

    /**
     * Enum representing STP (Sistema de Transferencias y Pagos).
     */
    STP("STP"),

    /**
     * Enum representing Credicapital financial institution.
     */
    CREDICAPITAL("CREDICAPITAL"),

    /**
     * Enum representing Kuspit financial institution.
     */
    KUSPIT("KUSPIT"),

    /**
     * Enum representing Unagra financial institution.
     */
    UNAGRA("UNAGRA"),

    /**
     * Enum representing ASP Integra OPC financial institution.
     */
    ASP_INTEGRA_OPC("ASP_INTEGRA_OPC"),

    /**
     * Enum representing Alternativos financial institution.
     */
    ALTERNATIVOS("ALTERNATIVOS"),

    /**
     * Enum representing Libertad financial institution.
     */
    LIBERTAD("LIBERTAD"),

    /**
     * Enum representing Caja Pop Mexicana.
     */
    CAJA_POP_MEXICA("CAJA_POP_MEXICA"),

    /**
     * Enum representing Cristóbal Colón financial institution.
     */
    CRISTOBAL_COLON("CRISTOBAL_COLON"),

    /**
     * Enum representing Caja Telefonista financial institution.
     */
    CAJA_TELEFONIST("CAJA_TELEFONIST"),

    /**
     * Enum representing Transfer financial institution.
     */
    TRANSFER("TRANSFER"),

    /**
     * Enum representing Fondo FIRA financial institution.
     */
    FONDO_FIRA("FONDO_FIRA"),

    /**
     * Enum representing Invercap financial institution.
     */
    INVERCAP("INVERCAP"),

    /**
     * Enum representing Fomped financial institution.
     */
    FOMPED("FOMPED"),

    /**
     * Enum representing Tesored financial institution.
     */
    TESORED("TESORED"),

    /**
     * Enum representing Arcus financial institution.
     */
    ARCUS("ARCUS"),

    /**
     * Enum representing Nvio financial institution.
     */
    NVIO("NVIO"),

    /**
     * Enum representing Mercado Pago México.
     */
    MERCADO_PAGO("MERCADO_PAGO"),

    /**
     * Enum representing Cuenca digital bank.
     */
    CUENCA("CUENCA"),

    /**
     * Enum representing Spin by OXXO digital wallet.
     */
    SPIN("SPIN"),

    /**
     * Enum representing Indeval financial institution.
     */
    INDEVAL("INDEVAL"),

    /**
     * Enum representing Banco de México (Banxico).
     */
    BANXICO("BANXICO"),

    /**
     * Enum representing Bancomext (Banco Nacional de Comercio Exterior).
     */
    BANCOMEXT("BANCOMEXT"),

    /**
     * Enum representing Banobras (Banco Nacional de Obras y Servicios Públicos).
     */
    BANOBRAS("BANOBRAS"),

    /**
     * Enum representing Banjercito bank.
     */
    BANJERCITO("BANJERCITO"),

    /**
     * Enum representing NAFIN (Nacional Financiera).
     */
    NAFIN("NAFIN"),

    /**
     * Enum representing Babien financial institution.
     */
    BABIEN("BABIEN"),

    /**
     * Enum representing Hipotecaria Federal.
     */
    HIPOTECARIA_FED("HIPOTECARIA_FED"),

    /**
     * Enum representing Citi México bank.
     */
    CITI_MEXICO("CITI_MEXICO"),

    /**
     * Enum representing Hey Banco digital bank.
     */
    HEY_BANCO("HEY_BANCO"),

    /**
     * Enum representing Kapital financial institution.
     */
    KAPITAL("KAPITAL"),

    /**
     * Enum representing Ualá digital wallet.
     */
    UALA("UALA"),

    /**
     * Enum representing Klar digital bank.
     */
    KLAR("KLAR"),

    /**
     * Enum representing Crediclub financial institution.
     */
    CREDICLUB("CREDICLUB"),

    /**
     * Enum representing Fondeadora digital bank.
     */
    FONDEADORA("FONDEADORA"),

    /**
     * Enum representing Cashi Cuenta digital wallet.
     */
    CASHI_CUENTA("CASHI_CUENTA"),

    /**
     * Enum representing Mexpago financial institution.
     */
    MEXPAGO("MEXPAGO"),

    /**
     * Enum representing Albo digital bank.
     */
    ALBO("ALBO"),

    /**
     * Enum representing Coopdesarrollo financial institution.
     */
    COOPDESARROLLO("COOPDESARROLLO"),

    /**
     * Enum representing Transfer Direct financial institution.
     */
    TRANSFER_DIRECT("TRANSFER_DIRECT"),

    /**
     * Enum representing Depósitos y Pagos Digitales financial institution.
     */
    DEP_Y_PAG_DIG("DEP_Y_PAG_DIG"),

    /**
     * Enum representing Swap financial institution.
     */
    SWAP("SWAP"),

    /**
     * Enum representing Peibo financial institution.
     */
    PEIBO("PEIBO"),

    /**
     * Enum representing Finco Pay financial institution.
     */
    FINCO_PAY("FINCO_PAY"),

    /**
     * Enum representing Fintoc financial institution.
     */
    FINTOC("FINTOC"),

    /**
     * Enum representing CLS financial institution.
     */
    CLS("CLS"),

    /**
     * Enum representing CODI Valida financial institution.
     */
    CODI_VALIDA("CODI_VALIDA");

    private final String code;

    private static final Map<String, BanksMxCodeEnum> banksMxCodeEnumMap = new HashMap<>();

    static {
        for (final BanksMxCodeEnum banksMxCodeEnum : EnumSet.allOf(BanksMxCodeEnum.class)) {
            banksMxCodeEnumMap.put(banksMxCodeEnum.getCode(), banksMxCodeEnum);
        }
    }

    BanksMxCodeEnum(final String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }

    public static BanksMxCodeEnum getBankByCode(final String code) {
        return !code.isEmpty() ? banksMxCodeEnumMap.get(code.toUpperCase(Locale.ROOT)) : null;
    }

    public static boolean exists(final String code) {
        return code != null
            && !code.isEmpty()
            && banksMxCodeEnumMap.containsKey(code.toUpperCase(Locale.ROOT));
    }
}