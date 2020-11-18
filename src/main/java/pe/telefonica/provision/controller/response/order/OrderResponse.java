package pe.telefonica.provision.controller.response.order;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.telefonica.provision.model.order.Order;
import pe.telefonica.provision.util.DateUtil;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

	private String source;

	private String code;

	private String serviceCode;

	private String phone;

	private String documentType;

	private String documentNumber;

	private String registerDate;

	private String execSuspDate;

	private String execRecoxDate;

	private String note1;

	private String application;

	private String commercialOp;

	private String contactPhone;

	private String contactCellphone;

	private String contactMail;

	private String errorCode;

	private String errorDescription;

	private String receptionDate;

	private String status;

	private String atisOrder;

	private String cmsRequest;

	private String statusOrderCode;

	private String statusOrderDescription;

	private String registerOrderDate;

	private String releaseOrderDate;

	private String note2;

	private String oldResult;

	public OrderResponse fromOrder(Order order) {
		source = order.getSource();
		code = order.getCode();
		serviceCode = order.getServiceCode();
		phone = order.getPhone();
		documentType = order.getDocumentType();
		documentNumber = order.getDocumentNumber();
		registerDate = DateUtil.localDateTimeToString(order.getRegisterDate());
		execSuspDate = order.getExecSuspDate();
		execRecoxDate = order.getExecRecoxDate();
		note1 = order.getNote1();
		application = order.getApplication();
		commercialOp = order.getCommercialOp();
		contactPhone = order.getContactPhone();
		contactCellphone = order.getContactCellphone();
		contactMail = order.getContactMail();
		errorCode = order.getErrorCode();
		errorDescription = order.getErrorDescription();
		receptionDate = order.getReceptionDate();
		status = order.getStatus();
		atisOrder = order.getAtisOrder();
		cmsRequest = order.getCmsRequest();
		statusOrderCode = order.getStatusOrderCode();
		statusOrderDescription = order.getStatusOrderDescription();
		registerOrderDate = order.getRegisterOrderDate();
		releaseOrderDate = order.getReleaseOrderDate();
		note2 = order.getNote2();
		oldResult = order.getOldResult();
		return this;
	}

	public List<OrderResponse> fromOrderList(List<Order> orderList) {
		return orderList.parallelStream().map(order -> {
			return fromOrder(order);
		}).collect(Collectors.toList());
	}
}
