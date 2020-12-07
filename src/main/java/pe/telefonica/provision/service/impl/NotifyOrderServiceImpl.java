package pe.telefonica.provision.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import pe.telefonica.provision.controller.response.ErrorResponse;
import pe.telefonica.provision.controller.response.order.OrderNotificationResponse;
import pe.telefonica.provision.model.order.Order;
import pe.telefonica.provision.repository.OrderRepository;
import pe.telefonica.provision.service.NotifyOrderService;

@Service
public class NotifyOrderServiceImpl implements NotifyOrderService {

	private static final Log log = LogFactory.getLog(NotifyOrderServiceImpl.class);

	@Autowired
	OrderRepository orderRepository;

	private MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();

	@Override
	public ResponseEntity<Object> getOrders(String uServiceId, String uPid) {

		headers.clear();
		headers.add("UNICA-ServiceId", uServiceId);
		headers.add("UNICA-PID", uPid);

		List<Order> orders = orderRepository.getOrdersToNotify();

		orders = orders.stream().filter(order -> {
			if (order.getNote1() != null) {
				if (order.getCommercialOp().equals("SUSPENSION APC")
						&& !order.getNote1().toUpperCase().contains("BAJA")) {
					return true;
				}
			}

			return false;
		}).collect(Collectors.toList());

		return evaluateOrders(orders);
	}

	private ResponseEntity<Object> evaluateOrders(List<Order> orders) {

		HttpStatus status;
		Object response;

		log.info("Orders - " + (orders != null ? orders.toString() : "null"));

		if (orders != null && orders.size() > 0) {
			status = HttpStatus.OK;
			response = new OrderNotificationResponse().fromOrderList(orders);
		} else {
			status = HttpStatus.NOT_FOUND;
			response = new ErrorResponse("SVC1006", String.format("Resources does not exist"),
					"Reference to a resource identifier which does not exist in the collection/repository referred (e.g.: invalid Id)",
					"Not existing Resource Id");
		}

		return new ResponseEntity<Object>(response, headers, status);
	}

}
