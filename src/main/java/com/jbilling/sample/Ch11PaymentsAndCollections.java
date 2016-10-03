package com.jbilling.sample;

import com.sapienter.jbilling.server.item.ItemDTOEx;
import com.sapienter.jbilling.server.item.ItemTypeWS;
import com.sapienter.jbilling.server.metafields.DataType;
import com.sapienter.jbilling.server.metafields.EntityType;
import com.sapienter.jbilling.server.metafields.MetaFieldValueWS;
import com.sapienter.jbilling.server.metafields.MetaFieldWS;
import com.sapienter.jbilling.server.order.ApplyToOrder;
import com.sapienter.jbilling.server.order.OrderChangeStatusWS;
import com.sapienter.jbilling.server.order.OrderWS;
import com.sapienter.jbilling.server.payment.PaymentInformationWS;
import com.sapienter.jbilling.server.payment.PaymentMethodTemplateWS;
import com.sapienter.jbilling.server.payment.PaymentMethodTypeWS;
import com.sapienter.jbilling.server.payment.PaymentWS;
import com.sapienter.jbilling.server.process.AgeingWS;
import com.sapienter.jbilling.server.process.ProcessStatusWS;
import com.sapienter.jbilling.server.user.AccountTypeWS;
import com.sapienter.jbilling.server.user.UserWS;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
import org.joda.time.format.DateTimeFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

/**
 * jBilling Payments and Collections examples.
 *
 * @author Bojan Dikovski
 * @since 29-AUG-2016
 */
public class Ch11PaymentsAndCollections {

    public static final Integer CUSTOMER_ROLE_ID = Integer.valueOf(5);
    public final static String CC_MF_CARDHOLDER_NAME = "cc.cardholder.name";
    public final static String CC_MF_NUMBER = "cc.number";
    public final static String CC_MF_EXPIRY_DATE = "cc.expiry.date";
    public final static String CC_MF_TYPE = "cc.type";

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();
        Integer companyId = api.getCallerCompanyId();

        // Build and create an account type and a user.
        AccountTypeWS accType = Ch1AccountTypes.buildAccountType(companyId);
        Integer accTypeId = api.createAccountType(accType);
        UserWS user = Ch2CustomerManagement.buildUser(accTypeId, CUSTOMER_ROLE_ID, companyId);
        Integer userId = api.createUser(user);

        // Build and create an item type and an item.
        ItemTypeWS itemType = Ch3ProductAndPricing.buildItemType(companyId);
        Integer itemTypeId = api.createItemCategory(itemType);
        String productCode = "sampleProduct_" + System.currentTimeMillis();
        ItemDTOEx item = Ch3ProductAndPricing.buildItem(companyId, productCode, itemTypeId,
                Integer.valueOf(0), Ch3ProductAndPricing.withFlatPrice(new Date(), "10"));
        Integer itemId = api.createItem(item);

        // Build and create an order, then create an invoice from the order.
        OrderChangeStatusWS applyStatus = Ch7OrderConfiguration.buildOrderChangeStatus(
                companyId, ApplyToOrder.YES, Integer.valueOf(1));
        Integer applyStatusId = api.createOrderChangeStatus(applyStatus);
        OrderWS order = Ch8PurchaseOrders.buildOrder(userId, new GregorianCalendar(2010, 1, 1).getTime(), null,
                Constants.PERIOD_UNIT_MONTH, Constants.ORDER_BILLING_POST_PAID, Boolean.TRUE, itemId);
        Integer orderId = api.createOrder(order, Ch8PurchaseOrders.buildChangesFromOrder(order, applyStatusId));
        Integer invoiceId = api.createInvoiceFromOrder(orderId, null);

        /*
            PAYMENTS
         */

        // Build and create a payment method type using createPaymentMethodType().
        PaymentMethodTypeWS pmType = buildPaymentMethodType(api);
        Integer pmTypeId = api.createPaymentMethodType(pmType);

        // Verify and fetch the created type using getPaymentMethodType().
        pmType = api.getPaymentMethodType(pmTypeId);
        System.out.println("Created PAYMENT METHOD TYPE: " + pmType);

        // A payment method type can be updated using updatePaymentMethodType().
        pmType.setMethodName("upType_" + System.currentTimeMillis());
        api.updatePaymentMethodType(pmType);
        pmType = api.getPaymentMethodType(pmTypeId);
        System.out.println("Updated PAYMENT METHOD TYPE: " + pmType);

        // A payment can be created using the call createPayment().
        // Here we also create a payment instrument - a credit card.
        Date date = new GregorianCalendar(2010, 1, 1).getTime();
        PaymentInformationWS paymentInformation = buildCreditCard(date, pmTypeId, "Test User", "4123123419441152");
        PaymentWS payment = buildPayment(paymentInformation, date, Constants.PAYMENT_METHOD_VISA, userId);
        Integer paymentId = api.createPayment(payment);

        // We can fetch the created payment using getPayment().
        payment = api.getPayment(paymentId);
        System.out.println("Created PAYMENT: " + payment);

        // Searching for payments can also be done with other API calls.
        // getPaymentsByDate() returns all the payments for the a user between two dates.
        Date since = new GregorianCalendar(2010, 1, 1).getTime();
        Date until = new GregorianCalendar(2011, 1, 1).getTime();
        Integer[] paymentIds = api.getPaymentsByDate(userId, since, until);

        // getLatestPayment() returns the latest payment for a user.
        PaymentWS latestPayment = api.getLatestPayment(userId);

        // getLastPayments() return the last X payments for a user.
        paymentIds = api.getLastPayments(userId, Integer.valueOf(10));

        // Pagination when getting the latest payments for a user is also available with getLastPaymentsPage().
        Integer limit = Integer.valueOf(20);
        Integer offset = Integer.valueOf(10);
        paymentIds = api.getLastPaymentsPage(userId, limit, offset);

        // A payment can be applied to an invoice using createPaymentLink().
        api.createPaymentLink(invoiceId, paymentId);

        // The link can be removed using removePaymentLink().
        api.removePaymentLink(invoiceId, paymentId);

        // Additionally, all links from a payment can be removed using removeAllPaymentLinks().
        api.removeAllPaymentLinks(paymentId);

        // The total amount of payments made by a user can be obtained with a call to getTotalRevenueByUser().
        BigDecimal totalPayment = api.getTotalRevenueByUser(userId);

        // A payment can be updated using updatePayment().
        payment.setAmount(BigDecimal.ZERO);
        api.updatePayment(payment);
        payment = api.getPayment(paymentId);
        System.out.println("Updated PAYMENT: " + payment);

        // A payment can be deleted using deletePayment().
        api.deletePayment(paymentId);

        // A payment method type can be deleted using deletePaymentMethodType().
        api.deletePaymentMethodType(pmTypeId);


        /*
            COLLECTIONS (Old name for this process is Ageing)
         */

        AgeingWS[] collectionsSteps = new AgeingWS[4];
        collectionsSteps[0] = buildAgeingStep(companyId, "Payment Due", Integer.valueOf(0), false, false, false);
        collectionsSteps[1] = buildAgeingStep(companyId, "Grace Period", Integer.valueOf(2), false, true, false);
        collectionsSteps[2] = buildAgeingStep(companyId, "First Retry", Integer.valueOf(3), true, false, false);
        collectionsSteps[3] = buildAgeingStep(companyId, "Suspended", Integer.valueOf(7), false, false, true);

        // A collections configuration can be made with the API call saveAgeingConfiguration().
        api.saveAgeingConfiguration(collectionsSteps, Constants.LANGUAGE_ENGLISH_ID);

        // The current saved collections configuration can be obtained using getAgeingConfiguration().
        collectionsSteps = api.getAgeingConfiguration(Constants.LANGUAGE_ENGLISH_ID);
        for (AgeingWS step : collectionsSteps) {
            System.out.println("Collections step: " + step);
        }

        // The collections process is triggered using triggerAgeing().
        api.triggerAgeing(new GregorianCalendar(2010, 1, 1).getTime());

        // The API call isAgeingProcessRunning() can be used to see if the process is running.
        if (api.isAgeingProcessRunning()) {
            System.out.println("The collections process is running currently.");
        } else {
            System.out.println("The collections process is not running currently.");
        }

        // More detailed information can be obtained using getAgeingProcessStatus().
        // If the process is not currently running, this method will return the status when it last ran.
        ProcessStatusWS processStatus = api.getAgeingProcessStatus();
        System.out.println("Collections process status: " + processStatus);
    }

    public static PaymentMethodTypeWS buildPaymentMethodType(JbillingAPI api) {

        // Using an api method getPaymentMethodTemplate() to obtain an existing payment method template.
        PaymentMethodTemplateWS pmTemplate = api.getPaymentMethodTemplate(Constants.PAYMENT_METHOD_VISA);

        PaymentMethodTypeWS pmType = new PaymentMethodTypeWS();
        // Setting the name of the payment method.
        pmType.setMethodName("pmType_" + System.currentTimeMillis());
        // Flag to set if the payment method is recurring.
        pmType.setIsRecurring(Boolean.FALSE);
        // Setting the id of the payment method template.
        pmType.setTemplateId(pmTemplate.getId());
        // Flag to set if the payment method type is available to all account types.
        pmType.setAllAccountType(Boolean.TRUE);
        // Field used to specify to which account types this type is available,
        // used when the flag above is set to FALSE.
        pmType.setAccountTypes(new ArrayList<>());
        // Copying the meta fields from the payment method template.
        Set<MetaFieldWS> templateMetaFields = pmTemplate.getMetaFields();
        List<MetaFieldWS> metaFields = new ArrayList<>();
        if (templateMetaFields != null && templateMetaFields.size() > 0) {
            for (MetaFieldWS metaField : templateMetaFields) {
                MetaFieldWS metaFieldCopy = Ch14MetaFields.copyMetaField(metaField);
                metaFieldCopy.setEntityId(api.getCallerCompanyId());
                metaFieldCopy.setEntityType(EntityType.PAYMENT_METHOD_TYPE);
                metaFields.add(metaFieldCopy);
            }
        }
        pmType.setMetaFields(metaFields.toArray(new MetaFieldWS[0]));

        return pmType;
    }

    public static PaymentInformationWS buildCreditCard(Date date, Integer methodTypeId,
                                                       String cardHolderName, String cardNumber) {

        PaymentInformationWS paymentInformation = new PaymentInformationWS();
        // Id of the payment method type.
        paymentInformation.setPaymentMethodTypeId(methodTypeId);
        // Id of the payment method.
        paymentInformation.setPaymentMethodId(Constants.PAYMENT_METHOD_VISA);
        // Processing order for the payment insturment.
        paymentInformation.setProcessingOrder(new Integer(1));
        // Setting the meta fields for payment instrument.
        List<MetaFieldValueWS> metaFields = new ArrayList<>();
        metaFields.add(Ch14MetaFields.buildMetaFieldValue(CC_MF_CARDHOLDER_NAME, false, true, DataType.STRING, 1, cardHolderName));
        metaFields.add(Ch14MetaFields.buildMetaFieldValue(CC_MF_NUMBER, false, true, DataType.STRING, 2, cardNumber));
        metaFields.add(Ch14MetaFields.buildMetaFieldValue(CC_MF_EXPIRY_DATE, false, true,
                DataType.STRING, 3, DateTimeFormat.forPattern(Constants.CC_DATE_FORMAT).print(date.getTime())));
        metaFields.add(Ch14MetaFields.buildMetaFieldValue(CC_MF_TYPE, true, false, DataType.INTEGER, 4, new Integer(0)));
        paymentInformation.setMetaFields(metaFields.toArray(new MetaFieldValueWS[metaFields.size()]));

        return paymentInformation;
    }

    public static PaymentWS buildPayment(PaymentInformationWS paymentInformation, Date date,
                                         Integer methodId, Integer userId) {

        PaymentWS payment = new PaymentWS();
        // Setting the amount of the payment.
        payment.setAmount(new BigDecimal("15.00"));
        // Flag that should be set to 1 when the payment is a refund.
        payment.setIsRefund(new Integer(0));
        // Id of the payment method used for this payment.
        payment.setMethodId(methodId);
        // Date when the payment will be applied.
        payment.setPaymentDate(date);
        // Date when the payment is considered to be created.
        payment.setCreateDatetime(date);
        // Setting the id of the payment result id.
        payment.setResultId(Constants.RESULT_ENTERED);
        // Id of the currency in which the payment is made.
        payment.setCurrencyId(Constants.PRIMARY_CURRENCY_ID);
        // Id of the user for whom the payment is made.
        payment.setUserId(userId);
        // Note for the payment.
        payment.setPaymentNotes("sampleAppPayment_" + System.currentTimeMillis());
        payment.getPaymentInstruments().add(paymentInformation);

        return payment;
    }

    public static AgeingWS buildAgeingStep(Integer companyId, String statusStep, Integer days,
                                           Boolean payment, Boolean sendNotification, Boolean suspended){

        AgeingWS ageingWS = new AgeingWS();
        // Id of the company for which this step is created.
        ageingWS.setEntityId(companyId);
        // Step description string.
        ageingWS.setStatusStr(statusStep);
        // How many days after the due date does this step come into effect.
        ageingWS.setDays(days);
        // Flag to be set if in the step a payment try is made.
        ageingWS.setPaymentRetry(payment);
        // Flag to be set if in the step a notification is sent.
        ageingWS.setSendNotification(sendNotification);
        // Flag to be set if in the step the customer is suspended.
        ageingWS.setSuspended(suspended);
        ageingWS.setWelcomeMessage("Welcome_" + System.currentTimeMillis());
        ageingWS.setFailedLoginMessage("Login failed_" + System.currentTimeMillis());

        return ageingWS;
    }

}