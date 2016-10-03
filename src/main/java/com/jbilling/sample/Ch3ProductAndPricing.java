package com.jbilling.sample;

import com.sapienter.jbilling.server.item.ItemDTOEx;
import com.sapienter.jbilling.server.item.ItemTypeWS;
import com.sapienter.jbilling.server.pricing.PriceModelWS;
import com.sapienter.jbilling.server.user.AccountTypeWS;
import com.sapienter.jbilling.server.user.UserWS;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;

import java.math.BigDecimal;
import java.util.*;

/**
 * jBilling Product and Pricing examples.
 *
 * @author Hristijan Todorovski
 * @author Bojan Dikovski
 * @since 25-AUG-2016
 */
public class Ch3ProductAndPricing {

    public static final Integer CUSTOMER_ROLE_ID = Integer.valueOf(5);
    public static final String PRICE_ZERO = "ZERO";
    public static final String PRICE_FLAT = "FLAT";
    public static final String PRICE_GRADUATED = "GRADUATED";

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();
        Integer companyId = api.getCallerCompanyId();

        // Creating an account type and customer
        AccountTypeWS accountType = Ch1AccountTypes.buildAccountType(companyId);
        Integer accountTypeId = api.createAccountType(accountType);
        UserWS user = Ch2CustomerManagement.buildUser(accountTypeId, CUSTOMER_ROLE_ID, companyId);
        api.createUser(user);

        // Building and creating the item type (category)
        ItemTypeWS itemType = buildItemType(companyId);
        Integer itemTypeId = api.createItemCategory(itemType);
        System.out.println("Created category with id: " + itemTypeId);

        // Getting the item type by the ID with getItemCategoryById()
        ItemTypeWS itemTypeWS = api.getItemCategoryById(itemTypeId);

        // Updating given category with updateItemCategory()
        itemTypeWS.setDescription("updatedSampleCategory_" + System.currentTimeMillis());
        api.updateItemCategory(itemTypeWS);

        // Get all the item categories avialable to a company with getAllItemCategoriesByEntityId()
        ItemTypeWS[] itemTypes = api.getAllItemCategoriesByEntityId(companyId);

        // Get all item categories with getAllItemCategories()
        itemTypes = api.getAllItemCategories();

        // Build and create an item with createItem()
        String productCode = "sampleProduct_" + System.currentTimeMillis();
        ItemDTOEx item = buildItem(companyId, productCode, itemTypeId, Integer.valueOf(0), withFlatPrice(new Date(), "10"));
        Integer itemId = api.createItem(item);
        System.out.println("Created item with id: " + itemId);

        // The item id can be also obtained with getItemID()
        itemId = api.getItemID(productCode);

        // Get item with getItem(), the item id is mandatory, the customer and pricing fields can be set to null
        item = api.getItem(itemId, null, null);
        System.out.println("Created item:" + item);

        // Updating an item with updateItem()
        item.setDescription("Updated description for " + productCode);
        api.updateItem(item);
        System.out.println("Updated item:" + item);

        // Getting all of the items with getAllItems()
        ItemDTOEx[] items = api.getAllItems();
        // Get all items that belong to a category with getItemByCategory()
        ItemDTOEx[] itemByCategory = api.getItemByCategory(itemTypeId);
        //G et all items that belong to a company with getAllItemsByEntityId()
        ItemDTOEx[] itemsByEntity = api.getAllItemsByEntityId(companyId);

        // Delete the item using deleteItem()
        api.deleteItem(itemId);
        // Delete the item category using deleteItemCategory(). An item category can only be deleted
        // if all of the items belonging to that category are already deleted.
        api.deleteItemCategory(itemTypeId);
    }


    public static ItemTypeWS buildItemType(Integer companyId) {

        ItemTypeWS itemType = new ItemTypeWS();
        // Set the item type description
        itemType.setDescription("sampleCategory_" + System.currentTimeMillis());
        // Set the id of the company for which this item type is created.
        itemType.setEntityId(companyId);
        // Set the availability for other companies
        itemType.setEntities(Arrays.asList(companyId));
        // Set if the item type is global, this will override the entities set above if used.
        itemType.setGlobal(Boolean.FALSE);
        // Setting the order line type of the Items
        itemType.setOrderLineTypeId(Constants.ORDER_LINE_TYPE_ITEM);

        // Optional fields
        // If you want to limit the items from this category to be able to be added only once per order
        itemType.setOnePerOrder(Boolean.FALSE);
        // Setting this item to be limited for the customers,
        // each customer will be able to purchase only one item of this item type
        itemType.setOnePerCustomer(Boolean.FALSE);
        // Flag to allow items of this type to have asset management
        itemType.setAllowAssetManagement(Integer.valueOf(0));

        return itemType;
    }

    public static ItemDTOEx buildItem(Integer companyId, String productCode, Integer itemTypeId, Integer assetManaged,
                                      SortedMap<Date, PriceModelWS> prices) {

        ItemDTOEx item = new ItemDTOEx();
        // Setting the items unique number (product code)
        item.setNumber(productCode);
        // Setting the description of the item
        item.setDescription("Description for " + productCode);
        // Setting a list of categories that this item belongs
        item.setTypes(new Integer[]{itemTypeId});
        // Setting excluded categories
        item.setExcludedTypes(new Integer[0]);
        // Setting the date when this item becomes available for use
        item.setActiveSince(new GregorianCalendar(2006, 1, 1).getTime());
        // Setting the date when this item stops being available for use
        item.setActiveUntil(new GregorianCalendar(2026, 1, 1).getTime());
        // Set the id of the company for which this item is created.
        item.setEntityId(companyId);
        // Set the availability for other companies
        item.setEntities(Arrays.asList(companyId));
        // Set if the item is global, this will override the entities set above if used.
        item.setGlobal(Boolean.FALSE);
        // Flag used to soft delete the item
        item.setDeleted(Integer.valueOf(0));
        // Flag used to enable asset management
        item.setAssetManagementEnabled(assetManaged);
        // Prices for the item.
        item.setDefaultPrices(prices);

        return item;
    }

    public static PriceModelWS zeroPriceModel() {

        return new PriceModelWS(PRICE_ZERO, BigDecimal.ZERO, Constants.PRIMARY_CURRENCY_ID);
    }

    public static PriceModelWS flatPriceModel(String price) {

        return new PriceModelWS(PRICE_FLAT, new BigDecimal(price), Constants.PRIMARY_CURRENCY_ID);
    }

    public static PriceModelWS graduatedPriceModel(String price, String included) {

        PriceModelWS priceModel = new PriceModelWS(PRICE_GRADUATED, new BigDecimal(price), Constants.PRIMARY_CURRENCY_ID);
        priceModel.addAttribute("included", included);
        return priceModel;
    }

    public static SortedMap<Date,PriceModelWS> withZeroPrice(Date fromDate) {

        SortedMap<Date, PriceModelWS> prices = new TreeMap<>();
        prices.put(fromDate, new PriceModelWS(PRICE_ZERO, BigDecimal.ZERO, Constants.PRIMARY_CURRENCY_ID));
        return prices;
    }

    public static SortedMap<Date,PriceModelWS> withFlatPrice(Date fromDate, String price) {

        SortedMap<Date, PriceModelWS> prices = new TreeMap<>();
        prices.put(fromDate, new PriceModelWS(PRICE_FLAT, new BigDecimal(price), Constants.PRIMARY_CURRENCY_ID));
        return prices;
    }

    public static SortedMap<Date,PriceModelWS> withGraduatedPrice(Date fromDate, String price, String included) {

        SortedMap<Date, PriceModelWS> prices = new TreeMap<>();
        PriceModelWS priceModel = new PriceModelWS(PRICE_GRADUATED, new BigDecimal(price), Constants.PRIMARY_CURRENCY_ID);
        priceModel.addAttribute("included", included);
        prices.put(fromDate, priceModel);
        return prices;
    }


}


