package pe.telefonica.provision.model;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Field;

public class UpFront {

	@Field("cip")
	private String cip;

	@Field("currency")
	private String currency;

	@Field("amount")
	private Double amount;

	@Field("exp_date")
	private LocalDateTime expDate;

	@Field("cip_url")
	private String cipUrl;

	@Field("status")
	private String status;

	@Field("payment_date")
	private LocalDateTime paymentDate;

	public String getCip() {
		return cip;
	}

	public void setCip(String cip) {
		this.cip = cip;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getCipUrl() {
		return cipUrl;
	}

	public void setCipUrl(String cipUrl) {
		this.cipUrl = cipUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getExpDate() {
		return expDate;
	}

	public void setExpDate(LocalDateTime expDate) {
		this.expDate = expDate;
	}

	public LocalDateTime getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(LocalDateTime paymentDate) {
		this.paymentDate = paymentDate;
	}

	@Override
	public String toString() {
		return "UpFront [cip=" + cip + ", currency=" + currency + ", amount=" + amount + ", expDate=" + expDate
				+ ", cipUrl=" + cipUrl + ", status=" + status + ", paymentDate=" + paymentDate + "]";
	}

}
