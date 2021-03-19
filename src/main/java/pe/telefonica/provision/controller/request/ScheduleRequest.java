package pe.telefonica.provision.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleRequest extends LogDataFrontendRequest {

	private String requestId;

	private String requestType;

	private String requestName;

	private String selectedDate;

	private String selectedRange;

	private boolean isPilot;
	
	private String channel;

	private String stpsiCode;

	private String xaOrderCode;
	
	private String workZone;

	private CustomerRequest customer;

	private String scheduler;

	private String priority;

	private String customerType;

	private String customerSubType;

	private String phoneNetworkTechnology;

	private String phoneTechnology;

	private String broadbandNetworkTechnology;

	private String broadbandTechnology;

	private String tvNetworkTechnology;

	private String tvTechnology;

}
