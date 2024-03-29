package pe.telefonica.provision.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.core.query.Update;

import pe.telefonica.provision.model.order.Order;

public interface OrderRepository {

	Order saveOrder(Order order);

	void updateOrder(String idOrder, Update update);

	Order getOrderByAtisCode(String atisCode);

	Order getOrderBySaleCode(String saleCode);

	Order getOrdersByAtisCode(String atisCode, LocalDateTime startDate, LocalDateTime endDate);

	Order getOrdersByCmsCode(String cmsCode, LocalDateTime startDate, LocalDateTime endDate);

	Order getOrdersBySaleCode(String saleCode, LocalDateTime startDate, LocalDateTime endDate);

	List<Order> getOrdersByPhone(String publicId, LocalDateTime startDate, LocalDateTime endDate);
	
	void updateFlagDateNotify(List<Order> order);
	
	List<Order> getOrdersToNotify();
}
