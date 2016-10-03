package com.jbilling.sample;

import com.sapienter.jbilling.server.user.AccountTypeWS;
import com.sapienter.jbilling.server.user.UserWS;
import com.sapienter.jbilling.server.user.partner.*;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;

import java.util.GregorianCalendar;

/**
 * jBilling Agents and Commissions examples.
 * Created by branko on 8/22/16.
 */
public class Ch12AgentsAndCommissions {

    public static final Integer PARTNER_ROLE_ID = Integer.valueOf(4);

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();
        Integer companyId = api.getCallerCompanyId();

        AccountTypeWS accType = Ch1AccountTypes.buildAccountType(companyId);
        Integer accTypeId = api.createAccountType(accType);

        // Build and then create two partners using createPartner().
        UserWS userOne = Ch2CustomerManagement.buildUser(accTypeId, PARTNER_ROLE_ID, companyId);
        PartnerWS partnerOne = buildPartner(userOne);
        Integer partnerOneId = api.createPartner(userOne, partnerOne);

        UserWS userTwo = Ch2CustomerManagement.buildUser(accTypeId, PARTNER_ROLE_ID, companyId);
        PartnerWS partnerTwo = buildPartner(userTwo);
        Integer partnerTwoId = api.createPartner(userTwo, partnerTwo);

        // We can verify that the partners were created properly by fetching them with getPartner().
        partnerOne = api.getPartner(partnerOneId);
        System.out.println("Partner One:\n" + partnerOne);
        partnerTwo = api.getPartner(partnerTwoId);
        System.out.println("Partner Two:\n" + partnerTwo);

        // An agent can be updated using updatePartner().
        partnerOne.setType(PartnerType.MASTER.name());
        Integer userOneId = partnerOne.getUserId();
        userOne = api.getUserWS(userOneId);
        api.updatePartner(userOne, partnerOne);

        // An agent can be deleted using deletePartner().
        api.deletePartner(partnerTwoId);

        // We can setup or update the commission process with createUpdateCommissionProcessConfiguration().
        CommissionProcessConfigurationWS configuration = buildCommissionConfiguration(companyId);
        api.createUpdateCommissionProcessConfiguration(configuration);

        // We can trigger the commission process with calculatePartnerCommissions().
        api.calculatePartnerCommissions();

        // We can get all commission runs with getAllCommissionRuns().
        CommissionProcessRunWS[] commissionRuns = api.getAllCommissionRuns();

        CommissionProcessRunWS thisRun = null;
        for(CommissionProcessRunWS commissionRun : commissionRuns) {
            thisRun = commissionRun;
        }
        System.out.println("Commission run:\n" + thisRun);

        // Or we can also get commissions based on the commission process run with getCommissionsByProcessRunId().
        CommissionWS[] commissions = api.getCommissionsByProcessRunId(thisRun.getId());

        // We can check if the partner commission process is running or not with isPartnerCommissionRunning().
        boolean running = api.isPartnerCommissionRunning();
    }

    public static PartnerWS buildPartner(UserWS user) {

        PartnerWS partner = new PartnerWS();
        // Id of the user.
        partner.setUserId(user.getUserId());
        // Partner type (Can be Standard or Master).
        partner.setType(PartnerType.STANDARD.name());

        return partner;
    }

    public static  CommissionProcessConfigurationWS buildCommissionConfiguration(Integer companyId){

        CommissionProcessConfigurationWS configuration = new CommissionProcessConfigurationWS();

        // Id of the company in which the commission process is being configured.
        configuration.setEntityId(companyId);
        // Next run date of the commission process.
        configuration.setNextRunDate(new GregorianCalendar(2000, 1, 1).getTime());
        // Period unit of the commission process.
        configuration.setPeriodUnitId(Constants.PERIOD_UNIT_MONTH);
        // Period value of the commission process.
        configuration.setPeriodValue(Integer.valueOf(1));

        return configuration;
    }
}
