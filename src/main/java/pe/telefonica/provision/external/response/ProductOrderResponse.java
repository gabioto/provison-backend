package pe.telefonica.provision.external.response;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pe.telefonica.provision.model.order.Order;
import pe.telefonica.provision.util.DateUtil;
import pe.telefonica.provision.util.constants.Constants;

@Getter
@Setter
public class ProductOrderResponse {

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
	private List<RelatedParty> relatedParty = new ArrayList<RelatedParty>();
	private List<Channel> channel = new ArrayList<Channel>();

	@Getter
	@Setter
	@AllArgsConstructor
	public static class RelatedParty {
		private String id;
		private String href;
		
		public RelatedParty() {			
		}
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class Channel {
		private String id;
		private String href;
		
		public Channel() {
		}
	}

	public Order fromThis(String publicId) {
		Order order = new Order();
		order.setCommercialOp(productOrderType);
		order.setRegisterOrderDate(DateUtil.stringToLocalDateTime(orderDate, Constants.TIMESTAMP_FORMAT_PSI));
		order.setCmsRequest(id);
		order.setServiceCode(publicId);
		order.setStatusOrderCode(status);
		order.setStatusOrderDescription(Constants.ATIS_CMS_STATUS.get(status));
		return order;
	}
}
