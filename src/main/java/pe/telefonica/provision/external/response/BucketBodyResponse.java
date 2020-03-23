package pe.telefonica.provision.external.response;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author jpaneyra
 *
 */
public class BucketBodyResponse {

	@JsonProperty("header")
	private String header;
	@JsonProperty("content")
	private boolean content;
	// private ContentBean content;
	// private ArrayList<ContentBean> content;

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public boolean isContent() {
		return content;
	}

	public void setContent(boolean content) {
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
