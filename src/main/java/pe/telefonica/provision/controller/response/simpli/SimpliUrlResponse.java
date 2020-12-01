package pe.telefonica.provision.controller.response.simpli;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SimpliUrlResponse {
	private Header header;
	private Body body;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode(callSuper = false)
	public static class Header {
		private String UNICA_ServiceId;
		private String UNICA_PID;

	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode(callSuper = false)
	public static class Body {
		private String APPT_Numer;
		private String status;

	}
}
