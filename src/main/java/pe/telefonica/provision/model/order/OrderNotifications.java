package pe.telefonica.provision.model.order;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderNotifications {

	private Boolean finalizadoSendNotify = false;

	private LocalDateTime finalizadoSendDate;
}
