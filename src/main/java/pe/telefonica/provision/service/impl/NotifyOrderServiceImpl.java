package pe.telefonica.provision.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pe.telefonica.provision.model.order.Order;
import pe.telefonica.provision.repository.OrderRepository;
import pe.telefonica.provision.service.NotifyOrderService;

@Service
public class NotifyOrderServiceImpl implements NotifyOrderService {

	@Autowired
	OrderRepository orderRepository;

	@Override
	public ResponseEntity<Object> getOrders(String uServiceId, String uPid) {

		List<Order> orders = orderRepository.getOrdersToNotify();
		return null;
	}

}
