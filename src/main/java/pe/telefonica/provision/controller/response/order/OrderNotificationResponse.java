package pe.telefonica.provision.controller.response.order;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.telefonica.provision.model.order.Order;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderNotificationResponse {

	private String contactPhone;

	private String contactMail;

	private String commercialOpAtis;

	private String flagMT;

	public OrderNotificationResponse fromOrder(Order order) {

		contactPhone = order.getContactCellphone() != null ? order.getContactCellphone() : order.getContactPhone();
		contactMail = order.getContactMail();
		commercialOpAtis = order.getCommercialOpAtis();
		flagMT = order.getFlagMT();

		return this;
	}

	public List<OrderNotificationResponse> fromOrderList(List<Order> orderList) {
		return orderList.parallelStream().map(order -> {
			return fromOrder(order);
		}).collect(Collectors.toList());
	}
}
