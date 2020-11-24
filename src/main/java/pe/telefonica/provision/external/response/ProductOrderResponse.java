package pe.telefonica.provision.external.response;

import lombok.Getter;
import lombok.Setter;
import pe.telefonica.provision.model.order.Order;
import pe.telefonica.provision.util.DateUtil;
import pe.telefonica.provision.util.constants.Constants;

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

	public Order fromThis(String publicId) {
		Order order = new Order();
		order.setCommercialOp(productOrderType.getProductOrderType());
		order.setRegisterOrderDate(
				DateUtil.stringToLocalDateTime(productOrderType.getOrderDate(), Constants.TIMESTAMP_FORMAT_CMS_ATIS));
		order.setCmsRequest(productOrderType.getId());
		order.setServiceCode(publicId);
		order.setStatusOrderCode(productOrderType.getStatus());
		order.setStatusOrderDescription(Constants.ATIS_CMS_STATUS.get(productOrderType.getStatus()));
		return order;
	}
}
