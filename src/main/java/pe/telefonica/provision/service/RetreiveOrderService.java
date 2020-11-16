package pe.telefonica.provision.service;

import org.springframework.http.ResponseEntity;

public interface RetreiveOrderService {

	ResponseEntity<Object> getOrder(String uServiceId, String uPid, String code, String originSystem, String publicId,
			String order, String orderCode, String customerCode, String startDate, String endDate);
}
