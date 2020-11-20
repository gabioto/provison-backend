package pe.telefonica.provision.external.response;

import lombok.Getter;
import lombok.Setter;
import pe.telefonica.provision.model.order.Order;

@Getter
@Setter
public class ProductOrderResponse {

	private ProductOrderType productOrderType;

	@Getter
	@Setter
	public class ProductOrderType {
		private String id;
		private String href;
		private String correlationId;
		private String description;
		private String productOrderType;
		private String orderDate;
		private String completionDate;
		private String status;
		private String statusReason;
		private String statusChangeDate;
		private String source;
		private RelatedParty relatedParty;
		private Channel channel;

	}

	@Getter
	@Setter
	public class RelatedParty {
		private String id;
		private String href;
	}

	@Getter
	@Setter
	public class Channel {
		private String id;
		private String href;
	}
	
	public Order fromThis() {
		return new Order();
	}
}
