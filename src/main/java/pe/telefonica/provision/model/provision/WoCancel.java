package pe.telefonica.provision.model.provision;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.data.mongodb.core.mapping.Field;

public class WoCancel implements Serializable {

	private static final long serialVersionUID = 3775714898258466530L;

	@Field("xa_cancel_reason")
	private String xaCancelReason;

	@Field("user_cancel")
	private String userCancel;

	@Field("register_date")
	private LocalDateTime registerDate = LocalDateTime.now(ZoneOffset.of("-05:00"));

	public String getXaCancelReason() {
		return xaCancelReason;
	}

	public void setXaCancelReason(String xaCancelReason) {
		this.xaCancelReason = xaCancelReason;
	}

	public String getUserCancel() {
		return userCancel;
	}

	public void setUserCancel(String userCancel) {
		this.userCancel = userCancel;
	}

	public LocalDateTime getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(LocalDateTime registerDate) {
		this.registerDate = registerDate;
	}

}
