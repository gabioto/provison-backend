package pe.telefonica.provision.model.order;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "collOrder")
@JsonPropertyOrder({ "idOrder" })
public class Order {

	@Id
	@Field("_id")
	private String idOrder;

	private String source;

	private String code;

	private String serviceCode;

	private String phone;

	private String documentType;

	private String documentNumber;

	private LocalDateTime registerDate;

	private LocalDateTime execSuspDate;

	private LocalDateTime execRecoxDate;

	private String note1;

	private String application;

	private String commercialOp;

	private String contactPhone;

	private String contactCellphone;

	private String contactMail;

	private String errorCode;

	private String errorDescription;

	private LocalDateTime receptionDate;

	private String status;

	private String atisOrder;

	private String cmsRequest;

	private String statusOrderCode;

	private String statusOrderDescription;

	private LocalDateTime registerOrderDate;

	private LocalDateTime releaseOrderDate;

	private String note2;

	private String idResult;

	private LocalDateTime registerLocalDate = LocalDateTime.now(ZoneOffset.of("-05:00"));
}
