package pe.telefonica.provision.api;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProvisionResponse<T> {

	@Field("header")
	private ProvisionHeaderResponse header;
	
	@JsonProperty("content")
	private List<T> data;
	
	public List<T> getData() {
		return data;
	}
	public ProvisionResponse<T> setData(List<T> data) {
		this.data = data;
		return this;
	}
	public ProvisionHeaderResponse getHeader() {
		return header;
	}
	public ProvisionResponse<T> setHeader(ProvisionHeaderResponse header) {
		this.header = header;
		return this;
	}
}
