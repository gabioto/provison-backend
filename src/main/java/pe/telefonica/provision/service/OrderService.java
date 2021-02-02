package pe.telefonica.provision.service;

import org.springframework.http.ResponseEntity;

public interface OrderService {

	ResponseEntity<Object> createOrder(String data);
}
