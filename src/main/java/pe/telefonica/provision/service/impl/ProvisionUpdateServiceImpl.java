package pe.telefonica.provision.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import pe.telefonica.provision.conf.ProvisionTexts;
import pe.telefonica.provision.controller.request.KafkaTOARequest;
import pe.telefonica.provision.controller.request.MailRequest.MailParameter;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Contact;
import pe.telefonica.provision.controller.request.SMSByIdRequest.Message.MsgParameter;
import pe.telefonica.provision.external.PSIApi;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Status;
import pe.telefonica.provision.model.provision.WoPreStart;
import pe.telefonica.provision.repository.ProvisionRepository;
import pe.telefonica.provision.util.constants.Constants;

public abstract class ProvisionUpdateServiceImpl {

	@Autowired
	private PSIApi restPSI;

	@Autowired
	private ProvisionTexts provisionTexts;

	@Autowired
	private TrazabilidadSecurityApi trazabilidadSecurityApi;

	@Autowired
	private ProvisionRepository provisionRepository;

	public abstract boolean updateInToa(Provision provision, KafkaTOARequest kafkaToaRequest, Status status);
	
	public abstract boolean updateWoPrestart();
	
	public abstract boolean updateWoInit();
	
	public abstract boolean updateWoCompleted();
	
	public abstract boolean updateWoCancel();
	
	public abstract boolean updateWoReschdule();
	
	public abstract boolean updateWoNotDone();
	
	public boolean getCarrier(String phoneNumber) {

		boolean isMovistar = false;
		if (!phoneNumber.trim().equals("")) {
			String switchOnPremise = System.getenv("TDP_SWITCH_ON_PREMISE");
			if (switchOnPremise.equals("true")) {
				isMovistar = restPSI.getCarrier(phoneNumber);
			} else {
				isMovistar = restPSI.getCarrierOld(phoneNumber);
			}
		}
		return isMovistar;
	}

	public void sendEmailToCustomer(Customer objCustomer, WoPreStart objWoPreStart) {
		ArrayList<MailParameter> mailParameters = new ArrayList<MailParameter>();
		String customerFullName = objCustomer.getName();

		MailParameter mailParameter1 = new MailParameter();
		mailParameter1.setParamKey("SHORTNAME");
		if (customerFullName.trim().length() > 0) {
			String[] customerFullNameArrStr = customerFullName.split(" ");
			mailParameter1.setParamValue(customerFullNameArrStr[0]);
		} else {
			mailParameter1.setParamValue("");
		}
		mailParameters.add(mailParameter1);

		MailParameter mailParameter2 = new MailParameter();
		mailParameter2.setParamKey("EMAIL");
		mailParameter2.setParamValue(objCustomer.getMail());
		mailParameters.add(mailParameter2);

		MailParameter mailParameter3 = new MailParameter();
		mailParameter3.setParamKey("TECNICNAME");
		mailParameter3.setParamValue(objWoPreStart.getFullName());
		mailParameters.add(mailParameter3);

		MailParameter mailParameter4 = new MailParameter();
		mailParameter4.setParamKey("TECNICID");
		mailParameter4.setParamValue(objWoPreStart.getDocumentNumber());
		mailParameters.add(mailParameter4);

		mailParameter4 = new MailParameter();
		mailParameter4.setParamKey("TECNICDOCTYPE");
		mailParameter4.setParamValue(objWoPreStart.getDocumentNumber().length() == 8 ? "DNI" : "N° DOCUMENTO");
		mailParameters.add(mailParameter4);

		MailParameter mailParameter5 = new MailParameter();
		mailParameter5.setParamKey("SCHEDULEORDER");
		mailParameter5.setParamValue(provisionTexts.getWebUrl());
		mailParameters.add(mailParameter5);

		trazabilidadSecurityApi.sendMail("192826", mailParameters.toArray(new MailParameter[mailParameters.size()]));
	}

	public void sendSMSWoPrestartContact(Provision provision) {
		if (!Boolean.valueOf(System.getenv("TDP_MESSAGE_PROVISION_ENABLE"))) {
			return;
		}
		List<Contacts> conct = provision.getContacts();

		for (Contacts item : conct) {
			String text = item.getFullName();

			String nameCapitalize = text.substring(0, 1).toUpperCase() + text.substring(1);

			List<MsgParameter> msgParameters = new ArrayList<>();
			MsgParameter paramName = new MsgParameter();
			paramName.setKey(Constants.TEXT_NAME_REPLACE);
			paramName.setValue(nameCapitalize);

			msgParameters.add(paramName);
			// msgParameters.add(paramProduct);

			List<Contact> contacts = new ArrayList<>();

			Contact contactCustomer = new Contact();
			contactCustomer.setPhoneNumber(item.getPhoneNumber());
			contactCustomer.setIsMovistar(item.getCarrier());
			contactCustomer.setFullName(item.getFullName());
			contactCustomer.setHolder(false);
			contacts.add(contactCustomer);

			String urlTraza = provision.getWoPreStart().getTrackingUrl();

			trazabilidadSecurityApi.sendSMS(contacts, Constants.MSG_PRO_SCHEDULE_TECHNICIAN_KEY,
					msgParameters.toArray(new MsgParameter[0]), "", urlTraza);
		}

	}

	public void sendSMSWoPrestartHolder(Provision provision) {
		if (!Boolean.valueOf(System.getenv("TDP_MESSAGE_PROVISION_ENABLE"))) {
			return;
		}
		String text = provision.getCustomer().getName();

		String nameCapitalize = text.substring(0, 1).toUpperCase() + text.substring(1);

		List<MsgParameter> msgParameters = new ArrayList<>();
		MsgParameter paramName = new MsgParameter();
		paramName.setKey(Constants.TEXT_NAME_REPLACE);
		paramName.setValue(nameCapitalize);

		msgParameters.add(paramName);
		// msgParameters.add(paramProduct);

		List<Contact> contacts = new ArrayList<>();

		Contact contactCustomer = new Contact();
		contactCustomer.setPhoneNumber(provision.getCustomer().getPhoneNumber());
		contactCustomer.setIsMovistar(provision.getCustomer().getCarrier());
		contactCustomer.setFullName(provision.getCustomer().getName());
		contactCustomer.setHolder(true);
		contacts.add(contactCustomer);

		String urlTraza = provisionTexts.getWebUrl();
		trazabilidadSecurityApi.sendSMS(contacts, Constants.MSG_FAULT_WOPRESTART,
				msgParameters.toArray(new MsgParameter[0]), urlTraza, "");

	}

	public pe.telefonica.provision.model.Status getInfoStatus(String statusName,
			List<pe.telefonica.provision.model.Status> statusList) {
		pe.telefonica.provision.model.Status localStatus = null;

		if (statusList == null) {
			Optional<pe.telefonica.provision.model.Status> repoStatus = provisionRepository.getInfoStatus(statusName);
			localStatus = repoStatus.get();
		} else {
			for (pe.telefonica.provision.model.Status status : statusList) {
				if (status.getStatusName().equalsIgnoreCase(statusName)) {
					localStatus = status;
				}
			}
		}

		return localStatus;
	}

	public boolean hasCustomerInfo(Customer customer) {
		return customer != null && customer.getName() != null && !customer.getName().isEmpty();
	}
}
