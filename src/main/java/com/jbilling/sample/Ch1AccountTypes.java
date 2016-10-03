package com.jbilling.sample;

import com.sapienter.jbilling.server.metafields.DataType;
import com.sapienter.jbilling.server.metafields.EntityType;
import com.sapienter.jbilling.server.metafields.MetaFieldWS;
import com.sapienter.jbilling.server.user.AccountInformationTypeWS;
import com.sapienter.jbilling.server.user.AccountTypeWS;
import com.sapienter.jbilling.server.user.MainSubscriptionWS;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * jBilling Account Type examples.
 *
 * @author Bojan Dikovski
 * @since 17-AUG-2016
 */
public class Ch1AccountTypes {

    public static final Integer EMAIL_DELIVERY_METHOD = Integer.valueOf(1);

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();
        Integer companyId = api.getCallerCompanyId();
        System.out.print(companyId);

        // Build and then create two account types using createAccountType().
        AccountTypeWS accTypeOne = buildAccountType(companyId);
        Integer accTypeOneId = api.createAccountType(accTypeOne);
        AccountTypeWS accTypeTwo = buildAccountType(companyId);
        Integer accTypeTwoId = api.createAccountType(accTypeTwo);

        // We can verify that the account types were created properly by fetching them with getAccountType().
        accTypeOne = api.getAccountType(accTypeOneId);
        System.out.println("Account Type One:\n" + accTypeOne);
        accTypeTwo = api.getAccountType(accTypeTwoId);
        System.out.println("Account Type Two:\n" + accTypeTwo);

        // We can also retrieve all of the created account types using getAllAccountTypes().
        AccountTypeWS[] accountTypes = api.getAllAccountTypes();

        // An account type can be updated using a call to updateAccountType().
        accTypeOne.setCreditLimit(new BigDecimal("100"));
        api.updateAccountType(accTypeOne);

        // An account type can be deleted using a call to deleteAccountType().
        api.deleteAccountType(accTypeTwoId);

        // An account information type can be created using createAccountInformationType()
        AccountInformationTypeWS accInfoType = buildAccountInformationType(accTypeOneId, companyId);
        Integer accTypeInfoId = api.createAccountInformationType(accInfoType);

        // The information type can be obtained using getAccountInformationType()
        accInfoType = api.getAccountInformationType(accTypeInfoId);
        System.out.printf("Account Information Type:\n" + accInfoType);

        // We can also fetch all account information types for a given account type
        // using getInformationTypesForAccountType()
        AccountInformationTypeWS[] informationTypes = api.getInformationTypesForAccountType(accTypeOneId);

        // An account information type can be updated using a call to updateAccountInformationType().
        accInfoType.setName("updatedAccInfoType_" + System.currentTimeMillis());
        api.updateAccountInformationType(accInfoType);

        // An account information type can be deleted using a call to deleteAccountInformationType().
        api.deleteAccountInformationType(accTypeInfoId);
    }

    public static AccountTypeWS buildAccountType(Integer companyId) {

        AccountTypeWS accountType = new AccountTypeWS();
        // Name of the account type.
        accountType.setName("sampleAccType_" + System.currentTimeMillis(), Constants.LANGUAGE_ENGLISH_ID);
        // Id of the company in which the account type will be created.
        accountType.setEntityId(companyId);
        // Billing cycle of the account type. The provided parameters are the period unit and
        // the next invoice day of the period.
        accountType.setMainSubscription(new MainSubscriptionWS(Constants.PERIOD_UNIT_MONTH, Integer.valueOf(1)));
        // Allowed amount of debt for the account type.
        accountType.setCreditLimit(BigDecimal.ZERO);
        // Default currency to apply to the account type.
        accountType.setCurrencyId(Constants.PRIMARY_CURRENCY_ID);
        // Default language to apply to the account type.
        accountType.setLanguageId(Constants.LANGUAGE_ENGLISH_ID);
        // Credit limit notification amount #1.
        accountType.setCreditNotificationLimit1(BigDecimal.ZERO);
        // Credit limit notification amount #2.
        accountType.setCreditNotificationLimit2(BigDecimal.ZERO);
        // Invoice delivery method to apply to the account type.
        accountType.setInvoiceDeliveryMethodId(EMAIL_DELIVERY_METHOD);

        return accountType;
    }

    public static AccountInformationTypeWS buildAccountInformationType(Integer accountTypeId, Integer companyId) {

        AccountInformationTypeWS accInfoType = new AccountInformationTypeWS();
        // The id of the account type with which this account information type is associated.
        accInfoType.setAccountTypeId(accountTypeId);
        // Name of the account information type.
        accInfoType.setName("sampleAccInfoType_" + System.currentTimeMillis());
        // This used so that the information type is used only on account types.
        accInfoType.setEntityType(EntityType.ACCOUNT_TYPE);
        // Id of the company in which the account information type will be used.
        accInfoType.setEntityId(companyId);
        // Display order of the account information type. Used when more information types are added
        // to the same account type.
        accInfoType.setDisplayOrder(Integer.valueOf(1));
        // Meta fields used in the account information type.
        List<MetaFieldWS> metaFieldList = new ArrayList<>();
        metaFieldList.add(Ch14MetaFields.buildMetaField(companyId, "First Name", DataType.STRING, EntityType.ACCOUNT_TYPE,
                Boolean.FALSE, Boolean.FALSE, Integer.valueOf(1), Boolean.FALSE, ""));
        metaFieldList.add(Ch14MetaFields.buildMetaField(companyId, "Phone", DataType.STRING, EntityType.ACCOUNT_TYPE,
                Boolean.FALSE, Boolean.FALSE, Integer.valueOf(2), Boolean.FALSE, ""));
        accInfoType.setMetaFields(metaFieldList.toArray(new MetaFieldWS[metaFieldList.size()]));

        return accInfoType;
    }

}
