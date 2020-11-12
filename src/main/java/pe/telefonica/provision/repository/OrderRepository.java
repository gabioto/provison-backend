package pe.telefonica.provision.repository;

import org.springframework.data.mongodb.core.query.Update;

import pe.telefonica.provision.model.order.Order;

public interface OrderRepository {

	Order saveOrder(Order order);
	
	void updateOrder(String idOrder, Update update);
	
	Order getOrderByAtisCode(String atisCode);
	
	Order getOrderBySaleCode(String saleCode);
}
