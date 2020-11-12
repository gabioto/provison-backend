package pe.telefonica.provision.service;

import org.springframework.http.ResponseEntity;

import pe.telefonica.provision.controller.request.order.OrderRequest;

public interface OrderService {

	ResponseEntity<Object> createOrder(OrderRequest request);
}
