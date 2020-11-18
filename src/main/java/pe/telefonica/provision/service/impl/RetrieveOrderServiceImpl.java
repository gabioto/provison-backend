package pe.telefonica.provision.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import pe.telefonica.provision.controller.response.ErrorResponse;
import pe.telefonica.provision.controller.response.order.OrderResponse;
import pe.telefonica.provision.model.order.Order;
import pe.telefonica.provision.repository.OrderRepository;
import pe.telefonica.provision.service.RetreiveOrderService;
import pe.telefonica.provision.util.DateUtil;
import pe.telefonica.provision.util.constants.Constants;

@Service
public class RetrieveOrderServiceImpl implements RetreiveOrderService {

	private static final Log log = LogFactory.getLog(OrderServiceImpl.class);

	@Autowired
	private OrderRepository orderRepository;

	private MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();

	@Override
	public ResponseEntity<Object> getOrder(String uServiceId, String uPid, String code, String originSystem,
			String publicId, String order, String orderCode, String customerCode, String startDate, String endDate) {

		LocalDateTime lStartDate = DateUtil.stringToLocalDateTime(startDate);
		LocalDateTime lEndDate = DateUtil.stringToLocalDateTime(endDate);

		headers.add("UNICA-ServiceId", uServiceId);
		headers.add("UNICA-PID", uPid);

		if (originSystem.isEmpty()) {
			if (!code.isEmpty()) {
				return getOrderBySaleCode(code, lStartDate, lEndDate);
			}

			if (!order.isEmpty()) {
				return getOrderByOrderCode(order, lStartDate, lEndDate);
			}
		} else {
			if (originSystem.equals(Constants.SOURCE_ORDERS_ATIS)) {
				return getOrderAtis(publicId, lStartDate, lEndDate);
			} else {
				return getOrderCms(publicId, orderCode, customerCode, lStartDate, lEndDate);
			}
		}
		return null;
	}

	private ResponseEntity<Object> getOrderAtis(String publicId, LocalDateTime startDate, LocalDateTime endDate) {
		List<Order> orders = new ArrayList<>();

		try {
			orders = orderRepository.getOrdersByPhone(publicId, startDate, endDate);

			return evaluateOrders(orders, publicId);
		} catch (Exception e) {
			return setInternalError(e.getLocalizedMessage());
		}
	}

	private ResponseEntity<Object> getOrderCms(String publicId, String orderCode, String customerCode,
			LocalDateTime startDate, LocalDateTime endDate) {

		return null;
	}

	private ResponseEntity<Object> getOrderBySaleCode(String code, LocalDateTime startDate, LocalDateTime endDate) {
		List<Order> orders = new ArrayList<>();

		try {
			orders = orderRepository.getOrdersBySaleCode(code, startDate, endDate);

			return evaluateOrders(orders, code);
		} catch (Exception e) {
			return setInternalError(e.getLocalizedMessage());
		}
	}

	private ResponseEntity<Object> getOrderByOrderCode(String order, LocalDateTime startDate, LocalDateTime endDate) {
		List<Order> orders = new ArrayList<>();

		try {
			orders = orderRepository.getOrdersByAtisCode(order, startDate, endDate);

			return evaluateOrders(orders, order);
		} catch (Exception e) {
			return setInternalError(e.getLocalizedMessage());
		}
	}

	private ResponseEntity<Object> evaluateOrders(List<Order> orders, String filterCode) {
		HttpStatus status;
		Object response;

		log.info("Orders - " + orders.size() + ": " + orders.toString());

		if (orders != null && orders.size() > 0) {
			status = HttpStatus.OK;
			response = new OrderResponse().fromOrderList(orders);
		} else {
			status = HttpStatus.NOT_FOUND;
			response = new ErrorResponse("SVC1006",
					String.format("Resource %1$s does not exist %1$s Resource Identifier", filterCode),
					"Reference to a resource identifier which does not exist in the collection/repository referred (e.g.: invalid Id)",
					"Not existing Resource Id");
		}

		return new ResponseEntity<Object>(response, headers, status);
	}

	private ResponseEntity<Object> setInternalError(String message) {
		return new ResponseEntity<Object>(
				new ErrorResponse("SVR1000", String.format("Generic Server Error: %1$s - Details", message),
						"There was a problem in the Service Providers network that prevented to carry out the request",
						"Generic Server Fault"),
				headers, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
