package pe.telefonica.provision.controller.response.order;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

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

	@JsonProperty("codigo")
	private String code;

	@JsonProperty("codigoServicio")
	private String serviceCode;

	@JsonProperty("telefono")
	private String phone;

	@JsonProperty("tipoDocumento")
	private String documentType;

	@JsonProperty("numeroDocumento")
	private String documentNumber;

	@JsonProperty("fechaRegistro")
	private String registerDate;

	@JsonProperty("fechaEjecucionSusp")
	private String execSuspDate;

	@JsonProperty("fechaEjecucionRecox")
	private String execRecoxDate;

	@JsonProperty("nota1")
	private String note1;

	@JsonProperty("Aplicación")
	private String application;

	@JsonProperty("TransaccionComercial")
	private String commercialOp;

	@JsonProperty("contactoTelefono")
	private String contactPhone;

	@JsonProperty("contactoCelular")
	private String contactCellphone;

	@JsonProperty("contactoCorreo")
	private String contactMail;

	@JsonProperty("codigoError")
	private String errorCode;

	@JsonProperty("descripcionError")
	private String errorDescription;

	@JsonProperty("fechaRecepcion")
	private String receptionDate;

	@JsonProperty("estado")
	private String status;

	@JsonProperty("pedidoAtis")
	private String atisOrder;

	@JsonProperty("requerimientoCMS")
	private String cmsRequest;

	@JsonProperty("estadoIdOrden")
	private String statusOrderCode;

	@JsonProperty("estadoDescripcionOrden")
	private String statusOrderDescription;

	@JsonProperty("fechaRegistroOrden")
	private String registerOrderDate;

	@JsonProperty("fechaEmisionOrden")
	private String releaseOrderDate;

	@JsonProperty("nota2")
	private String note2;

	@JsonProperty("resultadoId")
	private String idResult;

	public OrderResponse fromOrder(Order order) {
		code = order.getCode();
		serviceCode = order.getServiceCode();
		phone = order.getPhone();
		documentType = order.getDocumentType();
		documentNumber = order.getDocumentNumber();
		registerDate = DateUtil.localDateTimeToString(order.getRegisterDate());
		execSuspDate = DateUtil.localDateTimeToString(order.getExecSuspDate());
		execRecoxDate = DateUtil.localDateTimeToString(order.getExecRecoxDate());
		note1 = order.getNote1();
		application = order.getApplication();
		commercialOp = order.getCommercialOp();
		contactPhone = order.getContactPhone();
		contactCellphone = order.getContactCellphone();
		contactMail = order.getContactMail();
		errorCode = order.getErrorCode();
		errorDescription = order.getErrorDescription();
		receptionDate = DateUtil.localDateTimeToString(order.getReceptionDate());
		status = order.getStatus();
		atisOrder = order.getAtisOrder();
		cmsRequest = order.getCmsRequest();
		statusOrderCode = order.getStatusOrderCode();
		statusOrderDescription = order.getStatusOrderDescription();
		registerOrderDate = DateUtil.localDateTimeToString(order.getRegisterOrderDate());
		releaseOrderDate = DateUtil.localDateTimeToString(order.getReleaseOrderDate());
		note2 = order.getNote2();
		idResult = order.getIdResult();
		return this;
	}

	public List<OrderResponse> fromOrderList(List<Order> orderList) {
		return orderList.parallelStream().map(order -> {
			return fromOrder(order);
		}).collect(Collectors.toList());
	}
}
