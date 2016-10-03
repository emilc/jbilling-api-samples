package com.jbilling.sample;

import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.PreferenceTypeWS;
import com.sapienter.jbilling.server.util.PreferenceWS;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;

/**
 * jBilling Preferences examples.
 * Created by branko on 9/9/16.
 */
public class Ch14Preferences {

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();

        // Update a single preference by using updatePreference().
        PreferenceWS createCredentialsByDefault = new PreferenceWS(
                new PreferenceTypeWS(Constants.PREFERENCE_CREATE_CREDENTIALS_BY_DEFAULT), "1");
        api.updatePreference(createCredentialsByDefault);

        // We can also update multiple preferences with updatePreferences().
        PreferenceWS[] preferences = new PreferenceWS[2];
        preferences[0] = new PreferenceWS(
                new PreferenceTypeWS(Constants.PREFERENCE_PARTNER_DEFAULT_COMMISSION_TYPE), "INVOICE");
        preferences[1] = new PreferenceWS(
                new PreferenceTypeWS(Constants.PREFERENCE_FORGOT_PASSWORD_EXPIRATION), "12");
        api.updatePreferences(preferences);

        // The current state of a preference can be obtained with getPreference().
        PreferenceWS nextInvoiceNumber = api.getPreference(Constants.PREFERENCE_INVOICE_NUMBER);
        System.out.print("Id: 19 - Next invoice number: " + nextInvoiceNumber.getValue());
    }
}
