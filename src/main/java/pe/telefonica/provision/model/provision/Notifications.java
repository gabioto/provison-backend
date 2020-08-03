package pe.telefonica.provision.model.provision;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Field;

public class Notifications implements Serializable {
	
	private static final long serialVersionUID = 5094729030347835498L;
	@Field("caida_send_notify")
	private Boolean caidaSendNotify = false;

	@Field("caida_send_date")
	private LocalDateTime caidaSendDate;

	@Field("pagado_send_notify")
	private Boolean pagadoSendNotify = false;

	@Field("pagado_send_date")
	private LocalDateTime pagadoSendDate;

	@Field("into_send_notify")
	private Boolean intoaSendNotify = false;

	@Field("into_send_date")
	private LocalDateTime intoaSendDate;

	@Field("prestart_send_notify")
	private Boolean prestartSendNotify = false;

	@Field("prestart_send_date")
	private LocalDateTime prestartSendDate;

	@Field("notdone_send_notify")
	private Boolean notdoneSendNotify = false;

	@Field("notdone_send_date")
	private LocalDateTime notdoneSendDate;

	@Field("completed_send_notify")
	private Boolean completedSendNotify = false;

	@Field("completed_send_date")
	private LocalDateTime completedSendDate;
	
	@Field("cancel_send_notify")
	private Boolean cancelSendNotify = false;

	@Field("cancel_send_date")
	private LocalDateTime cancelSendDate;
	

	public Boolean getCaidaSendNotify() {
		return caidaSendNotify;
	}

	public LocalDateTime getCaidaSendDate() {
		return caidaSendDate;
	}

	public Boolean getPagadoSendNotify() {
		return pagadoSendNotify;
	}

	public LocalDateTime getPagadoSendDate() {
		return pagadoSendDate;
	}

	public void setCaidaSendNotify(Boolean caidaSendNotify) {
		this.caidaSendNotify = caidaSendNotify;
	}

	public void setCaidaSendDate(LocalDateTime caidaSendDate) {
		this.caidaSendDate = caidaSendDate;
	}

	public void setPagadoSendNotify(Boolean pagadoSendNotify) {
		this.pagadoSendNotify = pagadoSendNotify;
	}

	public void setPagadoSendDate(LocalDateTime pagadoSendDate) {
		this.pagadoSendDate = pagadoSendDate;
	}

	public Boolean getIntoaSendNotify() {
		return intoaSendNotify;
	}

	public LocalDateTime getIntoaSendDate() {
		return intoaSendDate;
	}

	public Boolean getPrestartSendNotify() {
		return prestartSendNotify;
	}

	public LocalDateTime getPrestartSendDate() {
		return prestartSendDate;
	}

	public Boolean getNotdoneSendNotify() {
		return notdoneSendNotify;
	}

	public LocalDateTime getNotdoneSendDate() {
		return notdoneSendDate;
	}

	public Boolean getCompletedSendNotify() {
		return completedSendNotify;
	}

	public LocalDateTime getCompletedSendDate() {
		return completedSendDate;
	}

	public void setIntoaSendNotify(Boolean intoaSendNotify) {
		this.intoaSendNotify = intoaSendNotify;
	}

	public void setIntoaSendDate(LocalDateTime intoaSendDate) {
		this.intoaSendDate = intoaSendDate;
	}

	public void setPrestartSendNotify(Boolean prestartSendNotify) {
		this.prestartSendNotify = prestartSendNotify;
	}

	public void setPrestartSendDate(LocalDateTime prestartSendDate) {
		this.prestartSendDate = prestartSendDate;
	}

	public void setNotdoneSendNotify(Boolean notdoneSendNotify) {
		this.notdoneSendNotify = notdoneSendNotify;
	}

	public void setNotdoneSendDate(LocalDateTime notdoneSendDate) {
		this.notdoneSendDate = notdoneSendDate;
	}

	public void setCompletedSendNotify(Boolean completedSendNotify) {
		this.completedSendNotify = completedSendNotify;
	}

	public void setCompletedSendDate(LocalDateTime completedSendDate) {
		this.completedSendDate = completedSendDate;
	}

	public Boolean getCancelSendNotify() {
		return cancelSendNotify;
	}	

	public LocalDateTime getCancelSendDate() {
		return cancelSendDate;
	}

	public void setCancelSendNotify(Boolean cancelSendNotify) {
		this.cancelSendNotify = cancelSendNotify;
	}

	public void setCancelSendDate(LocalDateTime cancelSendDate) {
		this.cancelSendDate = cancelSendDate;
	}
	
	

}
