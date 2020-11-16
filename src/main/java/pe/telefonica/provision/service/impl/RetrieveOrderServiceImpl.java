package pe.telefonica.provision.service.impl;

import java.time.LocalDateTime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

		return null;
	}

	private ResponseEntity<Object> getOrderByOrderCode(String order, LocalDateTime startDate, LocalDateTime endDate) {

		return null;
	}
}
