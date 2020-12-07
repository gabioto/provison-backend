package pe.telefonica.provision.service;

import org.springframework.http.ResponseEntity;

public interface NotifyOrderService {

	ResponseEntity<Object> getOrders(String uServiceId, String uPid);
}
