package com.jbilling.sample;

import com.sapienter.jbilling.server.item.ItemTypeWS;
import com.sapienter.jbilling.server.order.ApplyToOrder;
import com.sapienter.jbilling.server.order.OrderChangeStatusWS;
import com.sapienter.jbilling.server.order.OrderChangeTypeWS;
import com.sapienter.jbilling.server.order.OrderPeriodWS;
import com.sapienter.jbilling.server.order.OrderStatusFlag;
import com.sapienter.jbilling.server.order.OrderStatusWS;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.InternationalDescriptionWS;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * jBilling Order Configuration examples.
 *
 * @author Bojan Dikovski
 * @since 19-AUG-2016
 */
public class Ch7OrderConfiguration {

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();
        Integer companyId = api.getCallerCompanyId();

        /*
            ORDER PERIOD
         */

        // Build and then create a new order period using createOrderPeriod().
        OrderPeriodWS monthlyPeriod = buildOrderPeriod(companyId, Constants.PERIOD_UNIT_MONTH, Integer.valueOf(1));
        Integer monthlyPeriodId = api.createOrderPeriod(monthlyPeriod);
        OrderPeriodWS dailyPeriod = buildOrderPeriod(companyId, Constants.PERIOD_UNIT_DAY, Integer.valueOf(1));
        Integer dailyPeriodId = api.createOrderPeriod(dailyPeriod);

        // All existing order periods can be fetched with getOrderPeriods().
        OrderPeriodWS[] orderPeriods = api.getOrderPeriods();
        monthlyPeriod = Arrays.stream(orderPeriods)
                .filter(op -> op.getId().equals(monthlyPeriodId))
                .findFirst()
                .orElse(null);
        System.out.println("Newly created MONTHLY PERIOD: " + monthlyPeriod);
        dailyPeriod = Arrays.stream(orderPeriods)
                .filter(op -> op.getId().equals(dailyPeriodId))
                .findFirst()
                .orElse(null);
        System.out.println("Newly created DAILY PERIOD: " + dailyPeriod);

        // Order periods can be updated by calling updateOrderPeriods()
        monthlyPeriod.getDescriptions().get(0).setContent("Updated description for an order period");
        api.updateOrderPeriods(new OrderPeriodWS[] {monthlyPeriod});

        // An order period can be deleted using deleteOrderPeriod().
        api.deleteOrderPeriod(monthlyPeriodId);
        api.deleteOrderPeriod(dailyPeriodId);

        /*
            ORDER STATUS
         */

        // Get the ids of the 'Invoice' and 'Not Invoice' order statuses using getDefaultOrderStatusId().
        Integer statusInvoiceId = api.getDefaultOrderStatusId(OrderStatusFlag.INVOICE, companyId);
        Integer statusNotInvoiceId = api.getDefaultOrderStatusId(OrderStatusFlag.NOT_INVOICE, companyId);

        // Fetch the order status objects using findOrderStatusById().
        OrderStatusWS statusInvoice = api.findOrderStatusById(statusInvoiceId);
        System.out.println("Existing INVOICE order status: " + statusInvoice);
        OrderStatusWS statusNotInvoice = api.findOrderStatusById(statusNotInvoiceId);
        System.out.println("Existing NOT INVOICE order status: " + statusNotInvoice);

        // Build and then create a new order status using createUpdateOrderStatus().
        statusNotInvoiceId = api.createUpdateOrderStatus(
                buildOrderStatus(api, OrderStatusFlag.NOT_INVOICE, "Not Invoice Order Status"));
        statusNotInvoice = api.findOrderStatusById(statusNotInvoiceId);
        System.out.println("Newly created NOT INVOICE order status: " + statusNotInvoice);

        // Existing order statuses can be deleted with deleteOrderStatus().
        api.deleteOrderStatus(statusNotInvoice);

        /*
            ORDER CHANGE TYPE
         */

        // Build and create an order change type using createUpdateOrderChangeType().
        OrderChangeTypeWS ocType = buildOrderChangeType(api, companyId);
        Integer ocTypeId = api.createUpdateOrderChangeType(ocType);

        // Created order change types can be fetched using by calling getOrderChangeTypeById().
        ocType = api.getOrderChangeTypeById(ocTypeId);
        System.out.println("Newly created ORDER CHANGE TYPE: " + ocType);

        // An order change type can updated with the same call used for creation, createUpdateOrderChangeType().
        ocType.setName("updatedOrderChangeType_" + System.currentTimeMillis());
        api.createUpdateOrderChangeType(ocType);
        ocType = api.getOrderChangeTypeById(ocTypeId);
        System.out.println("Updated ORDER CHANGE TYPE: " + ocType);

        // An order change type can be deleted using deleteOrderChangeType().
        api.deleteOrderChangeType(ocTypeId);

        // All the created order change types in a company can be obtained by calling getOrderChangeTypesForCompany().
        OrderChangeTypeWS[] ocTypes = api.getOrderChangeTypesForCompany();

        /*
            ORDER CHANGE STATUS
         */

        // Build and create an order change status using createOrderChangeStatus().
        OrderChangeStatusWS ocStatus = buildOrderChangeStatus(companyId, ApplyToOrder.NO, Integer.valueOf(1));
        Integer ocStatusId = api.createOrderChangeStatus(ocStatus);

        // All order change statuses for a company can be fetched using getOrderChangeStatusesForCompany().
        ocStatus = Arrays.stream(api.getOrderChangeStatusesForCompany())
                .filter(ocs -> ocs.getId().equals(ocStatusId))
                .findFirst()
                .orElse(null);
        System.out.println("Newly created ORDER CHANGE STATUS: " + ocStatus);

        // An order change status can be updated using updateOrderChangeStatus().
        ocStatus.setApplyToOrder(ApplyToOrder.YES);
        api.updateOrderChangeStatus(ocStatus);
        ocStatus = Arrays.stream(api.getOrderChangeStatusesForCompany())
                .filter(ocs -> ocs.getId().equals(ocStatusId))
                .findFirst()
                .orElse(null);
        System.out.println("Updated ORDER CHANGE STATUS: " + ocStatus);

        // An order change status can be deleted using deleteOrderChangeStatus().
        api.deleteOrderChangeStatus(ocStatusId);

    }

    public static OrderPeriodWS buildOrderPeriod(Integer companyId, Integer periodUnitId, Integer periodValue) {

        OrderPeriodWS orderPeriod = new OrderPeriodWS();
        // Setting the company for which this order period is created.
        orderPeriod.setEntityId(companyId);
        // Setting the period unit id of the order period.
        orderPeriod.setPeriodUnitId(periodUnitId);
        // Setting the period value of the order period.
        orderPeriod.setValue(periodValue);
        // Setting the description of the order period.
        List<InternationalDescriptionWS> descriptions = new ArrayList<>();
        descriptions.add(new InternationalDescriptionWS(Constants.LANGUAGE_ENGLISH_ID,
                "Sample app order period with unit id: " + periodUnitId + " and value: " + periodValue));
        orderPeriod.setDescriptions(descriptions);

        return orderPeriod;
    }

    public static OrderStatusWS buildOrderStatus(JbillingAPI api, OrderStatusFlag flag, String name) {

        OrderStatusWS orderStatus = new OrderStatusWS();
        // Setting the company for which this order status is created.
        // An API call to get the caller company is used here.
        orderStatus.setEntity(api.getCompany());
        // Setting the flag of the order status.
        orderStatus.setOrderStatusFlag(flag);
        // Setting the name of the order status.
        orderStatus.setDescription(name + "_" + System.currentTimeMillis());
        // Setting the description of the order status.
        List<InternationalDescriptionWS> descriptions = new ArrayList<>();
        descriptions.add(new InternationalDescriptionWS(Constants.LANGUAGE_ENGLISH_ID,
                "Sample app order status with flag: " + flag.toString()));
        orderStatus.setDescriptions(descriptions);

        return orderStatus;
    }

    public static OrderChangeTypeWS buildOrderChangeType(JbillingAPI api, Integer companyId) {

        OrderChangeTypeWS ocType = new OrderChangeTypeWS();
        // Setting the name of the order change type.
        ocType.setName("orderChangeType_" + System.currentTimeMillis());
        // Id of the company for which this order change type is created.
        ocType.setEntityId(companyId);
        // Flag to set if this order change type will be the default one.
        ocType.setDefaultType(false);
        // Flag to set if this order change type will allow order status changes.
        ocType.setAllowOrderStatusChange(true);
        // Item categories to which this order change type can be applied.
        // An API call is used to obtain all the existing item types.
        ocType.setItemTypes(Arrays.stream(api.getAllItemCategories())
                                .map(ItemTypeWS::getId)
                                .collect(Collectors.toList()));

        return ocType;
    }

    public static OrderChangeStatusWS buildOrderChangeStatus(Integer companyId, ApplyToOrder apply, Integer order) {

        OrderChangeStatusWS ocStatus = new OrderChangeStatusWS();
        // Id of the company for which this order change status is created.
        ocStatus.setEntityId(companyId);
        // Flag to set whether this order change status will apply to an order.
        ocStatus.setApplyToOrder(apply);
        // Flag used for soft deletion of the order change status.
        ocStatus.setDeleted(Integer.valueOf(0));
        // Number for which the status will appear in on the order.
        ocStatus.setOrder(order);
        // Setting the description of the order change status.
        InternationalDescriptionWS description = new InternationalDescriptionWS();
        description.setLanguageId(Constants.LANGUAGE_ENGLISH_ID);
        description.setContent("orderChangeStatus_" + System.currentTimeMillis());
        ocStatus.addDescription(description);

        return ocStatus;
    }

}
