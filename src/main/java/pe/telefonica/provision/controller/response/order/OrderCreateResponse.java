package pe.telefonica.provision.controller.response.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrderCreateResponse {

	private String source;

	private String code;

	private boolean success;
}
