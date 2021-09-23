package pe.telefonica.provision.service.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import pe.telefonica.provision.controller.request.order.OrderRequest;
import pe.telefonica.provision.controller.response.ErrorResponse;
import pe.telefonica.provision.controller.response.order.OrderCreateResponse;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.model.order.Order;
import pe.telefonica.provision.repository.OrderRepository;
import pe.telefonica.provision.service.OrderService;
import pe.telefonica.provision.util.StringUtil;
import pe.telefonica.provision.util.constants.Constants;

@Service
public class OrderServiceImpl implements OrderService {

	private static final Log log = LogFactory.getLog(OrderServiceImpl.class);

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private TrazabilidadSecurityApi restSecuritySaveLogData;

	@Async
	@Override
	public ResponseEntity<Object> createOrder(String data) {
		OrderRequest request = formatOrder(data);
		Order order = request.fromThis();
		Order orderSaved = null;
		HttpStatus status;
		boolean success;

		restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
				"code: " + request.getCode() + ", atisCode: " + order.getAtisOrder(), "", "",
				new Gson().toJson(request), "", "INSERT ORDER REQUEST", "", "",
				LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)).format(DateTimeFormatter.ISO_DATE_TIME),
				order.getSource(), "ORDERS_BACK");
		try {
			switch (order.getSource()) {
			case Constants.SOURCE_ORDERS_ORDENES:
			case Constants.SOURCE_ORDERS_VENTAS_FIJA:
				orderSaved = orderRepository.getOrderBySaleCode(order.getCode());
				break;
			case Constants.SOURCE_ORDERS_ATIS:
				orderSaved = orderRepository.getOrderByAtisCode(order.getAtisOrder());
				break;
			default:
				break;
			}
			if (orderSaved != null) {
				if (order.getSource().equals(Constants.SOURCE_ORDERS_VENTAS_FIJA)
						&& orderSaved.getStatus().equals(order.getStatus())) {
					return new ResponseEntity<Object>(
							new ErrorResponse("SVC1000", "Duplicate status field",
									"Status field has the same value as the stored one", "Duplicate status field"),
							HttpStatus.BAD_REQUEST);
				}
				Update update = updateOrderFields(order, orderSaved);
				orderRepository.updateOrder(orderSaved.getIdOrder(), update);
			} else {
				orderRepository.saveOrder(order);
			}

			success = true;
			status = HttpStatus.OK;

			restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
					"code: " + request.getCode() + ", atisCode: " + order.getAtisOrder(), "", "OK",
					new Gson().toJson(request), "", "INSERT ORDER RESPONSE", "", "", LocalDateTime
							.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)).format(DateTimeFormatter.ISO_DATE_TIME),
					order.getSource(), "ORDERS_BACK");

		} catch (Exception e) {
			log.error(this.getClass().getName() + " - Exception: " + e.getMessage());

			success = false;
			status = HttpStatus.INTERNAL_SERVER_ERROR;

			restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
					"code: " + request.getCode() + ", atisCode: " + order.getAtisOrder(), "", "ERROR",
					new Gson().toJson(request), e.getLocalizedMessage(), "INSERT ORDER RESPONSE", "", "", LocalDateTime
							.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)).format(DateTimeFormatter.ISO_DATE_TIME),
					order.getSource(), "ORDERS_BACK");
		}
		return new ResponseEntity<Object>(new OrderCreateResponse(order.getSource(),
				order.getSource().equals(Constants.SOURCE_ORDERS_ATIS) ? order.getAtisOrder() : order.getCode(),
				success), status);
	}

	private Update updateOrderFields(Order order, Order orderSaved) {
		Update update = new Update();
		update.set("source", order.getSource());
		update.set("code", StringUtil.getValue(order.getCode(), orderSaved.getCode()));
		update.set("serviceCode", StringUtil.getValue(order.getServiceCode(), orderSaved.getServiceCode()));
		update.set("phone", StringUtil.getValue(order.getPhone(), orderSaved.getPhone()));
		update.set("documentType", StringUtil.getValue(order.getDocumentType(), orderSaved.getDocumentType()));
		update.set("documentNumber", StringUtil.getValue(order.getDocumentNumber(), orderSaved.getDocumentNumber()));
		update.set("registerDate",
				order.getRegisterDate() != null ? order.getRegisterDate() : orderSaved.getRegisterDate());
		update.set("execSuspDate",
				order.getExecSuspDate() != null ? order.getExecSuspDate() : orderSaved.getExecSuspDate());
		update.set("execRecoxDate",
				order.getExecRecoxDate() != null ? order.getExecRecoxDate() : orderSaved.getExecRecoxDate());
		update.set("note1", StringUtil.getValue(order.getNote1(), orderSaved.getNote1()));
		update.set("application", StringUtil.getValue(order.getApplication(), orderSaved.getApplication()));
		update.set("contactPhone", StringUtil.getValue(order.getContactPhone(), orderSaved.getContactPhone()));
		update.set("contactCellphone",
				StringUtil.getValue(order.getContactCellphone(), orderSaved.getContactCellphone()));
		update.set("contactMail", StringUtil.getValue(order.getContactMail(), orderSaved.getContactMail()));
		update.set("errorCode", StringUtil.getValue(order.getErrorCode(), orderSaved.getErrorCode()));
		update.set("errorDescription",
				StringUtil.getValue(order.getErrorDescription(), orderSaved.getErrorDescription()));
		update.set("receptionDate",
				order.getReceptionDate() != null ? order.getReceptionDate() : orderSaved.getReceptionDate());
		update.set("status", StringUtil.getValue(order.getStatus(), orderSaved.getStatus()));
		update.set("atisOrder", StringUtil.getValue(order.getAtisOrder(), orderSaved.getAtisOrder()));
		update.set("cmsRequest", StringUtil.getValue(order.getCmsRequest(), orderSaved.getCmsRequest()));
		update.set("statusOrderCode", StringUtil.getValue(order.getStatusOrderCode(), orderSaved.getStatusOrderCode()));
		update.set("statusOrderDescription",
				StringUtil.getValue(order.getStatusOrderDescription(), orderSaved.getStatusOrderDescription()));
		update.set("registerOrderDate", order.getRegisterOrderDate() != null ? order.getRegisterOrderDate()
				: orderSaved.getRegisterOrderDate());
		update.set("releaseOrderDate",
				order.getReleaseOrderDate() != null ? order.getReleaseOrderDate() : orderSaved.getReleaseOrderDate());
		update.set("note2", StringUtil.getValue(order.getNote2(), orderSaved.getNote2()));
		update.set("idResult", StringUtil.getValue(order.getIdResult(), orderSaved.getIdResult()));
		update.set("lastUpdateDate", LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)));
		update.set("flagMT", StringUtil.getValue(order.getFlagMT(), orderSaved.getFlagMT()));

		if (!order.getSource().equals(Constants.SOURCE_ORDERS_ATIS)) {
			update.set("commercialOp", StringUtil.getValue(order.getCommercialOp(), orderSaved.getCommercialOp()));
		} else {
			update.set("commercialOpAtis",
					StringUtil.getValue(order.getCommercialOpAtis(), orderSaved.getCommercialOpAtis()));
		}
		return update;
	}

	public OrderRequest formatOrder(String data) {
		OrderRequest orderRequest = new OrderRequest();

		String[] parts = data.split("\\|", -1);
		if (parts[0].equalsIgnoreCase("VENTASFIJA_PARKUR")) {
			orderRequest.setSource(parts[0]);
			orderRequest.setCode(parts[2]);
			orderRequest.setServiceCode("");
			orderRequest.setPhone("");
			orderRequest.setDocumentType(parts[13]);
			orderRequest.setDocumentNumber(parts[4]);
			orderRequest.setRegisterDate(parts[18]);
			orderRequest.setExecSuspDate("");
			orderRequest.setExecRecoxDate("");
			orderRequest.setNote1("");
			orderRequest.setApplication(parts[0]);
			orderRequest.setCommercialOp(parts[12]);
			orderRequest.setContactPhone(parts[5]);
			orderRequest.setContactCellphone("");
			orderRequest.setContactMail(parts[20]);
			orderRequest.setErrorCode("");
			orderRequest.setErrorDescription("");
			orderRequest.setReceptionDate("");
			orderRequest.setStatus(parts[16]);
			// legados
			if (parts[42].equalsIgnoreCase("ATIS")) {
				orderRequest.setAtisOrder(parts[11]);
				orderRequest.setCmsRequest("");
			} else {
				orderRequest.setAtisOrder("");
				orderRequest.setCmsRequest(parts[11]);
			}
			orderRequest.setStatusOrderCode("");
			orderRequest.setStatusOrderDescription("");
			orderRequest.setRegisterOrderDate(parts[19]);
			orderRequest.setReleaseOrderDate("");
			orderRequest.setNote2("");
			orderRequest.setOldResult("");
		} else if (parts[0].equalsIgnoreCase("ATIS")) {
			orderRequest.setSource(parts[0].replaceAll("\\s+", " ").trim());
			orderRequest.setCode("");
			orderRequest.setServiceCode("");
			orderRequest.setPhone(parts[12].replaceAll("\\s+", " ").trim());
			orderRequest.setDocumentType("");
			orderRequest.setDocumentNumber("");
			orderRequest.setRegisterDate("");
			orderRequest.setExecSuspDate("");
			orderRequest.setExecRecoxDate("");
			orderRequest.setNote1("");
			orderRequest.setApplication("");
			orderRequest.setCommercialOp(parts[11].replaceAll("\\s+", " ").trim());
			orderRequest.setContactPhone("");
			orderRequest.setContactCellphone("");
			orderRequest.setContactMail("");
			orderRequest.setErrorCode("");
			orderRequest.setErrorDescription("");
			orderRequest.setReceptionDate("");
			orderRequest.setStatus("");
			orderRequest.setAtisOrder(parts[1].replaceAll("\\s+", " ").trim());
			orderRequest.setCmsRequest("");
			orderRequest.setStatusOrderCode(parts[3].replaceAll("\\s+", " ").trim());
			orderRequest.setStatusOrderDescription(parts[4].replaceAll("\\s+", " ").trim());
			
			// Formater date
			String dateString = parts[6].replaceAll("\\s+", " ").trim() + " 00:00:00";
			SimpleDateFormat dateFormatString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat dateFormatNew = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			try {
				Date date = dateFormatString.parse(dateString);
				dateString = dateFormatNew.format(date);
			} catch (Exception e) {
				log.error(e.getMessage());
				dateString = "";
			}
			orderRequest.setRegisterOrderDate(dateString);
			orderRequest.setReleaseOrderDate("");
			orderRequest.setNote2("");
			orderRequest.setOldResult("");
			orderRequest.setFlagMT(parts[21]);
		} else if (parts[0].equalsIgnoreCase("ORDENES")) {
			SimpleDateFormat dateFormatString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat dateFormatNew = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

			orderRequest.setSource(parts[0]);
			orderRequest.setCode(parts[1]);
			orderRequest.setServiceCode(parts[2]);
			orderRequest.setPhone(parts[3]);
			orderRequest.setDocumentType(parts[4]);
			orderRequest.setDocumentNumber(parts[5]);

			// VALDIATION Y FORMAT RegisterDate
			String registerDateString = parts[7].replaceAll("\\s+", " ").trim();
			if (!registerDateString.isEmpty()) {
				try {
					registerDateString = registerDateString.substring(0, 10) + " "
							+ registerDateString.substring(11, 19);
					Date dateSusp = dateFormatString.parse(registerDateString);
					registerDateString = dateFormatNew.format(dateSusp);
				} catch (Exception e) {
					registerDateString = "";
				}
			}

			// VALDIATION Y FORMAT ExecSuspDate
			String dateSuspString = parts[8].replaceAll("\\s+", " ").trim();
			if (!dateSuspString.isEmpty()) {
				try {
					dateSuspString = dateSuspString.substring(0, 10) + " " + dateSuspString.substring(11, 19);
					Date dateSusp = dateFormatString.parse(dateSuspString);
					dateSuspString = dateFormatNew.format(dateSusp);

				} catch (Exception e) {
					dateSuspString = "";
				}
			}

			// VALDIATION Y FORMAT ExecRecoxDate
			String execRecoxDateString = parts[9].replaceAll("\\s+", " ").trim();
			if (!execRecoxDateString.isEmpty()) {
				try {
					execRecoxDateString = execRecoxDateString.substring(0, 10) + " "
							+ execRecoxDateString.substring(11, 19);
					Date datRecox = dateFormatString.parse(execRecoxDateString);
					execRecoxDateString = dateFormatNew.format(datRecox);
				} catch (Exception e) {
					execRecoxDateString = "";
				}
			}
			orderRequest.setRegisterDate(registerDateString);
			orderRequest.setExecSuspDate(dateSuspString);
			orderRequest.setExecRecoxDate(execRecoxDateString);
			orderRequest.setNote1(parts[10]);
			orderRequest.setApplication(parts[11]);
			orderRequest.setCommercialOp(parts[12]);
			orderRequest.setContactPhone(parts[13]);
			orderRequest.setContactCellphone(parts[14]);
			orderRequest.setContactMail(parts[15]);
			orderRequest.setErrorCode(parts[16]);
			orderRequest.setErrorDescription(parts[17]);

			// VALDIATION Y FORMAT ReceptionDate
			String receptionDateString = parts[18].replaceAll("\\s+", " ").trim();
			if (!receptionDateString.isEmpty()) {
				try {
					receptionDateString = receptionDateString.substring(0, 10) + " "
							+ receptionDateString.substring(11, 19);
					Date reception = dateFormatString.parse(receptionDateString);
					receptionDateString = dateFormatNew.format(reception);
				} catch (Exception e) {
					receptionDateString = "";
				}
				System.out.println(receptionDateString);
			}
			orderRequest.setReceptionDate(receptionDateString);
			orderRequest.setStatus(parts[19]);
			orderRequest.setAtisOrder(parts[20]);
			orderRequest.setCmsRequest(parts[21]);
			orderRequest.setStatusOrderCode(parts[22]);
			orderRequest.setStatusOrderDescription(parts[23]);

			// VALDIATION Y FORMAT RegisterOrderDate
			String registerOrderDateString = parts[24].trim().replaceAll("\\s+", " ").trim();
			if (!registerOrderDateString.isEmpty()) {
				try {
					registerOrderDateString = registerOrderDateString.substring(0, 10) + " "
							+ registerOrderDateString.substring(11, 19);
					Date dateOrder = dateFormatString.parse(registerOrderDateString);
					registerOrderDateString = dateFormatNew.format(dateOrder);
				} catch (Exception e) {
					registerOrderDateString = "";
				}
			}

			// VALDIATION Y FORMAT ReceptionDate
			String releaseOrderString = parts[25].replaceAll("\\s+", " ").trim();
			if (!releaseOrderString.isEmpty()) {
				try {
					execRecoxDateString = releaseOrderString.substring(0, 10) + " " + releaseOrderString.substring(11, 19);
					Date releaseOrder = dateFormatString.parse(releaseOrderString);
					releaseOrderString = dateFormatNew.format(releaseOrder);
				} catch (Exception e) {
					releaseOrderString = "";
				}
				System.out.println(releaseOrderString);
			}
			orderRequest.setRegisterOrderDate(registerOrderDateString);
			orderRequest.setReleaseOrderDate(releaseOrderString);
			orderRequest.setNote2(parts[26]);
			orderRequest.setOldResult(parts[27]);
		}
		return orderRequest;
	}
}