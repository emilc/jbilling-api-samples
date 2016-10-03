package com.jbilling.sample;

import com.sapienter.jbilling.server.metafields.*;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;

import java.util.Arrays;

/**
 * jBilling Meta Fields examples.
 *
 * @author Bojan Dikovski
 * @since 30-AUG-2016
 */
public class Ch14MetaFields {

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();
        Integer companyId = api.getCallerCompanyId();

        /*
            META FIELD
         */

        // Build and then create an integer meta field and meta field value for customers by using createMetaField().
        MetaFieldWS metaField = buildMetaField(companyId, "Salary", DataType.INTEGER, EntityType.CUSTOMER,
                Boolean.FALSE, Boolean.FALSE, 1, Boolean.TRUE, 100);
        Integer metaFieldId = api.createMetaField(metaField);

        // We can retrieve the meta field by using getMetaField().
        metaField = api.getMetaField(metaFieldId);

        // A meta field can be updated by using updateMetaField().
        metaField.getDefaultValue().setValue(200);
        api.updateMetaField(metaField);

        // A meta field can be deleted with deleteMetaField().
        api.deleteMetaField(metaFieldId);

        // We can retrieve all meta fields from a specific meta field category with getMetaFieldsForEntity().
        MetaFieldWS[] metaFields = api.getMetaFieldsForEntity(EntityType.CUSTOMER.toString());

        /*
            META FIELD GROUP
         */

        // Build and then create a meta field group for customers by using createMetaFieldGroup().
        MetaFieldGroupWS metaFieldGroup = buildMetaFieldGroup(companyId, EntityType.CUSTOMER, 1, metaFields);
        Integer metaFieldGroupId = api.createMetaFieldGroup(metaFieldGroup);

        // We can retrieve the meta field group by using getMetaFieldGroup().
        metaFieldGroup = api.getMetaFieldGroup(metaFieldGroupId);

        // A meta field group can be updated by using updateMetaFieldGroup().
        metaFieldGroup.setName("Updated Group");
        api.updateMetaFieldGroup(metaFieldGroup);

        // A meta field group can be deleted with deleteMetaFieldGroup().
        api.deleteMetaFieldGroup(metaFieldGroupId);

        // We can retrieve all meta field groups from a specific meta field category with getMetaFieldGroupsForEntity().
        MetaFieldGroupWS[] metaFieldGroups = api.getMetaFieldGroupsForEntity(EntityType.CUSTOMER.toString());

}

    public static MetaFieldWS buildMetaField(Integer companyId, String name, DataType dataType,
                                             EntityType entityType, boolean disabled, boolean mandatory,
                                             Integer displayOrder, boolean isPrimary, Object defaultValue) {

        MetaFieldWS metaField = new MetaFieldWS();
        // Id of the company in which the meta field is being created.
        metaField.setEntityId(companyId);
        // Name of the meta field.
        metaField.setName(name);
        // What data type is the meta field.
        metaField.setDataType(dataType);
        // For what meta field category is the meta field being created.
        metaField.setEntityType(entityType);
        // A disabled meta field is unusable.
        metaField.setDisabled(disabled);
        // A mandatory meta field must be filled with a value.
        metaField.setMandatory(mandatory);
        // The order in which the meta field will appear in the entity type.
        metaField.setDisplayOrder(displayOrder);
        // Meta fields created from the Configuration are considered primary by default,
        // other dynamic meta fields created on the fly are not considered as primary.
        metaField.setPrimary(isPrimary);

        // Default value for the meta field.
        MetaFieldValueWS metaFieldValue = new MetaFieldValueWS();
        metaFieldValue.setFieldName(name);
        metaFieldValue.setDisabled(disabled);
        metaFieldValue.setMandatory(mandatory);
        metaFieldValue.setDisplayOrder(displayOrder);
        metaFieldValue.setDataType(dataType);
        metaFieldValue.setValue(defaultValue);
        metaField.setDefaultValue(metaFieldValue);

        return metaField;
    }

    public static MetaFieldGroupWS buildMetaFieldGroup(Integer companyId, EntityType entityType,
                                                       Integer displayOrder, MetaFieldWS[] metaFields) {

        MetaFieldGroupWS metaFieldGroup = new MetaFieldGroupWS();
        // Id of the company in which the meta field group is being created.
        metaFieldGroup.setEntityId(companyId);
        // Name of the meta field group.
        metaFieldGroup.setName("Group Name");
        // For what meta field category is the meta field group being created.
        metaFieldGroup.setEntityType(entityType);
        // The order in which the meta field group will appear in the entity type.
        metaFieldGroup.setDisplayOrder(displayOrder);
        // Which meta fields to be included in the group.
        metaFieldGroup.setMetaFields(metaFields);

        return metaFieldGroup;
    }

    public static MetaFieldValueWS buildMetaFieldValue(String fieldName, Boolean disabled, Boolean mandatory,
                                                       DataType dataType, Integer displayOrder, Object value) {

        MetaFieldValueWS metaFieldValue = new MetaFieldValueWS();
        metaFieldValue.setFieldName(fieldName);
        metaFieldValue.setDisabled(disabled);
        metaFieldValue.setMandatory(mandatory);
        metaFieldValue.setDataType(dataType);
        metaFieldValue.setDisplayOrder(displayOrder);
        metaFieldValue.setValue(value);

        return metaFieldValue;
    }

    public static MetaFieldWS copyMetaField(MetaFieldWS metaField) {

        MetaFieldWS metaFieldCopy = new MetaFieldWS();
        metaFieldCopy.setDataType(metaField.getDataType());
        metaFieldCopy.setDefaultValue(metaField.getDefaultValue());
        metaFieldCopy.setDisabled(metaField.isDisabled());
        metaFieldCopy.setDisplayOrder(metaField.getDisplayOrder());
        metaFieldCopy.setFieldUsage(metaField.getFieldUsage());
        metaFieldCopy.setFilename(metaField.getFilename());
        metaFieldCopy.setMandatory(metaField.isMandatory());
        metaFieldCopy.setName(metaField.getName());
        metaFieldCopy.setValidationRule(metaField.getValidationRule());
        metaFieldCopy.setPrimary(metaField.isPrimary());
        if (metaFieldCopy.getValidationRule() != null) {
            metaFieldCopy.getValidationRule().setId(Integer.valueOf(0));
        }

        if (null != metaField.getDefaultValue()) {
            MetaFieldValueWS defaultValue = new MetaFieldValueWS();
            defaultValue.setFieldName(metaField.getDefaultValue().getFieldName());
            defaultValue.setDisabled(metaField.getDefaultValue().isDisabled());
            defaultValue.setMandatory(metaField.getDefaultValue().isMandatory());
            defaultValue.setDisplayOrder(metaField.getDefaultValue().getDisplayOrder());
            defaultValue.setDataType(metaField.getDefaultValue().getDataType());
            defaultValue.setValue(metaField.getDefaultValue().getValue());
            metaFieldCopy.setDefaultValue(defaultValue);
        }

        return metaFieldCopy;
    }

    public static MetaFieldValueWS[] copyMetaFields(MetaFieldValueWS[] source, boolean clearId) {

        if(source == null) {
            return new MetaFieldValueWS[0];
        }
        MetaFieldValueWS[] copiedValues = Arrays.copyOf(source, source.length);
        if(clearId) {
            Arrays.stream(copiedValues).forEach(value -> value.setId(Integer.valueOf(0)));
        }

        return copiedValues;
    }
}
