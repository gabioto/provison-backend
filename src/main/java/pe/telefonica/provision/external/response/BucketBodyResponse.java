package pe.telefonica.provision.external.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author jpaneyra
 *
 */
public class BucketBodyResponse {

	@JsonProperty("header")
	private String header;
	@JsonProperty("content")
	private HashMap<String, List<OrigenBean>> content;
	// private ContentBean content;
	// private ArrayList<ContentBean> content;

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public HashMap<String, List<OrigenBean>> getContent() {
		return content;
	}

	public void setContent(HashMap<String, List<OrigenBean>> content) {
		this.content = content;
	}

	public class OrigenBean {

		@JsonProperty("product")
		private String product;
		@JsonProperty("buckets")
		private ArrayList<String> buckets;

		public String getProduct() {
			return product;
		}

		public void setProduct(String product) {
			this.product = product;
		}

		public ArrayList<String> getBuckets() {
			return buckets;
		}

		public void setBuckets(ArrayList<String> buckets) {
			this.buckets = buckets;
		}

	}

}
