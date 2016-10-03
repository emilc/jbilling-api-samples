package com.jbilling.sample;

import com.sapienter.jbilling.server.item.ItemDTOEx;
import com.sapienter.jbilling.server.item.ItemTypeWS;
import com.sapienter.jbilling.server.order.ApplyToOrder;
import com.sapienter.jbilling.server.order.OrderChangeStatusWS;
import com.sapienter.jbilling.server.order.OrderChangeWS;
import com.sapienter.jbilling.server.order.OrderLineWS;
import com.sapienter.jbilling.server.order.OrderWS;
import com.sapienter.jbilling.server.user.AccountTypeWS;
import com.sapienter.jbilling.server.user.UserWS;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * jBilling Purchase Orders examples.
 *
 * @author Bojan Dikovski
 * @since 23-AUG-2016
 */
public class Ch8PurchaseOrders {

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

        // Build and create order change status.
        OrderChangeStatusWS applyStatus = Ch7OrderConfiguration.buildOrderChangeStatus(
                companyId, ApplyToOrder.YES, Integer.valueOf(1));
        Integer applyStatusId = api.createOrderChangeStatus(applyStatus);

        // Build and create order using createOrder().
        OrderWS order = buildOrder(userId, new GregorianCalendar(2010, 1, 1).getTime(), null,
                Constants.PERIOD_UNIT_MONTH, Constants.ORDER_BILLING_POST_PAID, Boolean.TRUE, itemId);
        Integer orderId = api.createOrder(order, buildChangesFromOrder(order, applyStatusId));

        // Get the created order using getOrder().
        order = api.getOrder(orderId);

        // We can update the order using updateOrder().
        OrderChangeWS orderChange = buildChangeFromLine(order, order.getOrderLines()[0],
                applyStatusId, new GregorianCalendar(2010, 1, 10).getTime());
        orderChange.setQuantity(BigDecimal.TEN);
        api.updateOrder(order, new OrderChangeWS[] {orderChange});

        // An order line alone can be fetched using getOrderLine().
        OrderLineWS orderLine = api.getOrderLine(order.getOrderLines()[0].getId());

        // The order line can be updated using updateOrderLine().
        orderLine.setDescription("Updated sampleApp order line for product id: " + orderLine.getItemId());
        api.updateOrderLine(orderLine);

        // We can fetch the last order for a customer, or an arbitrary number of the latest orders for a customer.
        order = api.getLatestOrder(userId);
        Integer[] orderIds = api.getLastOrders(userId, Integer.valueOf(10));
        // Additionally to the two methods above, the latest order can be filtered by those containing
        // an item of a given item type.
        order = api.getLatestOrderByItemType(userId, itemTypeId);
        orderIds = api.getLastOrdersByItemType(userId, itemTypeId, Integer.valueOf(10));

        // An order can be deleted using deleteOrder().
        api.deleteOrder(orderId);
    }

    public static OrderWS buildOrder(Integer userId, Date activeSince, Date activeUntil, Integer orderPeriodId,
                                     int billingTypeId, boolean prorate, Integer... productIds) {

        OrderWS order = new OrderWS();
        // Id of the user for which this order is created.
        order.setUserId(userId);
        // Billing type for the order, prepaid or postpaid.
        order.setBillingTypeId(billingTypeId);
        // Flag used
        order.setProrateFlag(prorate);
        // Id of the period unit for the order.
        order.setPeriod(orderPeriodId);
        // Id of the currency for the order.
        order.setCurrencyId(Constants.PRIMARY_CURRENCY_ID);
        // Date when the order becomes active.
        order.setActiveSince(activeSince);
        // Date when the order stops being active.
        order.setActiveUntil(activeUntil);

        // Setting the order lines for the order.
        List<OrderLineWS> orderLines = new ArrayList<>();
        for (Integer productId : Arrays.asList(productIds)) {
            OrderLineWS orderLine = new OrderLineWS();
            // Order line type id.
            orderLine.setTypeId(Constants.ORDER_LINE_TYPE_ITEM);
            // Id of the product used in the order line.
            orderLine.setItemId(productId);
            // Description for the order line.
            orderLine.setDescription("sampleApp order line for product id: " + productId);
            // Quantity of items used in the order line.
            orderLine.setQuantity(Integer.valueOf(1));
            // Flag used whether the order line should use the item price.
            orderLine.setUseItem(Boolean.TRUE);
            // If the 'UseItem' flag is set to FALSE, this field sets the price of the order line.
            orderLine.setPrice(BigDecimal.ONE);
            // Amount of the order line, price * quantity.
            orderLine.setAmount(BigDecimal.ONE);
            orderLines.add(orderLine);
        }
        order.setOrderLines(orderLines.toArray(new OrderLineWS[0]));

        return order;
    }

    public static OrderChangeWS[] buildChangesFromOrder(OrderWS orderWS, Integer statusId) {

        Date startDate = orderWS.getActiveSince();
        List<OrderChangeWS> orderChanges = new ArrayList<>();
        Map<OrderLineWS, OrderChangeWS> lineToChangeMap = new HashMap<>();
        OrderWS rootOrder = findRootOrderIfPossible(orderWS);

        // Build order changes from the root order.
        for (OrderLineWS orderLine : rootOrder.getOrderLines()) {
            OrderChangeWS orderChange = buildChangeFromLine(orderWS, orderLine, statusId, startDate);
            orderChanges.add(orderChange);
            lineToChangeMap.put(orderLine, orderChange);
        }

        // Build order changes from the child orders.
        LinkedHashSet<OrderWS> childOrders = findChildOrders(rootOrder, new LinkedHashSet<>());
        for (OrderWS childOrder : childOrders) {
            for (OrderLineWS line : childOrder.getOrderLines()) {
                OrderChangeWS change = buildChangeFromLine(childOrder, line, statusId, startDate);
                orderChanges.add(change);
                lineToChangeMap.put(line, change);
            }
        }

        // Here the parent-child links are set for the order changes.
        for (OrderLineWS line : lineToChangeMap.keySet()) {
            if (line.getParentLine() != null) {
                OrderChangeWS change = lineToChangeMap.get(line);
                if (line.getParentLine().getId() > Integer.valueOf(0)) {
                    change.setParentOrderLineId(line.getParentLine().getId());
                } else {
                    OrderChangeWS parentChange = lineToChangeMap.get(line.getParentLine());
                    change.setParentOrderChange(parentChange);
                }
            }
        }

        return orderChanges.toArray(new OrderChangeWS[orderChanges.size()]);
    }

    private static OrderWS findRootOrderIfPossible(OrderWS order) {

        // Finds the root order in an order hierarchy.
        OrderWS rootOrder = order;
        while (rootOrder.getParentOrder() != null && !rootOrder.getParentOrder().equals(order)) {
            rootOrder = rootOrder.getParentOrder();
        }

        return rootOrder;
    }

    private static LinkedHashSet<OrderWS> findChildOrders(OrderWS order, LinkedHashSet<OrderWS> orders) {

        // Finds all the child orders for a given order.
        if (order == null) {
            return null;
        }
        if (order.getChildOrders() != null) {
            List<OrderWS> newChildren = new LinkedList<>(Arrays.asList(order.getChildOrders()));
            newChildren.removeAll(orders);
            orders.addAll(newChildren);
            for (OrderWS childOrder : newChildren) {
                findChildOrders(childOrder, orders);
            }
        }

        return orders;
    }


    public static OrderChangeWS buildChangeFromLine(OrderWS order, OrderLineWS line, Integer statusId, Date startDate) {

        // This method is used to build an order change from an order line.
        OrderChangeWS orderChange = new OrderChangeWS();
        // Setting the id of the order change type.
        orderChange.setOrderChangeTypeId(Constants.ORDER_CHANGE_TYPE_DEFAULT);
        // Setting the id of the order change status.
        orderChange.setUserAssignedStatusId(statusId);
        // Date when the change becomes effective.
        orderChange.setStartDate(startDate);
        // Date when the change will be applied.
        orderChange.setApplicationDate(startDate);
        // Here a connection between the order and order change is made
        if (line.getOrderId() != null && line.getOrderId() > Integer.valueOf(0)) {
            orderChange.setOrderId(line.getOrderId());
        } else {
            orderChange.setOrderWS(order);
        }
        // Setting the order line id if it exists, if not using the use item flag.
        if (line.getId() > Integer.valueOf(0)) {
            orderChange.setOrderLineId(line.getId());
        } else {
            orderChange.setUseItem(line.getUseItem() ? Integer.valueOf(1) : Integer.valueOf(0));
        }
        // Making a connection to the parent order line, if one exists.
        if (line.getParentLine() != null && line.getParentLine().getId() > 0) {
            orderChange.setParentOrderLineId(line.getParentLine().getId());
        }
        // Setting the description of the change.
        orderChange.setDescription(line.getDescription());
        // Id of the item used in the change.
        orderChange.setItemId(line.getItemId());
        // Ids of assets used, if any.
        orderChange.setAssetIds(line.getAssetIds());
        // Setting the price of the change.
        orderChange.setPrice(new BigDecimal(line.getPrice()));
        // Setting the quantity of the change.
        if (line.getDeleted() == Integer.valueOf(0)) {
            if (line.getId() > Integer.valueOf(0)) {
                orderChange.setQuantity(BigDecimal.ZERO);
            } else {
                orderChange.setQuantity(new BigDecimal(line.getQuantity()));
            }
        } else {
            orderChange.setQuantity(new BigDecimal(line.getQuantity()).negate());
        }
        // Setting the flag whether the change will result in line removal.
        orderChange.setRemoval(line.getDeleted());
        // Copying the next billable date from the order, if possible.
        if (order != null) {
            orderChange.setNextBillableDate(order.getNextBillableDay());
        }
        // Flag used when the change should be applied as percentage of the existing order.
        orderChange.setPercentage(line.isPercentage());
        // Copying the meta fields from the order line.
        orderChange.setMetaFields(Ch14MetaFields.copyMetaFields(line.getMetaFields(), true));
        return orderChange;
    }




}


