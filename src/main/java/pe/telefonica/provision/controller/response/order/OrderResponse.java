package pe.telefonica.provision.controller.response.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.telefonica.provision.model.order.Order;

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
		return null;
	}
}
