package com.jbilling.sample;

import com.sapienter.jbilling.server.item.ItemDTOEx;
import com.sapienter.jbilling.server.item.ItemTypeWS;
import com.sapienter.jbilling.server.item.PlanItemBundleWS;
import com.sapienter.jbilling.server.item.PlanItemWS;
import com.sapienter.jbilling.server.item.PlanWS;
import com.sapienter.jbilling.server.order.ApplyToOrder;
import com.sapienter.jbilling.server.order.OrderChangeStatusWS;
import com.sapienter.jbilling.server.order.OrderWS;
import com.sapienter.jbilling.server.pricing.PriceModelWS;
import com.sapienter.jbilling.server.user.AccountTypeWS;
import com.sapienter.jbilling.server.user.UserWS;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * jBilling Plans examples.
 *
 * @author Bojan Dikovski
 * @since 30-AUG-2016
 */
public class Ch5Plans {

    public static final Integer CUSTOMER_ROLE_ID = Integer.valueOf(5);

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();
        Integer companyId = api.getCallerCompanyId();

        // Build and create an account type and a user.
        AccountTypeWS accType = Ch1AccountTypes.buildAccountType(companyId);
        Integer accTypeId = api.createAccountType(accType);
        UserWS user = Ch2CustomerManagement.buildUser(accTypeId, CUSTOMER_ROLE_ID, companyId);
        Integer userId = api.createUser(user);

        // Build and create an item type, an item that will be used as a subscription item in the plan,
        // and two more items that will be used as bundled items in the plan.
        Date currentDate = new Date();
        ItemTypeWS itemType = Ch3ProductAndPricing.buildItemType(companyId);
        Integer itemTypeId = api.createItemCategory(itemType);
        String subscriptionCode = "sampleSubProduct_" + System.currentTimeMillis();
        String bundledOneCode = "sampleBundleProductOne_" + System.currentTimeMillis();
        String bundledTwoCode = "sampleBundleProductTwo_" + System.currentTimeMillis();
        ItemDTOEx subscriptionItem = Ch3ProductAndPricing.buildItem(companyId, subscriptionCode, itemTypeId,
                Integer.valueOf(0), Ch3ProductAndPricing.withFlatPrice(currentDate, "10"));
        Integer subscriptionId = api.createItem(subscriptionItem);
        ItemDTOEx bundledOneItem = Ch3ProductAndPricing.buildItem(companyId, bundledOneCode, itemTypeId,
                Integer.valueOf(0), Ch3ProductAndPricing.withFlatPrice(currentDate, "10"));
        Integer bundledOneId = api.createItem(bundledOneItem);
        ItemDTOEx bundledTwoItem = Ch3ProductAndPricing.buildItem(companyId, bundledTwoCode, itemTypeId,
                Integer.valueOf(0), Ch3ProductAndPricing.withFlatPrice(currentDate, "10"));
        Integer bundledTwoId = api.createItem(bundledTwoItem);

        // We use an API call to get the order periods defined for a company,
        // and then pick the appropriate one.
        Integer periodId = Arrays.stream(api.getOrderPeriods())
                .filter(period -> period.getValue().equals(Integer.valueOf(1)))
                .filter(period -> period.getPeriodUnitId().equals(Constants.PERIOD_UNIT_MONTH))
                .findFirst().get().getId();

        // Build and create a plan using createPlan().
        List<PlanItemWS> planItems = new ArrayList<>();
        planItems.add(buildPlanItem(bundledOneId, Ch3ProductAndPricing.flatPriceModel("5"), currentDate, periodId, "5"));
        planItems.add(buildPlanItem(bundledTwoId, Ch3ProductAndPricing.flatPriceModel("20"), currentDate, periodId, "1"));
        PlanWS plan = buildPlan(api, subscriptionId, periodId, planItems);
        Integer planId = api.createPlan(plan);

        // Fetching of a plan WS object is done using getPlanWS().
        plan = api.getPlanWS(planId);
        System.out.println("Created PLAN: " + plan);

        // getAllPlans() returns all available plans.
        PlanWS[] plans = api.getAllPlans();

        // getPlansBySubscriptionItem() returns the ids of all plans that are using a given item as a subscription item.
        Integer[] planIds = api.getPlansBySubscriptionItem(subscriptionId);

        // getPlansByAffectedItem() returns the ids of all plans that are using a given item as a bundled item.
        planIds = api.getPlansByAffectedItem(bundledOneId);

        // A plan can be updated using updatePlan().
        plan.setDescription("updatedPlan_" + System.currentTimeMillis());
        api.updatePlan(plan);
        plan = api.getPlanWS(planId);
        System.out.println("Updated PLAN: " + plan);

        // Creating an order with a plan.
        OrderChangeStatusWS applyStatus = Ch7OrderConfiguration.buildOrderChangeStatus(
                companyId, ApplyToOrder.YES, Integer.valueOf(1));
        Integer applyStatusId = api.createOrderChangeStatus(applyStatus);
        OrderWS order = Ch8PurchaseOrders.buildOrder(userId, new GregorianCalendar(2010, 1, 1).getTime(), null,
                Constants.PERIOD_UNIT_MONTH, Constants.ORDER_BILLING_POST_PAID, Boolean.TRUE, subscriptionId);
        Integer orderId = api.createOrder(order, Ch8PurchaseOrders.buildChangesFromOrder(order, applyStatusId));

        // Creating a second account type and user.
        accType = Ch1AccountTypes.buildAccountType(companyId);
        Integer accTypeTwoId = api.createAccountType(accType);
        user = Ch2CustomerManagement.buildUser(accTypeId, CUSTOMER_ROLE_ID, companyId);
        Integer userTwoId = api.createUser(user);

        // For plan items we can create special prices that apply only to certain customers or account types.
        // createAccountTypePrice() creates an account type price that becomes effective from a specified date.
        // createCustomerPrice() creates a customer price that becomes effective from a specified date.
        api.createAccountTypePrice(accTypeTwoId,
                buildPlanItem(bundledOneId, Ch3ProductAndPricing.flatPriceModel("50"), currentDate, periodId, "5"),
                new GregorianCalendar(2011, 1, 1).getTime());
        api.createCustomerPrice(userTwoId,
                buildPlanItem(bundledOneId, Ch3ProductAndPricing.flatPriceModel("20"), currentDate, periodId, "5"),
                new GregorianCalendar(2011, 1, 1).getTime());

        // getAccountTypePrice() and get getCustomerPrice() can be used to fetch the prices.
        System.out.println("General plan item price: " + plan.getPlanItems().stream()
                            .filter(planItem -> planItem.getItemId().equals(bundledOneId))
                            .findFirst().get().getModel());
        PlanItemWS planItem = api.getAccountTypePrice(accTypeTwoId, bundledOneId);
        System.out.println("Account type price for accountTypeTwo: " + planItem.getModel());
        planItem = api.getCustomerPrice(userTwoId, bundledOneId);
        System.out.println("Customer price for userTwo: " + planItem.getModel());

        // Deleting the special prices with deleteAccountTypePrice() and deleteCustomerPrice().
        api.deleteAccountTypePrice(accTypeTwoId, bundledOneId);
        api.deleteCustomerPrice(userTwoId, bundledOneId);

        // Deleting a plan can be done using deletePlan().
        api.deletePlan(planId);
    }

    public static PlanWS buildPlan(JbillingAPI api, Integer subItemId, Integer periodId, List<PlanItemWS> planItems) {

        PlanWS plan = new PlanWS();
        // This field sets the id of subscription item for the plan.
        plan.setItemId(subItemId);
        // Here we set the id of an existing order period (not the period unit id).
        plan.setPeriodId(periodId);
        // Setting the description for the plan.
        plan.setDescription("samplePlan_" + System.currentTimeMillis());
        // Setting the array of bundled items.
        plan.setPlanItems(planItems);

        return plan;
    }


    public static PlanItemWS buildPlanItem(Integer itemId, PriceModelWS priceModel, Date date,
                                           Integer periodId, String quantity) {

        PlanItemWS planItem = new PlanItemWS();
        // Id of the item that is going to be used as a bundled item in the plan.
        planItem.setItemId(itemId);
        // Price model for the bundled item.
        planItem.setModel(priceModel);
        SortedMap<Date, PriceModelWS> priceModels = new TreeMap<>();
        priceModels.put(date, priceModel);
        planItem.setModels(priceModels);
        PlanItemBundleWS planItemBundle = new PlanItemBundleWS();
        // Here we set the id of an existing order period (not the period unit id).
        planItemBundle.setPeriodId(periodId);
        // Quantity used in the bundle.
        planItemBundle.setQuantity(quantity);
        planItem.setBundle(planItemBundle);

        return planItem;
    }
}
