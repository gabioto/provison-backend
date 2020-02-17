package pe.telefonica.provision.model.provision;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.data.mongodb.core.mapping.Field;

public class WoReshedule implements Serializable {

	private static final long serialVersionUID = 3775714898258466530L;

	@Field("xa_appointment_scheduler")
	private String xaAppointmentScheduler;

	@Field("time_slot")
	private String timeSlot;

	@Field("register_date")
	private LocalDateTime registerDate = LocalDateTime.now(ZoneOffset.of("-05:00"));

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	public void setTimeSlot(String timeSlot) {
		this.timeSlot = timeSlot;
	}

	public String getXaAppointmentScheduler() {
		return xaAppointmentScheduler;
	}

	public void setXaAppointmentScheduler(String xaAppointmentScheduler) {
		this.xaAppointmentScheduler = xaAppointmentScheduler;
	}

	public LocalDateTime getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(LocalDateTime registerDate) {
		this.registerDate = registerDate;
	}

}
