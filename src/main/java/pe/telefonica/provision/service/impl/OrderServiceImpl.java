package pe.telefonica.provision.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pe.telefonica.provision.controller.request.order.OrderRequest;
import pe.telefonica.provision.controller.response.ErrorResponse;
import pe.telefonica.provision.controller.response.order.OrderCreateResponse;
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

	@Override
	public ResponseEntity<Object> createOrder(OrderRequest request) {
		Order order = request.fromThis();
		Order orderSaved = null;
		HttpStatus status;
		boolean success;

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

		} catch (Exception e) {
			log.error(this.getClass().getName() + " - Exception: " + e.getMessage());
			
			success = false;
			status = HttpStatus.INTERNAL_SERVER_ERROR;
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
}
