package com.jbilling.sample;

import com.sapienter.jbilling.server.discount.DiscountWS;
import com.sapienter.jbilling.server.discount.strategy.DiscountStrategyType;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;

/**
 * jBilling Discounts examples.
 * Created by branko on 8/25/16.
 */
public class Ch6Discounts {

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();
        Integer companyId = api.getCallerCompanyId();

        // Build and then create a discount using createOrUpdateDiscount().
        DiscountWS discount = buildDiscount(companyId, "10");
        Integer discountId = api.createOrUpdateDiscount(discount);

        // We can verify that the discount was created properly by fetching it with getDiscountWS().
        discount = api.getDiscountWS(discountId);
        System.out.println("Discount:\n" + discount);

        // A discount can be deleted using deleteDiscount().
        api.deleteDiscount(discountId);
    }

    public static DiscountWS buildDiscount(Integer companyId, String rate) {

        DiscountWS discount = new DiscountWS();

        // Id of the company in which the discount will be created.
        discount.setEntityId(companyId);
        // Discount code (must be unique).
        discount.setCode("disc_" + System.currentTimeMillis());
        // Name of the discount.
        discount.setDescription("discName_" + System.currentTimeMillis());
        // Type of the discount (can be One Time Amount, One Time Percentage, or Recurring Period Based).
        discount.setType(DiscountStrategyType.ONE_TIME_PERCENTAGE.name());
        // Discount Rate.
        discount.setRate(rate);

        return discount;
    }
}