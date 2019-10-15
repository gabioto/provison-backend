package pe.telefonica.provision.controller.response;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProvisionArrayResponse<T> {

	@Field("header")
	private ProvisionHeaderResponse header;
	
	@JsonProperty("content")
	private List<T> data;
	
	public List<T> getData() {
		return data;
	}
	public ProvisionArrayResponse<T> setData(List<T> data) {
		this.data = data;
		return this;
	}
	public ProvisionHeaderResponse getHeader() {
		return header;
	}
	public ProvisionArrayResponse<T> setHeader(ProvisionHeaderResponse header) {
		this.header = header;
		return this;
	}
}
