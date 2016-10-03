package com.jbilling.sample;

import com.sapienter.jbilling.server.util.CurrencyWS;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;

import java.math.BigDecimal;
import java.util.Date;

/**
 * jBilling Currencies examples.
 * Created by branko on 9/9/16.
 */
public class Ch14Currencies {

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();

        // Obtain all current currencies with getCurrencies().
        CurrencyWS[] currencies = api.getCurrencies();
        System.out.println("Currencies:");
        for(CurrencyWS currency: currencies){
            System.out.println(currency);
        }

        // We can update a currency by using updateCurrency().
        CurrencyWS eurCurrency = getCurrencyByCode("EUR", currencies);
        eurCurrency.setFromDate(new Date());
        eurCurrency.setRate(new BigDecimal("10.0"));
        eurCurrency.setSysRateAsDecimal(new BigDecimal("10.0"));
        api.updateCurrency(eurCurrency);

        // We can also update multiple currencies at once with updateCurrencies().
        CurrencyWS cadCurrency = getCurrencyByCode("CAD" , currencies);
        cadCurrency.setFromDate(new Date());
        cadCurrency.setRate(new BigDecimal("1"));
        cadCurrency.setSysRateAsDecimal(new BigDecimal("1"));
        CurrencyWS[] updatedCurrencies = new CurrencyWS[2];
        updatedCurrencies[0] = eurCurrency;
        updatedCurrencies[1] = cadCurrency;
        api.updateCurrencies(updatedCurrencies);

        // Obtain the default currency Id of the company by using getCallerCurrencyId().
        Integer callerCurrencyId = api.getCallerCurrencyId();

        // Build and then create a new currency by using createCurrency().
        CurrencyWS newCurrency = buildCurrency("Imaginary Currency", "i", "IMC", "IL", null, new BigDecimal("1.00"));
        Integer newCurrencyId = api.createCurrency(newCurrency);
    }

    public static CurrencyWS buildCurrency(String description, String symbol, String code, String countryCode, BigDecimal rate, BigDecimal sysRate){

        CurrencyWS currency = new CurrencyWS();
        // Name of the currency.
        currency.setDescription(description);
        // Currency symbol.
        currency.setSymbol(symbol);
        // Currency code.
        currency.setCode(code);
        // Currency country code.
        currency.setCountryCode(countryCode);
        // Activates or deactivates the currency.
        currency.setInUse(Boolean.TRUE);
        // Currency exchange rate.
        currency.setRate(rate);
        // Currency system rate.
        currency.setSysRate(sysRate);
        // Date from which this currency setup will take effect.
        currency.setFromDate(null);

        return currency;
    }

    private static CurrencyWS getCurrencyByCode(String currencyCode, CurrencyWS[] currencies) {

        for (CurrencyWS currency : currencies) {
            if (currencyCode.equals(currency.getCode())) {
                return currency;
            }
        }
        return null;
    }
}
