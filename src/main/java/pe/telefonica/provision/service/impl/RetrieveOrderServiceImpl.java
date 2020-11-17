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

import pe.telefonica.provision.controller.response.order.OrderResponse;
import pe.telefonica.provision.model.order.Order;
import pe.telefonica.provision.repository.OrderRepository;
import pe.telefonica.provision.service.RetreiveOrderService;
import pe.telefonica.provision.util.DateUtil;

@Service
public class RetrieveOrderServiceImpl implements RetreiveOrderService {

	private static final Log log = LogFactory.getLog(OrderServiceImpl.class);

	@Autowired
	private OrderRepository orderRepository;

	@Override
	public ResponseEntity<Object> getOrder(String uServiceId, String uPid, String code, String originSystem,
			String publicId, String order, String orderCode, String customerCode, String startDate, String endDate) {

		LocalDateTime lStartDate = DateUtil.stringToLocalDateTime(startDate);
		LocalDateTime lEndDate = DateUtil.stringToLocalDateTime(endDate);

		if (originSystem.isEmpty()) {
			if (!code.isEmpty()) {
				return getOrderBySaleCode(code, lStartDate, lEndDate);
			}

			if (!order.isEmpty()) {
				return getOrderByOrderCode(order, lStartDate, lEndDate);
			}
		} else {

		}
		return null;
	}

	private ResponseEntity<Object> getOrderAtis() {

		return null;
	}

	private ResponseEntity<Object> getOrderCms() {

		return null;
	}

	private ResponseEntity<Object> getOrderBySaleCode(String code, LocalDateTime startDate, LocalDateTime endDate) {
		List<Order> orders = new ArrayList<>();

		try {
			orders = orderRepository.getOrdersBySaleCode(code, startDate, endDate);

		} catch (Exception e) {
			// TODO: handle exception
		}

		return new ResponseEntity<Object>(orders, HttpStatus.OK);
	}

	private ResponseEntity<Object> getOrderByOrderCode(String order, LocalDateTime startDate, LocalDateTime endDate) {
		try {
			List<Order> orders = orderRepository.getOrdersByAtisCode(order, startDate, endDate);
		} catch (Exception e) {
			// TODO: handle exception
		}

		return null;
	}
}
