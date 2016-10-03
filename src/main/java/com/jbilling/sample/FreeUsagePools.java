package com.jbilling.sample;

import com.sapienter.jbilling.server.item.ItemDTOEx;
import com.sapienter.jbilling.server.item.ItemTypeWS;
import com.sapienter.jbilling.server.item.PlanItemWS;
import com.sapienter.jbilling.server.item.PlanWS;
import com.sapienter.jbilling.server.notification.NotificationMediumType;
import com.sapienter.jbilling.server.order.ApplyToOrder;
import com.sapienter.jbilling.server.order.OrderChangeStatusWS;
import com.sapienter.jbilling.server.order.OrderWS;
import com.sapienter.jbilling.server.usagePool.CustomerUsagePoolWS;
import com.sapienter.jbilling.server.usagePool.UsagePoolConsumptionActionWS;
import com.sapienter.jbilling.server.usagePool.UsagePoolResetValueEnum;
import com.sapienter.jbilling.server.usagePool.UsagePoolWS;
import com.sapienter.jbilling.server.user.AccountTypeWS;
import com.sapienter.jbilling.server.user.UserWS;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;

import java.util.*;

/**
 * jBilling Free Usage Pools examples.
 * Created by branko on 9/13/16.
 */
public class FreeUsagePools {

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();
        Integer companyId = api.getCallerCompanyId();

        // Build and then create item type, subcription item, item, and a plan that will be used for usage pools.
        ItemTypeWS itemType = Ch3ProductAndPricing.buildItemType(api.getCallerCompanyId());
        Integer itemTypeId = api.createItemCategory(itemType);
        List<Integer> itemTypesIds = new ArrayList<>();
        itemTypesIds.add(itemTypeId);

        String subscriptionCode = "sampleSubProduct_" + System.currentTimeMillis();
        ItemDTOEx subscriptionItem = Ch3ProductAndPricing.buildItem(companyId, subscriptionCode, itemTypeId,
                Integer.valueOf(0), Ch3ProductAndPricing.withFlatPrice(new Date(), "10"));
        Integer subscriptionItemId = api.createItem(subscriptionItem);

        String productCode = "sampleProduct_" + System.currentTimeMillis();
        ItemDTOEx item = Ch3ProductAndPricing.buildItem(companyId, productCode, itemTypeId, Integer.valueOf(0),
                Ch3ProductAndPricing.withFlatPrice(new Date(), "10"));
        Integer itemId = api.createItem(item);

        List<Integer> itemsIds = new ArrayList<>();
        itemsIds.add(itemId);

        UsagePoolConsumptionActionWS consumptionAction = buildConsumptionAction(Constants.FUP_CONSUMPTION_FEE,
                NotificationMediumType.EMAIL, null, "50", itemId.toString());
        List<UsagePoolConsumptionActionWS> consumptionActions = new ArrayList<>();
        consumptionActions.add(consumptionAction);

        // Build and then create two usage pools by using createUsagePool().
        UsagePoolWS usagePoolOne = buildUsagePool(companyId, "200 National SMS " + System.currentTimeMillis(), "1", "Months",
                new Integer(1), itemTypesIds, itemsIds, UsagePoolResetValueEnum.ZERO.toString(), consumptionActions);
        Integer usagePoolOneId = api.createUsagePool(usagePoolOne);
        UsagePoolWS usagePoolTwo = buildUsagePool(companyId, "200 National SMS " + System.currentTimeMillis(), "1", "Months",
                new Integer(1), itemTypesIds, itemsIds, UsagePoolResetValueEnum.ZERO.toString(), consumptionActions);
        Integer usagePoolTwoId = api.createUsagePool(usagePoolTwo);

        // Verify that the usage pools were created properly by fetching them with getUsagePoolWS().
        usagePoolOne = api.getUsagePoolWS(usagePoolOneId);
        System.out.println("Usage Pool One:\n" + usagePoolOne);
        usagePoolTwo = api.getUsagePoolWS(usagePoolTwoId);
        System.out.println("Usage Pool Two:\n" + usagePoolTwo);

        // A usage pool can be updated by using updateUsagePool().
        usagePoolOne.setName("Updated 200 National SMS " + System.currentTimeMillis());
        api.updateUsagePool(usagePoolOne);

        // A usage pool can be deleted by using deleteUsagePool().
        api.deleteUsagePool(usagePoolTwoId);

        // We can get all usage pools by using getAllUsagePools().
        UsagePoolWS[] usagePools = api.getAllUsagePools();

        // We can also get usage pools wih a plan Id if they are used by a plan with getUsagePoolsByPlanId().
        PlanWS plan = buildPlanWithUsagePool(api, subscriptionItemId, itemId, usagePoolOneId);
        Integer planId = api.createPlan(plan);
        usagePools = api.getUsagePoolsByPlanId(planId);

        // Get all customer usage pools by using getCustomerUsagePoolsByCustomerId().
        // Customer usage pools are links between a customer and a FUP when an order with a FUP item is created for that customer.
        AccountTypeWS accType = Ch1AccountTypes.buildAccountType(companyId);
        Integer accTypeId = api.createAccountType(accType);

        UserWS user = Ch2CustomerManagement.buildUser(accTypeId, Integer.valueOf(5), companyId);
        Integer userId = api.createUser(user);
        user = api.getUserWS(userId);
        Integer customerId = user.getCustomerId();

        OrderChangeStatusWS applyStatus = Ch7OrderConfiguration.buildOrderChangeStatus(
                companyId, ApplyToOrder.YES, Integer.valueOf(1));
        Integer applyStatusId = api.createOrderChangeStatus(applyStatus);
        OrderWS order = Ch8PurchaseOrders.buildOrder(userId, new GregorianCalendar(2010, 1, 1).getTime(), null,
                Constants.PERIOD_UNIT_MONTH, Constants.ORDER_BILLING_POST_PAID, Boolean.TRUE, subscriptionItemId);
        Integer orderId = api.createOrder(order, Ch8PurchaseOrders.buildChangesFromOrder(order, applyStatusId));

        CustomerUsagePoolWS[] customerUsagePools = api.getCustomerUsagePoolsByCustomerId(customerId);
        System.out.println("Customer usage pools for " + user.getUserName() + ":");
        for(CustomerUsagePoolWS pools : customerUsagePools){System.out.println(pools);}

        // We can also get a customer usage pool by its id with getCustomerUsagePoolById().
        CustomerUsagePoolWS customeUsagePool = api.getCustomerUsagePoolById(customerUsagePools[0].getId());
    }

    public static UsagePoolWS buildUsagePool(Integer companyId, String name, String quantity, String cyclePeriodUnit,
                                             Integer cyclePeriodValue, List<Integer> itemTypesIds, List<Integer> itemsIds,
                                             String resetValue, List<UsagePoolConsumptionActionWS> consumptionActions){

        UsagePoolWS usagePool = new UsagePoolWS();
        // Name of the usage pool.
        usagePool.setName(name);
        // Initial value of free units of usage for a product.
        usagePool.setQuantity(quantity);
        // Period unit of the cycle period.
        usagePool.setCyclePeriodUnit(cyclePeriodUnit);
        // Period value of the cycle period.
        usagePool.setCyclePeriodValue(cyclePeriodValue);
        // Product categories included in the usage pool.
        usagePool.setItemTypes(itemTypesIds.toArray(new Integer[itemTypesIds.size()]));
        // Products included in the usage pool.
        usagePool.setItems(itemsIds.toArray(new Integer[itemsIds.size()]));
        // Id of the company in which the user is being created.
        usagePool.setEntityId(companyId);
        // Value assigned to the pool when the duration period is reached.
        usagePool.setUsagePoolResetValue(resetValue);
        // List of consumption actions that are available for the usage pool.
        usagePool.setConsumptionActions(consumptionActions);

        return usagePool;
    }

    public static UsagePoolConsumptionActionWS buildConsumptionAction(String type, NotificationMediumType mediumType,
                                                                      String notificationId, String percentage, String productId){

        UsagePoolConsumptionActionWS consumptionAction = new UsagePoolConsumptionActionWS();
        // Defines that type of consumption action.
        consumptionAction.setType(type);
        // Notification medium type (EMAIL, SMS, PDF).
        consumptionAction.setMediumType(mediumType);
        // Id of the notification type.
        consumptionAction.setNotificationId(notificationId);
        // Percentage value of the usage pool consumption when the action will take effect.
        consumptionAction.setPercentage(percentage);
        // Id of the product for which the consumption action is applicable.
        consumptionAction.setProductId(productId);

        return consumptionAction;
    }

    private static PlanWS buildPlanWithUsagePool(JbillingAPI api, Integer subscriptionItemId, Integer itemId, Integer... usagePoolIds){

        PlanWS plan;
        List<PlanItemWS> planItems = new ArrayList<>();
        Integer periodId = Arrays.stream(api.getOrderPeriods())
                .filter(period -> period.getValue().equals(Integer.valueOf(1)))
                .filter(period -> period.getPeriodUnitId().equals(Constants.PERIOD_UNIT_MONTH))
                .findFirst().get().getId();
        planItems.add(Ch5Plans.buildPlanItem(itemId, Ch3ProductAndPricing.flatPriceModel("5"), new Date(), periodId, "5"));
        plan = Ch5Plans.buildPlan(api, subscriptionItemId, periodId, planItems);
        plan.setUsagePoolIds(usagePoolIds);

        return plan;
    }
}
