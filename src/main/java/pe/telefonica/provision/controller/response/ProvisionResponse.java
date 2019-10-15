package pe.telefonica.provision.controller.response;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProvisionResponse<T> {

	@Field("header")
	private ProvisionHeaderResponse header;
	
	@JsonProperty("content")
	private T data;
	
	public T getData() {
		return data;
	}
	public ProvisionResponse<T> setData(T data) {
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
