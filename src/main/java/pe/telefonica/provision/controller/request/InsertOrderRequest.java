package pe.telefonica.provision.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InsertOrderRequest {

	private String data;
	private String dataOrigin;
	private String status;
}
