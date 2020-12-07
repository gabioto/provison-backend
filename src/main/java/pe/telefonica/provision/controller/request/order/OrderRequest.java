package pe.telefonica.provision.controller.request.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.telefonica.provision.model.order.Order;
import pe.telefonica.provision.util.DateUtil;
import pe.telefonica.provision.util.constants.Constants;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

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

	private String flagMT;

	public Order fromThis() {
		Order order = new Order();
		order.setSource(source);
		order.setCode(code);
		order.setServiceCode(serviceCode);
		order.setPhone(phone);
		order.setDocumentType(documentType);
		order.setDocumentNumber(documentNumber);
		order.setRegisterDate(DateUtil.stringToLocalDateTime(registerDate));
		order.setExecSuspDate(DateUtil.stringToLocalDateTime(execSuspDate));
		order.setExecRecoxDate(DateUtil.stringToLocalDateTime(execRecoxDate));
		order.setNote1(note1);
		order.setApplication(application);
		order.setCommercialOp(commercialOp);
		order.setContactPhone(contactPhone);
		order.setContactCellphone(contactCellphone);
		order.setContactMail(contactMail);
		order.setErrorCode(errorCode);
		order.setErrorDescription(errorDescription);
		order.setReceptionDate(DateUtil.stringToLocalDateTime(receptionDate));
		order.setStatus(status);
		order.setAtisOrder(atisOrder);
		order.setCmsRequest(cmsRequest);
		order.setStatusOrderCode(statusOrderCode);
		order.setStatusOrderDescription(
				source.equals(Constants.SOURCE_ORDERS_ATIS) ? Constants.ATIS_CMS_STATUS.get(statusOrderCode)
						: statusOrderDescription);
		order.setRegisterOrderDate(DateUtil.stringToLocalDateTime(registerOrderDate));
		order.setReleaseOrderDate(DateUtil.stringToLocalDateTime(releaseOrderDate));
		order.setNote2(note2);
		order.setIdResult(oldResult);
		order.setFlagMT(flagMT);
		order.setCommercialOpAtis(source.equals(Constants.SOURCE_ORDERS_ATIS) ? commercialOp : "");

		return order;
	}
}
