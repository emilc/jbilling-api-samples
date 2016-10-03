package com.jbilling.sample;

import com.sapienter.jbilling.server.user.AccountTypeWS;
import com.sapienter.jbilling.server.user.ContactWS;
import com.sapienter.jbilling.server.user.UserCodeWS;
import com.sapienter.jbilling.server.user.UserDTOEx;
import com.sapienter.jbilling.server.user.UserWS;
import com.sapienter.jbilling.server.util.Constants;
import com.sapienter.jbilling.server.util.api.JbillingAPI;
import com.sapienter.jbilling.server.util.api.JbillingAPIFactory;
import org.joda.time.LocalDate;

/**
 * jBilling Customer Management examples.
 *
 * Created by branko on 8/18/16.
 */
public class Ch2CustomerManagement {

    public static final Integer CUSTOMER_ROLE_ID = Integer.valueOf(5);

    public static void main(String[] args) throws Exception {

        JbillingAPI api = JbillingAPIFactory.getAPI();
        Integer companyId = api.getCallerCompanyId();

        AccountTypeWS accType = Ch1AccountTypes.buildAccountType(companyId);
        Integer accTypeId = api.createAccountType(accType);

        // Build and then create two users using createUser().
        UserWS userOne = buildUser(accTypeId, CUSTOMER_ROLE_ID, companyId);
        Integer userOneId = api.createUser(userOne);
        UserWS userTwo = buildUser(accTypeId, CUSTOMER_ROLE_ID, companyId);
        Integer userTwoId = api.createUser(userTwo);

        // We can verify that the users were created properly by fetching them with getUserWS().
        userOne = api.getUserWS(userOneId);
        System.out.println("User One:\n" + userOne);
        userTwo = api.getUserWS(userTwoId);
        System.out.println("User Two:\n" + userTwo);

        // A user can be updated using updateUser().
        ContactWS contact = buildContact();
        userOne.setContact(contact);
        api.updateUser(userOne);
        System.out.println("User One contact info: \n" + userOne.getContact());

        // A user can be deleted using deleteUser().
        api.deleteUser(userTwoId);

        // We can verify if a user exists either with userExistsWithName() or with userExistsWithId().
        boolean exists1 = api.userExistsWithName(userOne.getUserName());
        boolean exists2 = api.userExistsWithId(userTwoId);

        // We can retrieve the id of a user for the given user name by using getUserId().
        String userOneName = userOne.getUserName();
        userOneId = api.getUserId(userOneName);
        System.out.println("User One Id: " + userOneId);

        // We can also retrieve all users with a particular status with getUsersInStatus() or getUsersNotInStatus().
        Integer status = UserDTOEx.STATUS_ACTIVE;
        Integer[] usersInStatus = api.getUsersInStatus(status);
        Integer[] usersNotInStatus = api.getUsersNotInStatus(status);

        // Or we can also use getUsersByStatus (TRUE for in status, FALSE for not in status).
        Integer[] usersByStatus = api.getUsersByStatus(status, Boolean.TRUE);

        // Contact information of a user can be obtained by using getUserContactsWS().
        ContactWS[] contactInfo = api.getUserContactsWS(userOneId);

        // And we can update the contact information by using updateUserContact().
        contact.setCity("New York");
        api.updateUserContact(userOneId, contact);

        // Create user code by using createUserCode().
        UserCodeWS userCodeOne = buildUserCode(userOne);
        Integer userCodeOneId = api.createUserCode(userCodeOne);

        // A user code can be updated by using updateUserCode().
        userCodeOne.setId(userCodeOneId);
        api.updateUserCode(userCodeOne);
        System.out.println("User Code: \n" + userCodeOne);

        // We can obtain all user codes for a user by using getUserCodesForUser().
        UserCodeWS[] userOneCodes = api.getUserCodesForUser(userOneId);

        // We can get a list of customers linked to a user code with getCustomersByUserCode().
        Integer[] customersByUserCode = api.getCustomersByUserCode(userCodeOne.getIdentifier());

        // We can also get a list of customers linked to a specific user with getCustomersLinkedToUser().
        Integer[] customersByLinkedUser = api.getCustomersLinkedToUser(userOneId);
    }

    public static UserWS buildUser(Integer accTypeId, Integer role, Integer companyId) {

        UserWS user = new UserWS();
        // The id of the account type which is associated to the user.
        user.setAccountTypeId(accTypeId);
        // Name of the user.
        user.setUserName("sampleUserName_" + System.currentTimeMillis());
        // Id of the company in which the user is being created.
        user.setEntityId(companyId);
        // Default language to apply to the user.
        user.setLanguageId(Constants.LANGUAGE_ENGLISH_ID);
        // Default currency to apply to the user.
        user.setCurrencyId(Constants.PRIMARY_CURRENCY_ID);
        // Role of the user (Refer to Appendix A from the API reference document for User Role codes).
        user.setMainRoleId(role);
        // Status of the user (Refer to Appendix A from the API reference document for User Status codes).
        user.setStatusId(Integer.valueOf(1));

        return user;
    }

    public static ContactWS buildContact() {

        ContactWS contact = new ContactWS();
        contact.setFirstName("John");
        contact.setLastName("Smith");
        contact.setEmail("email@email.com");

        return contact;
    }

    public static UserCodeWS buildUserCode(UserWS user){

        UserCodeWS userCode = new UserCodeWS();
        // Id of the user associated with the user code.
        userCode.setUserId(user.getUserId());
        // User code valid from date.
        userCode.setValidFrom(new LocalDate(2000, 1, 1).toDate());
        // User code valid to date.
        userCode.setValidTo(new LocalDate(2020, 1, 1).toDate());
        // User code unique identifier (User Codes must be in the format of: User name plus 5 digits).
        userCode.setIdentifier(user.getUserName() + "00001");

        return userCode;
    }
}
