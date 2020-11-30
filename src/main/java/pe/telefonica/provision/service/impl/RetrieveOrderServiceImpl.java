package pe.telefonica.provision.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import pe.telefonica.provision.controller.response.ErrorResponse;
import pe.telefonica.provision.controller.response.order.OrderResponse;
import pe.telefonica.provision.external.ProductOrdersApi;
import pe.telefonica.provision.model.order.Order;
import pe.telefonica.provision.repository.OrderRepository;
import pe.telefonica.provision.service.RetreiveOrderService;
import pe.telefonica.provision.util.DateUtil;
import pe.telefonica.provision.util.StringUtil;
import pe.telefonica.provision.util.constants.Constants;

@Service
public class RetrieveOrderServiceImpl implements RetreiveOrderService {

	private static final Log log = LogFactory.getLog(OrderServiceImpl.class);

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductOrdersApi productOrdersApi;

	private MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();

	@Override
	public ResponseEntity<Object> getOrder(String uServiceId, String uPid, String code, String originSystem,
			String publicId, String order, String orderCode, String customerCode, String startDate, String endDate) {

		headers.clear();
		headers.add("UNICA-ServiceId", uServiceId);
		headers.add("UNICA-PID", uPid);

		LocalDateTime lStartDate = DateUtil.stringToLocalDateTime(startDate,
				Constants.TIMESTAMP_FORMAT_CMS_ATIS_NO_ZONE);
		LocalDateTime lEndDate = DateUtil.stringToLocalDateTime(endDate, Constants.TIMESTAMP_FORMAT_CMS_ATIS_NO_ZONE);
		ResponseEntity<Object> validateResponse = validateDates(lStartDate, lEndDate,
				(!startDate.isEmpty() || !startDate.isEmpty()));

		if (validateResponse != null) {
			return validateResponse;
		}

		if (originSystem.isEmpty()) {
			if (!code.isEmpty()) {
				return getOrderBySaleCode(code, lStartDate, lEndDate);
			}

			if (!order.isEmpty()) {
				return getOrderByOrderCode(order, lStartDate, lEndDate);
			}
		} else {
			if (originSystem.equals(Constants.SOURCE_ORDERS_ATIS)) {
				return getOrderAtis(publicId, order, lStartDate, lEndDate);
			} else {
				return getOrderCms(publicId, order, orderCode, customerCode, lStartDate, lEndDate);
			}
		}

		return new ResponseEntity<Object>(
				new ErrorResponse("SVC0001", "Generic Client Error: Empty query params",
						"API Generic wildcard fault response", "Generic Client Error"),
				headers, HttpStatus.BAD_REQUEST);
	}

	private ResponseEntity<Object> getOrderAtis(String publicId, String orderAtis, LocalDateTime startDate,
			LocalDateTime endDate) {

		boolean filterByOrder = false;
		List<Order> orders = new ArrayList<>();
		Order order = null;

		try {
			if (orderAtis.isEmpty()) {
				orders = orderRepository.getOrdersByPhone(publicId, startDate, endDate);

				if (orders.size() > 0) {
					order = getLastOrder(orders);
				}

			} else {
				order = orderRepository.getOrdersByAtisCode(orderAtis, startDate, endDate);
				filterByOrder = true;
			}

			return evaluateOrders(order, filterByOrder ? orderAtis : publicId);
		} catch (Exception e) {
			return setInternalError(e.getLocalizedMessage());
		}
	}

	private ResponseEntity<Object> getOrderCms(String publicId, String id, String orderCode, String customerCode,
			LocalDateTime startDate, LocalDateTime endDate) {

		try {
			List<Order> orders = productOrdersApi.getProductOrders(publicId, id, orderCode, customerCode);

			if (orders != null && orders.size() > 0) {
				if (!id.isEmpty()) {
					saveCmsOrder(orders.get(0), startDate, endDate);

					return evaluateOrders(orders.get(0), id);
				} else {
					for (Order order : orders) {
						saveCmsOrder(order, startDate, endDate);
					}

					Order returnedOrder = getLastOrder(orders);

					return evaluateOrders(returnedOrder, publicId);
				}
			} else {
				return evaluateOrders(null,
						id != null ? id
								: String.format("publicId: %1$s, codCliente: %2$s, codCuenta: %3$s", publicId,
										customerCode, orderCode));
			}

		} catch (Exception e) {
			return setInternalError(e.getLocalizedMessage());
		}
	}

	private ResponseEntity<Object> getOrderBySaleCode(String code, LocalDateTime startDate, LocalDateTime endDate) {

		try {
			Order order = orderRepository.getOrdersBySaleCode(code, startDate, endDate);

			return evaluateOrders(order, code);
		} catch (Exception e) {
			return setInternalError(e.getLocalizedMessage());
		}
	}

	private ResponseEntity<Object> getOrderByOrderCode(String order, LocalDateTime startDate, LocalDateTime endDate) {

		try {
			Order lOrder = orderRepository.getOrdersByAtisCode(order, startDate, endDate);

			return evaluateOrders(lOrder, order);
		} catch (Exception e) {
			return setInternalError(e.getLocalizedMessage());
		}
	}

	private void saveCmsOrder(Order order, LocalDateTime startDate, LocalDateTime endDate) {
		Order lOrder = orderRepository.getOrdersByCmsCode(order.getCmsRequest(), startDate, endDate);

		if (lOrder != null) {
			orderRepository.updateOrder(lOrder.getIdOrder(), updateOrderFields(order, lOrder));
		} else {
			orderRepository.saveOrder(order);
		}
	}

	private Order getLastOrder(List<Order> orders) {

		List<Order> orderDates = orders.stream().map(order -> {
			if (order.getRegisterDate() != null) {
				order.setAuxDate(order.getRegisterDate());
			} else if (order.getRegisterOrderDate() != null) {
				order.setAuxDate(order.getRegisterOrderDate());
			} else {
				order.setAuxDate(order.getRegisterLocalDate());
			}

			return order;
		}).collect(Collectors.toList());

		Comparator<Order> comparator = Comparator.comparing(Order::getAuxDate);

		Order maxDatedOrder = orderDates.stream().filter(emp -> emp.getAuxDate() != null).max(comparator).get();

		return maxDatedOrder;
	}

	private ResponseEntity<Object> validateDates(LocalDateTime startDate, LocalDateTime endDate, boolean haveValue) {
		if (startDate != null && endDate != null) {
			if (endDate.isBefore(startDate)) {
				return setBadRequestElement("fechaRegistroInicio/fechaRegistroFin");
			}
		} else if (haveValue) {
			return setBadRequestElement("fechaRegistroInicio/fechaRegistroFin");
		}

		return null;
	}

	private Update updateOrderFields(Order order, Order orderSaved) {

		Update update = new Update();
		update.set("source", Constants.SOURCE_ORDERS_CMS);
		update.set("commercialOp", StringUtil.getValue(order.getCommercialOp(), orderSaved.getCommercialOp()));
		update.set("registerOrderDate", order.getRegisterOrderDate() != null ? order.getRegisterOrderDate()
				: orderSaved.getRegisterOrderDate());
		update.set("cmsRequest", StringUtil.getValue(order.getCmsRequest(), orderSaved.getCmsRequest()));
		update.set("serviceCode", StringUtil.getValue(order.getServiceCode(), orderSaved.getServiceCode()));
		update.set("statusOrderDescription",
				StringUtil.getValue(order.getStatusOrderDescription(), orderSaved.getStatusOrderDescription()));
		update.set("statusOrderCode", StringUtil.getValue(order.getStatusOrderCode(), orderSaved.getStatusOrderCode()));
		update.set("lastUpdateDate", LocalDateTime.now(ZoneOffset.of(Constants.TIME_ZONE_LOCALE)));
		return update;
	}

	private ResponseEntity<Object> evaluateOrders(Order order, String filterCode) {

		HttpStatus status;
		Object response;

		log.info("Orders - " + (order != null ? order.toString() : "null"));

		if (order != null) {
			status = HttpStatus.OK;
			response = new OrderResponse().fromOrder(order);
		} else {
			status = HttpStatus.NOT_FOUND;
			response = new ErrorResponse("SVC1006", String.format("Resource %1$s does not exist", filterCode),
					"Reference to a resource identifier which does not exist in the collection/repository referred (e.g.: invalid Id)",
					"Not existing Resource Id");
		}

		return new ResponseEntity<Object>(response, headers, status);
	}

	private ResponseEntity<Object> setBadRequestElement(String message) {

		return new ResponseEntity<Object>(new ErrorResponse("SVC1001",
				String.format("Invalid parameter: %1$s", message),
				"API Request with an element not conforming to Swagger definitions or to a list of allowed Query Parameters.",
				"Invalid parameter"), headers, HttpStatus.BAD_REQUEST);
	}

	private ResponseEntity<Object> setInternalError(String message) {

		return new ResponseEntity<Object>(
				new ErrorResponse("SVR1000", String.format("Generic Server Error: %1$s", message),
						"There was a problem in the Service Providers network that prevented to carry out the request",
						"Generic Server Fault"),
				headers, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
