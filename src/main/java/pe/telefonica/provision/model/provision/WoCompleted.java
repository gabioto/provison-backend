package pe.telefonica.provision.model.provision;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class WoCompleted implements Serializable {
	private static final long serialVersionUID = 3775714898258466530L;

	@Field("xa_creation_date")
	private String xaCreationDate;

	@Field("eta_start_time")
	private String etaStartTime;

	@Field("eta_end_time")
	private String etaEndTime;

	@Field("date")
	private String date;

	@Field("xa_note")
	private String xaNote;

	@Field("source_system")
	private String sourceSystem;

	@Field("network_change")
	private String networkChange;

	@Field("observation")
	private String observation;

	@Field("receive_person_name")
	private String receivePersonName;

	@Field("receive_person_id")
	private String receivePersonId;

	@Field("relationship")
	private String relationship;

	@Field("complete_cause_rep_stb")
	private String completeCauseRepStb;

	@Field("complete_rep_stb")
	private String completeRepStb;

	@Field("complete_cause_rep_adsl")
	private String completeCauseRepAdsl;

	@Field("complete_rep_adsl")
	private String completeRepAdsl;

	@Field("complete_cause_rep_sat")
	private String completeCauseRepSat;

	@Field("complete_rep_sat")
	private String completeRepSat;

	@Field("complete_cause_rep_cbl")
	private String completeCauseRepCbl;

	@Field("complete_rep_cbl")
	private String completeRepCbl;

	public String getXaCreationDate() {
		return xaCreationDate;
	}

	public void setXaCreationDate(String xaCreationDate) {
		this.xaCreationDate = xaCreationDate;
	}

	public String getEtaStartTime() {
		return etaStartTime;
	}

	public void setEtaStartTime(String etaStartTime) {
		this.etaStartTime = etaStartTime;
	}

	public String getEtaEndTime() {
		return etaEndTime;
	}

	public void setEtaEndTime(String etaEndTime) {
		this.etaEndTime = etaEndTime;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getXaNote() {
		return xaNote;
	}

	public void setXaNote(String xaNote) {
		this.xaNote = xaNote;
	}

	public String getSourceSystem() {
		return sourceSystem;
	}

	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}

	public String getNetworkChange() {
		return networkChange;
	}

	public void setNetworkChange(String networkChange) {
		this.networkChange = networkChange;
	}

	public String getObservation() {
		return observation;
	}

	public void setObservation(String observation) {
		this.observation = observation;
	}

	public String getReceivePersonName() {
		return receivePersonName;
	}

	public void setReceivePersonName(String receivePersonName) {
		this.receivePersonName = receivePersonName;
	}

	public String getReceivePersonId() {
		return receivePersonId;
	}

	public void setReceivePersonId(String receivePersonId) {
		this.receivePersonId = receivePersonId;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public String getCompleteCauseRepStb() {
		return completeCauseRepStb;
	}

	public void setCompleteCauseRepStb(String completeCauseRepStb) {
		this.completeCauseRepStb = completeCauseRepStb;
	}

	public String getCompleteRepStb() {
		return completeRepStb;
	}

	public void setCompleteRepStb(String completeRepStb) {
		this.completeRepStb = completeRepStb;
	}

	public String getCompleteCauseRepAdsl() {
		return completeCauseRepAdsl;
	}

	public void setCompleteCauseRepAdsl(String completeCauseRepAdsl) {
		this.completeCauseRepAdsl = completeCauseRepAdsl;
	}

	public String getCompleteRepAdsl() {
		return completeRepAdsl;
	}

	public void setCompleteRepAdsl(String completeRepAdsl) {
		this.completeRepAdsl = completeRepAdsl;
	}

	public String getCompleteCauseRepSat() {
		return completeCauseRepSat;
	}

	public void setCompleteCauseRepSat(String completeCauseRepSat) {
		this.completeCauseRepSat = completeCauseRepSat;
	}

	public String getCompleteRepSat() {
		return completeRepSat;
	}

	public void setCompleteRepSat(String completeRepSat) {
		this.completeRepSat = completeRepSat;
	}

	public String getCompleteCauseRepCbl() {
		return completeCauseRepCbl;
	}

	public void setCompleteCauseRepCbl(String completeCauseRepCbl) {
		this.completeCauseRepCbl = completeCauseRepCbl;
	}

	public String getCompleteRepCbl() {
		return completeRepCbl;
	}

	public void setCompleteRepCbl(String completeRepCbl) {
		this.completeRepCbl = completeRepCbl;
	}

}
