package pe.telefonica.provision.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.HomePhone;
import pe.telefonica.provision.model.Internet;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.Television;
import pe.telefonica.provision.model.UpFront;
import pe.telefonica.provision.model.rating.Rating;

@Getter
@Setter
public class ProvisionDetailTrazaDto implements Serializable {

	private static final long serialVersionUID = 389605915051581891L;

	private String idProvision;

	private String actionNotDone;

	private String activeStatus;

	private String activityType = "provision";

	private List<ComponentsDto> components = new ArrayList<ComponentsDto>();

	private List<Contacts> contacts = new ArrayList<Contacts>();

	private CustomerDto customer;

	private String dummyStPsiCode;

	private String frontSpeech = "";

	private String genericSpeech = "";

	private Boolean isUpFront = false;

	private boolean isUpdatedummyStPsiCode;

	private String productName;

	private String productType;

	private List<Rating> rating = new ArrayList<>();

	private LocalDateTime registerDate = LocalDateTime.now(ZoneOffset.of("-05:00"));

	private String saleCode;

	private String showLocation;

	private String subReasonNotDone;

	private UpFront upFront;

	private WoPreStartDto woPreStart;

	private String workZone;

	private String xaIdSt;

	private String xaRequest;

	private String textReturn;

	private String scheduler;

	private String priority;

	private String customerType;

	private String customerSubType;

	private Internet internetDetail;

	private Television tvDetail;

	private HomePhone homePhoneDetail;

	public ProvisionDetailTrazaDto fromProvision(Provision provision) {
		this.idProvision = provision.getIdProvision();
		this.actionNotDone = provision.getActionNotDone();
		this.activeStatus = provision.getActiveStatus();
		this.activityType = provision.getActivityType();
		this.components.addAll(provision.getComponents());
		this.contacts.addAll(provision.getContacts());
		this.customer = new CustomerDto().fromCustomer(provision.getCustomer());
		this.dummyStPsiCode = provision.getDummyStPsiCode();
		this.frontSpeech = provision.getFrontSpeech();
		this.genericSpeech = provision.getGenericSpeech();
		this.isUpFront = provision.getIsUpFront();
		this.isUpdatedummyStPsiCode = provision.getIsUpdatedummyStPsiCode();
		this.productName = provision.getProductName();
		this.productType = provision.getProductType();
		this.rating.addAll(provision.getRating());
		this.registerDate = provision.getRegisterDate();
		this.saleCode = provision.getSaleCode();
		this.showLocation = provision.getShowLocation();
		this.subReasonNotDone = provision.getSubReasonNotDone();
		this.upFront = provision.getUpFront();
		this.woPreStart = new WoPreStartDto().fromWoPrestart(provision.getWoPreStart());
		this.workZone = provision.getWorkZone();
		this.xaIdSt = provision.getXaIdSt();
		this.xaRequest = provision.getXaRequest();
		this.textReturn = provision.getTextReturn();
		this.scheduler = provision.getScheduler();
		this.priority = provision.getPriority();
		this.customerType = provision.getCustomerType();
		this.customerSubType = provision.getCustomerSubType();
		this.internetDetail = provision.getInternetDetail();
		this.tvDetail = provision.getTvDetail();
		this.homePhoneDetail = provision.getHomePhoneDetail();
		return this;
	}

}
