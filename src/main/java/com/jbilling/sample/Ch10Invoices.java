package com.jbilling.sample;

import com.sapienter.jbilling.server.invoice.InvoiceWS;
import com.sapienter.jbilling.server.item.ItemDTOEx;
import com.sapienter.jbilling.server.item.ItemTypeWS;
import com.sapienter.jbilling.server.order.ApplyToOrder;
import com.sapienter.jbilling.server.order.OrderChangeStatusWS;
import com.sapienter.jbilling.server.order.OrderWS;
import com.sapienter.jbilling.server.user.AccountTypeWS;
import com.sapienter.jbilling.server.user.UserWS;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * jBilling Invoices examples.
 *
 * @author Bojan Dikovski
 * @since 24-AUG-2016
 */
public class Ch10Invoices {

    public static final Integer CUSTOMER_ROLE_ID = Integer.valueOf(5);

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

        // Build and create an order.
        OrderChangeStatusWS applyStatus = Ch7OrderConfiguration.buildOrderChangeStatus(
                companyId, ApplyToOrder.YES, Integer.valueOf(1));
        Integer applyStatusId = api.createOrderChangeStatus(applyStatus);
        OrderWS order = Ch8PurchaseOrders.buildOrder(userId, new GregorianCalendar(2010, 1, 1).getTime(), null,
                Constants.PERIOD_UNIT_MONTH, Constants.ORDER_BILLING_POST_PAID, Boolean.TRUE, itemId);
        Integer orderId = api.createOrder(order, Ch8PurchaseOrders.buildChangesFromOrder(order, applyStatusId));

        // Besides using the billing process, invoices can be created manually using the call createInvoice().
        // If the boolean flag is set to TRUE, the call will create invoices only if a recurring order is present.
        Integer[] invoiceIds = api.createInvoice(userId, Boolean.FALSE);

        // Additionally an invoice can be created from an order using createInvoiceFromOrder().
        // If the second field is null, a new invoice will be created, otherwise
        // the order will be added to the specified invoice.
        Integer invoiceId = api.createInvoiceFromOrder(orderId, null);

        // An invoice can be fetched using getInvoiceWS().
        InvoiceWS invoice = api.getInvoiceWS(invoiceIds[0]);
        System.out.printf("Created invoice: " + invoice);

        // The are also many different methods to search for invoices:
        // Getting all invoice ids or invoice objects for a user.
        invoiceIds = api.getAllInvoices(userId);
        InvoiceWS[] invoices = api.getAllInvoicesForUser(userId);

        // Getting the last or a number of latest invoices for a user.
        invoice = api.getLatestInvoice(userId);
        invoiceIds = api.getLastInvoices(userId, Integer.valueOf(10));

        // Also there is a way to filter the latest invoices by item type.
        invoice = api.getLatestInvoiceByItemType(userId, itemTypeId);
        invoiceIds = api.getLastInvoicesByItemType(userId, itemTypeId, Integer.valueOf(10));

        // Getting the invoice ids (from all users or for a specific user) in a given date range.
        // Dates are specified as strings in yyyy-mm-dd format.
        invoiceIds = api.getInvoicesByDate("2010-1-1", "2011-1-1");
        invoiceIds = api.getUserInvoicesByDate(userId, "2010-1-1", "2011-1-1");

        // The following method gives support for pagination when fetching user invoices.
        Integer limit = Integer.valueOf(20);
        Integer offset = Integer.valueOf(10);
        invoices = api.getUserInvoicesPage(userId, limit, offset);

        // Getting all unpaid invoices for a user.
        invoiceIds = api.getUnpaidInvoices(userId);

        // Gets the bytes for the generated invoice PDF.
        byte[] invoicePDF = api.getPaperInvoicePDF(invoiceIds[0]);

        // An invoice can be deleted using deleteInvoice().
        api.deleteInvoice(invoiceIds[0]);
    }

}
