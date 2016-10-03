package com.jbilling.sample;

import com.sapienter.jbilling.server.item.*;
import com.sapienter.jbilling.server.order.*;
import com.sapienter.jbilling.server.user.AccountTypeWS;
import com.sapienter.jbilling.server.user.UserWS;
import com.sapienter.jbilling.server.order.OrderChangeWS;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
import com.sapienter.jbilling.server.util.search.BasicFilter;
import com.sapienter.jbilling.server.util.search.Filter;
import com.sapienter.jbilling.server.util.search.SearchCriteria;

import java.io.File;
import java.util.*;

/**
 * jBilling Asset Management examples.
 * Created by branko on 8/26/16.
 */
public class Ch4AssetManagement {

    public static final Integer ENABLED = Integer.valueOf(1);
    public static final Integer DISABLED = Integer.valueOf(0);
    public static final Integer CUSTOMER_ROLE_ID = Integer.valueOf(5);

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();
        Integer companyId = api.getCallerCompanyId();

        // Build and create asset management product category.
        ItemTypeWS itemType = Ch3ProductAndPricing.buildItemType(companyId);
        itemType.setAllowAssetManagement(Integer.valueOf(1));
        addAssetStatuses(itemType);
        Integer itemTypeId = api.createItemCategory(itemType);

        // Retrieve the available asset status from the item category.
        itemType = api.getItemCategoryById(itemTypeId);
        AssetStatusDTOEx availableStatus = itemType.getAssetStatuses().stream()
                                        .filter(status -> 1 == status.getIsAvailable())
                                        .findFirst().orElse(null);

        // Build and create asset product.
        String productCode = "sampleProduct_" + System.currentTimeMillis();
        ItemDTOEx item = Ch3ProductAndPricing.buildItem(companyId, productCode, itemTypeId, Integer.valueOf(1),
                Ch3ProductAndPricing.withFlatPrice(new GregorianCalendar(2010, 1, 1).getTime(), "10"));
        Integer itemId = api.createItem(item);

        // Build and create two assets with createAsset().
        AssetWS assetOne = buildAsset(companyId, itemId, availableStatus);
        Integer assetOneId = api.createAsset(assetOne);
        AssetWS assetTwo = buildAsset(companyId, itemId, availableStatus);
        Integer assetTwoId = api.createAsset(assetTwo);

        // We can verify that the assets are created properly by fetching them with getAsset().
        assetOne = api.getAsset(assetOneId);
        System.out.println("Asset One: \n" + assetOne);
        assetTwo = api.getAsset(assetTwoId);
        System.out.println("Asset Two: \n" + assetTwo);

        AccountTypeWS accountType = Ch1AccountTypes.buildAccountType(companyId);
        Integer accountTypeId = api.createAccountType(accountType);
        UserWS user = Ch2CustomerManagement.buildUser(accountTypeId, CUSTOMER_ROLE_ID, companyId);
        Integer userId = api.createUser(user);

        // An asset can be reserved for a user with reserveAsset().
        api.reserveAsset(assetOneId, userId);

        // A reserved asset can be released with releaseAsset().
        api.releaseAsset(assetOneId, userId);

        // Example for creating an order with an asset.
        OrderChangeStatusWS applyStatus = Ch7OrderConfiguration.buildOrderChangeStatus(
                companyId, ApplyToOrder.YES, Integer.valueOf(1));
        Integer applyStatusId = api.createOrderChangeStatus(applyStatus);
        OrderWS order = Ch8PurchaseOrders.buildOrder(userId, new GregorianCalendar(2016, 1, 1).getTime(), null,
                Constants.PERIOD_UNIT_MONTH, Constants.ORDER_BILLING_POST_PAID, Boolean.FALSE, itemId);
        order.getOrderLines()[0].setAssetIds(new Integer[] {assetOneId});
        OrderChangeWS orderChange = Ch8PurchaseOrders.buildChangeFromLine(order, order.getOrderLines()[0],
                applyStatusId, new GregorianCalendar(2016, 1, 1).getTime());
        Integer orderId = api.createOrder(order, new OrderChangeWS[] {orderChange});

        // We can get all assets from a product category by using getAssetsForCategory().
        Integer[] assetsForCategory = api.getAssetsForCategory(itemTypeId);
        System.out.println("Assets in product category with Id " + itemTypeId + ":");
        for (Integer assetId : assetsForCategory) {
            AssetWS asset = api.getAsset(assetId);
            System.out.println(asset);
        }

        // We can also get all assets from a product by using getAssetsForItem().
        Integer[] assetsForItem = api.getAssetsForItem(itemId);
        System.out.println("Assets in product with Id " + itemId + ":");
        for (Integer assetId : assetsForItem) {
            AssetWS asset = api.getAsset(assetId);
            System.out.println(asset);
        }

        // We can also search for assets with a specific search criteria by using findAssets().
        SearchCriteria searchCriteria = findByCriteria(0, 10, "", SearchCriteria.SortDirection.ASC,
                new BasicFilter[]{ new BasicFilter("id", Filter.FilterConstraint.EQ, assetOneId)});
        AssetSearchResult assetSearchResult = api.findAssets(itemId, searchCriteria);
        System.out.println("Search Result: \n" + assetSearchResult);

        // We can get the asset transitions for an asset with getAssetTransitions().
        AssetTransitionDTOEx[] transitions = api.getAssetTransitions(assetOneId);

        // We can upload assets to a product from a CSV file with startImportAssetJob().
        File file = new File("src/main/resources/assets.csv");
        System.out.println("Uploading assets from " + file.getAbsolutePath());
        api.startImportAssetJob(itemId, "Identifier", "Notes", "Global", "Entities", file.getAbsolutePath(), "error-file-path");

        // We can get the asset assignments for an asset with getAssetAssignmentsForAsset().
        AssetAssignmentWS[] assetAssignments = api.getAssetAssignmentsForAsset(assetOneId);

        // We can also get the asset assignments for an asset by order with getAssetAssignmentsForOrder().
        assetAssignments = api.getAssetAssignmentsForOrder(orderId);

        // An order can be fetched with an asset id by using findOrderForAsset().
        orderId = api.findOrderForAsset(assetOneId, new GregorianCalendar(2000, 1, 1).getTime());

        // Orders can also be fetched within a date range by using findOrdersForAssetAndDateRange().
        Integer[] ordersIds = api.findOrdersForAssetAndDateRange(assetOneId,
                new GregorianCalendar(2000, 1, 1).getTime(),
                new GregorianCalendar(2015, 1, 1).getTime());

        // An asset can be updated by using updateAsset().
        assetOne = api.getAsset(assetOneId);
        assetOne.setNotes("Updated asset");
        api.updateAsset(assetOne);

        // An asset can be deleted by using deleteAsset().
        api.deleteAsset(assetTwoId);
    }

    public static AssetWS buildAsset(Integer companyId, Integer itemId, AssetStatusDTOEx status) {

        AssetWS asset = new AssetWS();
        // Id of the company in which the asset is being created.
        asset.setEntityId(companyId);
        // Ids of the companies to which the asset will be available for usage.
        asset.setEntities(Arrays.asList(companyId));
        // Id of the asset product for which the asset is being created.
        asset.setItemId(itemId);
        // Set if the asset is global, this will override the entities list set above if used.
        asset.setGlobal(Boolean.TRUE);
        // Unique identifier for the asset that is being created.
        asset.setIdentifier("sampleAsset_" + System.currentTimeMillis());
        // Current status of the asset.
        asset.setAssetStatusId(status.getId());
        return asset;
    }

    public static void addAssetStatuses(ItemTypeWS itemType){

        // Build and then add asset statuses to a product category.

        // Available
        AssetStatusDTOEx status = new AssetStatusDTOEx();
        status.setDescription("Available");
        status.setIsAvailable(ENABLED);
        status.setIsDefault(ENABLED);
        status.setIsOrderSaved(DISABLED);
        itemType.getAssetStatuses().add(status);

        // Order Saved
        status = new AssetStatusDTOEx();
        status.setDescription("Order Saved");
        status.setIsAvailable(DISABLED);
        status.setIsDefault(DISABLED);
        status.setIsOrderSaved(ENABLED);
        itemType.getAssetStatuses().add(status);

        // Reserved
        status = new AssetStatusDTOEx();
        status.setDescription("Reserved");
        status.setIsAvailable(DISABLED);
        status.setIsDefault(DISABLED);
        status.setIsOrderSaved(DISABLED);
        itemType.getAssetStatuses().add(status);
    }

    public static SearchCriteria findByCriteria(Integer offset, Integer max, String sort,
                                                SearchCriteria.SortDirection sortDirection, BasicFilter[] filter){

        SearchCriteria searchCriteria = new SearchCriteria();
        // Index of the first result in the entire result list.
        searchCriteria.setOffset(offset);
        // Maximum number of entries to return.
        searchCriteria.setMax(max);
        // Column to sort by.
        searchCriteria.setSort(sort);
        // Sort direction (Can be ascending ASC or descending DESC).
        searchCriteria.setDirection(sortDirection);
        // Filter to use in the sort.
        searchCriteria.setFilters(filter);

        return searchCriteria;
    }
}
