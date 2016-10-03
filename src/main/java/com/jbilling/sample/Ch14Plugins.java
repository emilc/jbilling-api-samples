package com.jbilling.sample;

import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskTypeCategoryWS;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskTypeWS;
import com.sapienter.jbilling.server.pluggableTask.admin.PluggableTaskWS;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;

import java.util.Hashtable;

/**
 * jBilling Plug-ins examples.
 *
 * Created by branko on 9/5/16.
 */
public class Ch14Plugins {

    private static final Integer GENERIC_ID = Integer.valueOf(17);
    private static final String GENERIC_INTERFACE_NAME =
            "com.sapienter.jbilling.server.system.event.task.IInternalEventsTask";
    private static final String ORDER_STATUS_TASK_CLASS_NAME =
            "com.sapienter.jbilling.server.order.task.OrderChangeApplyOrderStatusTask";

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();
        Integer companyId = api.getCallerCompanyId();

        /*
            PLUGIN TYPE
         */

        // Fetch a plugin category by its id with getPluginTypeCategory().
        PluggableTaskTypeCategoryWS category = api.getPluginTypeCategory(GENERIC_ID);
        System.out.println("Plugin category with Id " + GENERIC_ID + ":\n" + category);

        // We can also fetch a plugin category by its interface name with getPluginTypeCategoryByInterfaceName().
        category = api.getPluginTypeCategoryByInterfaceName(GENERIC_INTERFACE_NAME);

        // Fetch a plugin type by its class name with getPluginTypeWSByClassName().
        PluggableTaskTypeWS pluginType = api.getPluginTypeWSByClassName(ORDER_STATUS_TASK_CLASS_NAME);
        System.out.println("Plugin type with class name " + ORDER_STATUS_TASK_CLASS_NAME + ":\n" + pluginType);

        // We can also fetch a plugin type by its id with getPluginTypeWS().
        Integer pluginTypeId = pluginType.getId();
        pluginType = api.getPluginTypeWS(pluginTypeId);

        /*
            PLUGIN
         */

        // Build and then create a plugin from a plugin type with createPlugin().
        Hashtable<String, String> parameters = new Hashtable<>();
        PluggableTaskWS plugin = buildPlugin(pluginTypeId, 100, companyId, parameters);
        Integer pluginId = api.createPlugin(plugin);

        // We can verify if the plugin is created by using getPluginWS().
        plugin = api.getPluginWS(pluginId);
        System.out.println("Plugin with Id " + pluginId + ": " + plugin);

        // A plugin can be updated by using updatePlugin().
        plugin.setNotes("This plugin has been updated!");
        api.updatePlugin(plugin);

        // A plugin can be deleted by using deletePlugin().
        api.deletePlugin(pluginId);

        // We can retrieve all plugins of the same type by using getPluginsWS().
        PluggableTaskWS[] plugins = api.getPluginsWS(companyId, ORDER_STATUS_TASK_CLASS_NAME);

        // A plugin can also be retrieved by its own plugin type with getPluginWSByTypeId()
        // (Only if a single plugin of that plugin type exists).
        plugin = api.getPluginWSByTypeId(pluginTypeId);
    }

    public static PluggableTaskWS buildPlugin(Integer typeId, Integer order, Integer companyId,
                                              Hashtable<String, String> parameters){

        PluggableTaskWS plugin = new PluggableTaskWS();
        // Id of the plugin type.
        plugin.setTypeId(typeId);
        // The order in which the plugin will be processed.
        plugin.setProcessingOrder(order);
        // Id of the company in which the plugin is being created.
        plugin.setOwningEntityId(companyId);
        // Mandatory or optional parameters for the plugin.
        plugin.setParameters(parameters);

        return plugin;
    }
}
