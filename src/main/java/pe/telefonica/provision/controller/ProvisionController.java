package pe.telefonica.provision.controller;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

import com.google.gson.Gson;

import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.controller.request.AddressUpdateRequest;
import pe.telefonica.provision.controller.request.ApiTrazaSetContactInfoUpdateRequest;
import pe.telefonica.provision.controller.request.CancelOrderRequest;
import pe.telefonica.provision.controller.request.ContactRequest;
import pe.telefonica.provision.controller.request.GetAllInTimeRangeRequest;
import pe.telefonica.provision.controller.request.GetCustomerByOrderCodeRequest;
import pe.telefonica.provision.controller.request.GetProvisionByOrderCodeRequest;
import pe.telefonica.provision.controller.request.InsertCodeFictionalRequest;
import pe.telefonica.provision.controller.request.InsertOrderRequest;
import pe.telefonica.provision.controller.request.KafkaTOARequest;
import pe.telefonica.provision.controller.request.ProvisionRequest;
import pe.telefonica.provision.controller.request.ReceiveAddressUpdateBORequest;
import pe.telefonica.provision.controller.request.UpdateFromToaRequest;
import pe.telefonica.provision.controller.request.ValidateDataRequest;
import pe.telefonica.provision.controller.response.GetAllInTimeRangeResponse;
import pe.telefonica.provision.controller.response.ProvisionResponse;
import pe.telefonica.provision.dto.ProvisionDetailTrazaDto;
import pe.telefonica.provision.dto.ProvisionCustomerDto;
import pe.telefonica.provision.dto.ProvisionDto;
import pe.telefonica.provision.dto.ProvisionTrazaDto;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.ProvisionScheduler;
import pe.telefonica.provision.service.OrderService;
import pe.telefonica.provision.service.ProvisionService;
import pe.telefonica.provision.service.ProvisionUpdateService.ProvisionUpdateAsisService;
import pe.telefonica.provision.service.ProvisionUpdateService.ProvisionUpdateTobeService;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.constants.ConstantsLogData;
import pe.telefonica.provision.util.constants.ErrorCode;
import pe.telefonica.provision.util.constants.InternalError;
import pe.telefonica.provision.util.exception.FunctionalErrorException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("provision")
@EnableAsync
public class ProvisionController {

	private static final Log log = LogFactory.getLog(ProvisionController.class);

	@Autowired
	private ProvisionService provisionService;

	@Autowired
	private ProvisionUpdateAsisService provisionUpdateAsisService;

	@Autowired
	private ProvisionUpdateTobeService provisionUpdateTobeService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private TrazabilidadSecurityApi restSecuritySaveLogData;

	@RequestMapping(value = "/getCustomerByDocument", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<Customer>> getCustomerByDocument(
			@RequestBody @Valid ApiRequest<ProvisionRequest> request) {

		String timestamp = "";
		ApiResponse<Customer> apiResponse;
		HttpStatus status;

		try {
			Customer customer = provisionService.validateUser(request);

			if (customer != null) {
				timestamp = getTimestamp();
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<Customer>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_USER,
						String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(customer);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_VALIDATE_USER, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());
			} else {
				timestamp = getTimestamp();
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<Customer>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_USER,
						String.valueOf(status.value()), "No se encontraron datos del cliente", null);
				apiResponse.setBody(null);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "NOT_MATCH", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_VALIDATE_USER,
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp,
						request.getBody().getActivityType(), request.getHeader().getAppName());
			}

		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			timestamp = getTimestamp();
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Customer>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_USER,
					String.valueOf(status.value()), ex.getMessage(), null);

			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_VALIDATE_USER, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());
		}

		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * @param provisionRequest
	 * @return ProvisionResponse<Provision>
	 * @description get all provisions related to type and number of the document
	 */
	@RequestMapping(value = "/getAllProvision", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<List<ProvisionTrazaDto>>> getAllProvision(
			@RequestBody ApiRequest<ProvisionRequest> request) {

		ApiResponse<List<ProvisionTrazaDto>> apiResponse;
		HttpStatus status;
		String errorInternal = "";
		String timestamp = "";

		// Validate documentType
		if (request.getBody().getDocumentType() == null || request.getBody().getDocumentType().equals("")) {

			status = HttpStatus.BAD_REQUEST;
			errorInternal = InternalError.TRZ06.toString();
			errorInternal = ErrorCode.get(Constants.GET_ORDERS + errorInternal.replace("\"", "")).toString();

			timestamp = getTimestamp();

			apiResponse = new ApiResponse<List<ProvisionTrazaDto>>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_ALL, errorInternal, "Tipo de documento obligatorio", null);
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

			return ResponseEntity.status(status).body(apiResponse);
		}

		// Validate documentNumber
		if (request.getBody().getDocumentNumber() == null || request.getBody().getDocumentNumber().equals("")) {

			status = HttpStatus.BAD_REQUEST;
			errorInternal = InternalError.TRZ06.toString();
			errorInternal = ErrorCode.get(Constants.GET_ORDERS + errorInternal.replace("\"", "")).toString();

			timestamp = getTimestamp();
			apiResponse = new ApiResponse<List<ProvisionTrazaDto>>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_ALL, errorInternal, "Numero de documento obligatorio", null);
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

			return ResponseEntity.status(status).body(apiResponse);
		}

		Boolean typedata = request.getBody().getDocumentType() instanceof String;
		if (!typedata) {
			status = HttpStatus.BAD_REQUEST;
			errorInternal = InternalError.TRZ07.toString();
			errorInternal = ErrorCode.get(Constants.GET_ORDERS + errorInternal.replace("\"", "")).toString();

			timestamp = getTimestamp();
			apiResponse = new ApiResponse<List<ProvisionTrazaDto>>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_ALL, errorInternal, "Tipo de documento debe ser cadena", null);
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
			return ResponseEntity.status(status).body(apiResponse);
		}

		try {
			List<ProvisionTrazaDto> provisions = provisionService.getAllTraza(request);

			if (provisions != null) {

				status = HttpStatus.OK;
				apiResponse = new ApiResponse<List<ProvisionTrazaDto>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()), status.getReasonPhrase(),
						null);
				apiResponse.setBody(provisions);

				timestamp = getTimestamp();
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_GET_PROVISION_ALL, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			} else {
				status = HttpStatus.NOT_FOUND;
				apiResponse = new ApiResponse<List<ProvisionTrazaDto>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()),
						"No se encontraron provisiones", null);
				apiResponse.setBody(provisions);

				timestamp = getTimestamp();
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_GET_PROVISION_ALL,
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp,
						request.getBody().getActivityType(), request.getHeader().getAppName());
			}

		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<List<ProvisionTrazaDto>>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()), ex.getMessage().toString(), null);

			timestamp = getTimestamp();
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_GET_PROVISION_ALL, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());
		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/getProvisionDetailById", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<ProvisionDetailTrazaDto>> getProvisionDetailById(
			@RequestBody ApiRequest<ProvisionRequest> request) {

		ApiResponse<ProvisionDetailTrazaDto> apiResponse;
		HttpStatus status;
		String timestamp = "";

		try {
			ProvisionDetailTrazaDto provisions = provisionService.getProvisionDetailById(request.getBody());

			if (provisions != null) {
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<ProvisionDetailTrazaDto>(Constants.APP_NAME_PROVISION,
						Constants.OPER_GET_PROVISION_DETAIL, String.valueOf(status.value()), status.getReasonPhrase(),
						null);
				apiResponse.setBody(provisions);

				timestamp = getTimestamp();
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.ACCESS_APP_TYPE_ORDER, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			} else {
				status = HttpStatus.NOT_FOUND;
				apiResponse = new ApiResponse<ProvisionDetailTrazaDto>(Constants.APP_NAME_PROVISION,
						Constants.OPER_GET_PROVISION_DETAIL, String.valueOf(status.value()),
						"No se encontraron provisiones", null);
				apiResponse.setBody(provisions);

				timestamp = getTimestamp();
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.ACCESS_APP_TYPE_ORDER,
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp,
						request.getBody().getActivityType(), request.getHeader().getAppName());
			}

		} catch (Exception ex) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<ProvisionDetailTrazaDto>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_DETAIL, String.valueOf(status.value()), ex.getMessage().toString(),
					null);

			timestamp = getTimestamp();
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.ACCESS_APP_TYPE_ORDER, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());
		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/aftersales/services-contracted-by-customer", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<List<ProvisionDto>>> getOrders(
			@RequestBody ApiRequest<ProvisionRequest> request) {

		ApiResponse<List<ProvisionDto>> apiResponse;
		HttpStatus status;
		String errorInternal = "";
		String timestamp = "";

		if (request.getBody().getDocumentType() == null || request.getBody().getDocumentType().equals("")) {

			status = HttpStatus.BAD_REQUEST;
			errorInternal = InternalError.TRZ06.toString();
			errorInternal = ErrorCode.get(Constants.GET_ORDERS + errorInternal.replace("\"", "")).toString();

			timestamp = getTimestamp();

			apiResponse = new ApiResponse<List<ProvisionDto>>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_ALL, errorInternal, "Tipo de documento obligatorio", null);
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

			return ResponseEntity.status(status).body(apiResponse);
		}

		if (request.getBody().getDocumentNumber() == null || request.getBody().getDocumentNumber().equals("")) {

			status = HttpStatus.BAD_REQUEST;
			errorInternal = InternalError.TRZ06.toString();
			errorInternal = ErrorCode.get(Constants.GET_ORDERS + errorInternal.replace("\"", "")).toString();

			timestamp = getTimestamp();
			apiResponse = new ApiResponse<List<ProvisionDto>>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_ALL, errorInternal, "Numero de documento obligatorio", null);
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

			return ResponseEntity.status(status).body(apiResponse);
		}

		Boolean typedata = request.getBody().getDocumentType() instanceof String;
		if (!typedata) {
			status = HttpStatus.BAD_REQUEST;
			errorInternal = InternalError.TRZ07.toString();
			errorInternal = ErrorCode.get(Constants.GET_ORDERS + errorInternal.replace("\"", "")).toString();

			timestamp = getTimestamp();
			apiResponse = new ApiResponse<List<ProvisionDto>>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_ALL, errorInternal, "Tipo de documento debe ser cadena", null);
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
			return ResponseEntity.status(status).body(apiResponse);

		}

		try {
			List<ProvisionDto> provisions = provisionService.getAll(request);

			if (provisions != null) {

				status = HttpStatus.OK;
				apiResponse = new ApiResponse<List<ProvisionDto>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()), status.getReasonPhrase(),
						null);
				apiResponse.setBody(provisions);

				timestamp = getTimestamp();
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_GET_PROVISION_ALL, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			} else {
				status = HttpStatus.NOT_FOUND;
				apiResponse = new ApiResponse<List<ProvisionDto>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()),
						"No se encontraron provisiones", null);
				apiResponse.setBody(provisions);

				timestamp = getTimestamp();
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_GET_PROVISION_ALL,
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp,
						request.getBody().getActivityType(), request.getHeader().getAppName());
			}

		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<List<ProvisionDto>>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()), ex.getMessage().toString(), null);

			timestamp = getTimestamp();
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_GET_PROVISION_ALL, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());
		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * @param provisionId
	 * @return
	 */
	@RequestMapping(value = "/getOrderStatus", method = RequestMethod.GET)
	public ResponseEntity<ProvisionResponse<String>> getOrderStatus(
			@RequestParam(value = "provisionId", required = true) String provisionId) {
		return ResponseEntity.ok(provisionService.getStatus(provisionId));
	}

	@RequestMapping(value = "/getProvisionBySaleCode", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<Provision>> getCustomerByOrderCode(
			@RequestBody @Valid ApiRequest<GetCustomerByOrderCodeRequest> request) {
		ApiResponse<Provision> apiResponse;
		HttpStatus status;

		try {
			Provision provision = provisionService.getProvisionBySaleCode(request.getBody().getSaleCode());

			if (provision != null) {
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
						Constants.OPER_GET_PROVISION_BY_SALE_CODE, String.valueOf(status.value()),
						status.getReasonPhrase(), null);
				apiResponse.setBody(provision);
			} else {
				status = HttpStatus.NOT_FOUND;

				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
						Constants.OPER_GET_PROVISION_BY_SALE_CODE, String.valueOf(status.value()),
						"No se encontro registro", null);
				apiResponse.setBody(null);
			}
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_BY_SALE_CODE, String.valueOf(status.value()),
					ex.getMessage().toString(), null);
		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/insertOrder", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<Provision>> insertOrder(
			@RequestBody @Valid ApiRequest<InsertOrderRequest> request) {
		ApiResponse<Provision> apiResponse;
		HttpStatus status;

		try {
			boolean provisions = processMessage(request.getBody());

			if (provisions) {
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION, Constants.OPER_INSERT_PROVISION,
						String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(null);
			} else {
				status = HttpStatus.NOT_FOUND;

				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION, Constants.OPER_INSERT_PROVISION,
						String.valueOf(status.value()), "No se pudo insetar provision", null);
				apiResponse.setBody(null);
			}
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION, Constants.OPER_INSERT_PROVISION,
					String.valueOf(status.value()), ex.getMessage().toString(), null);

		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	private boolean processMessage(InsertOrderRequest request) {

		// Insertar en colección de Ordenes
		orderService.createOrder(request.getData());

		// Insertar en colección de Provisión
		return provisionService.insertProvision(request);
	}

	@RequestMapping(value = "/updateOrderFromTOA", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<Provision>> updateOrderFromTOA(
			@RequestBody @Valid ApiRequest<UpdateFromToaRequest> request) {
		ApiResponse<Provision> apiResponse;
		HttpStatus status;

		Boolean provisions = false;

		try {

			KafkaTOARequest kafkaTOARequest = new Gson().fromJson(request.getBody().getData(), KafkaTOARequest.class);

			provisions = kafkaTOARequest.getEvent().getAppointment().getScheduler().equals(Constants.SCHEDULER_PSI)
					? provisionUpdateAsisService.provisionUpdateFromTOA(kafkaTOARequest)
					: provisionUpdateTobeService.provisionUpdateFromTOA(kafkaTOARequest);

			if (provisions) {
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
						Constants.OPER_PROVISION_UPDATE_FROM_TOA, String.valueOf(status.value()),
						status.getReasonPhrase(), null);
				apiResponse.setBody(null);
			} else {
				status = HttpStatus.NOT_FOUND;

				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
						Constants.OPER_PROVISION_UPDATE_FROM_TOA, String.valueOf(status.value()),
						"No se encontro registro", null);
				apiResponse.setBody(null);
			}
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
					Constants.OPER_PROVISION_UPDATE_FROM_TOA, String.valueOf(status.value()), ex.getMessage(), null);

		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/provisionInsertCodeFictitious", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<Provision>> provisionInsertCodeFictional(
			@RequestBody @Valid ApiRequest<InsertCodeFictionalRequest> request) {
		ApiResponse<Provision> apiResponse;
		HttpStatus status;

		try {
			Boolean provisions = provisionService.provisionInsertCodeFictitious(request.getBody());

			if (provisions) {
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
						Constants.OPER_INSERT_PROVISION_CODE_FICT, String.valueOf(status.value()),
						status.getReasonPhrase(), null);
				apiResponse.setBody(null);
			} else {
				status = HttpStatus.NOT_FOUND;

				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
						Constants.OPER_INSERT_PROVISION_CODE_FICT, String.valueOf(status.value()),
						"No se pudo actualizar provision", null);
				apiResponse.setBody(null);
			}
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
					Constants.OPER_INSERT_PROVISION_CODE_FICT, String.valueOf(status.value()),
					ex.getMessage().toString(), null);

		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * 
	 * @param provisionId
	 * @return
	 */

	@RequestMapping(value = "/setProvisionValidated", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<List<Provision>>> setProvisionValidated(
			@RequestBody ApiRequest<ValidateDataRequest> request) {

		String timestamp;
		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;

		try {
			Provision result = provisionService.setProvisionIsValidated(request.getBody().getProvisionId());

			if (result != null) {
				List<Provision> provisions = new ArrayList<>();
				timestamp = getTimestamp();
				provisions.add(result);

				status = HttpStatus.OK;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_VALIDATE_DATA, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(provisions);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_VALIDATE_DATA, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			} else {
				status = HttpStatus.BAD_REQUEST;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_VALIDATE_DATA, String.valueOf(status.value()), status.getReasonPhrase(), null);

				timestamp = getTimestamp();

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_VALIDATE_DATA,
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp,
						request.getBody().getActivityType(), request.getHeader().getAppName());

			}
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_DATA,
					String.valueOf(status.value()), ex.getMessage().toString(), null);

			timestamp = getTimestamp();

			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_VALIDATE_DATA, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());

		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
//@RequestBody @Validated ApiRequest<ApiTrazaSetContactInfoUpdateRequest> request
	@RequestMapping(value = "/setContactInfoUpdate", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<ProvisionDetailTrazaDto>> setContactInfoUpdate(
			@RequestBody @Validated ApiRequest<ApiTrazaSetContactInfoUpdateRequest> request) {

		ApiResponse<ProvisionDetailTrazaDto> apiResponse;
		HttpStatus status;
		String errorInternal = "";
		String timestamp = "";

		try {

			ApiTrazaSetContactInfoUpdateRequest requestBody = request.getBody();

			// Validate PSICode
			if (requestBody.getPsiCode() == null || requestBody.getPsiCode().equals("")) {

				status = HttpStatus.BAD_REQUEST;
				errorInternal = InternalError.TRZ01.toString();
				errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
						.toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
						errorInternal, "PSICode obligatorio", null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				return ResponseEntity.status(status).body(apiResponse);
			}

			if (requestBody.getPsiCode() != null) {
				status = HttpStatus.BAD_REQUEST;
				if (requestBody.getPsiCode().length() > 11) {
					errorInternal = InternalError.TRZ02.toString();
					errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
							.toString();
					timestamp = getTimestamp();
					apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
							errorInternal, "PSICode maximo 11 caracteres", null);
					apiResponse.getHeader().setTimestamp(timestamp);
					apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

					return ResponseEntity.status(status).body(apiResponse);
				}
				Boolean typedata = requestBody.getPsiCode() instanceof String;
				if (!typedata) {
					errorInternal = InternalError.TRZ03.toString();
					errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
							.toString();
					timestamp = getTimestamp();
					apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
							errorInternal, "PSICode debe ser una cadena", null);
					apiResponse.getHeader().setTimestamp(timestamp);
					apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

					return ResponseEntity.status(status).body(apiResponse);
				}

			}

			// Validate contact
			List<ContactRequest> contact = requestBody.getContacts();

			if (requestBody.getContacts().size() > 0) {
				status = HttpStatus.BAD_REQUEST;

				for (ContactRequest list : contact) {
					if (list.getFullName() == null) {
						// contactFullname;
						// contactCellphone;
						errorInternal = InternalError.TRZ01.toString();
						errorInternal = ErrorCode
								.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", "")).toString();

						timestamp = getTimestamp();
						apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
								Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "fullName obligatorio", null);

						apiResponse.getHeader().setTimestamp(timestamp);
						apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

						return ResponseEntity.status(status).body(apiResponse);
					}
					if (list.getPhoneNumber() == null) {
						errorInternal = InternalError.TRZ01.toString();
						errorInternal = ErrorCode
								.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", "")).toString();

						timestamp = getTimestamp();
						apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
								Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "phoneNumber obligatorio", null);
						apiResponse.getHeader().setTimestamp(timestamp);
						apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

						return ResponseEntity.status(status).body(apiResponse);
					}

					boolean typePhone = list.getPhoneNumber() instanceof Integer;
					if (!typePhone) {
						errorInternal = InternalError.TRZ03.toString();
						errorInternal = ErrorCode
								.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", "")).toString();

						timestamp = getTimestamp();
						apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
								Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "phoneNumber debe ser numerico",
								null);
						apiResponse.getHeader().setTimestamp(timestamp);
						apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

						return ResponseEntity.status(status).body(apiResponse);
					}

					String countPhone = list.getPhoneNumber().toString();
					if (countPhone.length() > 9) {
						errorInternal = InternalError.TRZ02.toString();
						errorInternal = ErrorCode
								.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", "")).toString();

						timestamp = getTimestamp();
						apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
								Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "phoneNumber maximo 9 caracteres",
								null);
						apiResponse.getHeader().setTimestamp(timestamp);
						apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

						return ResponseEntity.status(status).body(apiResponse);
					}

				}
			}

			if (!requestBody.isHolderWillReceive()
					&& (requestBody.getContacts().size() == 0 || requestBody.getContacts().size() > 4)) {

				status = HttpStatus.BAD_REQUEST;
				errorInternal = InternalError.TRZ04.toString();
				errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
						.toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
						errorInternal, "Minimo 1 y maximo 4 datos datos de contacto", null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				return ResponseEntity.status(status).body(apiResponse);
			}

			ProvisionDetailTrazaDto result = provisionService.setContactInfoUpdate(request.getBody());

			if (result != null) {
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
						String.valueOf(status.value()), status.getReasonPhrase(), result);

				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
				timestamp = getTimestamp();
				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "OK",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			} else {
				status = HttpStatus.NOT_FOUND;
				errorInternal = InternalError.TRZ05.toString();
				errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
						.toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
						errorInternal, "No existe registro", null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			}

		} catch (BadRequest ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.BAD_REQUEST;

			timestamp = getTimestamp();
			apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
					String.valueOf(status.value()), ex.getMessage().toString(), null);
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
			restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
					new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());

		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			if (ex instanceof FunctionalErrorException) {

				status = HttpStatus.BAD_REQUEST;

				String errorCode = ((FunctionalErrorException) ex).getErrorCode().replace("\"", "");
				if (errorCode.equals("ERR10") || errorCode.equals("ERR11") || errorCode.equals("ERR02")) {
					status = HttpStatus.BAD_REQUEST;
				} else if (errorCode.equals("ERR15")) {
					status = HttpStatus.UNAUTHORIZED;
				} else if (errorCode.equals("ERR03")) {
					status = HttpStatus.NOT_FOUND;
				} else if (errorCode.equals("ERR19")) {
					status = HttpStatus.CONFLICT;
				}

				errorCode = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorCode.replace("\"", "")).toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
						errorCode, ((FunctionalErrorException) ex).getMessage().replace("\"", ""), null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				timestamp = getTimestamp();
				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
						String.valueOf(status.value()), ex.getMessage().toString(), null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());
			}
		}
		return ResponseEntity.status(status).body(apiResponse);
	}
	
	@RequestMapping(value = "/setContactInfoUpdateWeb", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<ProvisionDetailTrazaDto>> setContactInfoUpdateWeb(
			@RequestBody @Validated ApiRequest<ApiTrazaSetContactInfoUpdateRequest> request) {
		log.info(this.getClass().getName() + " - " + "setContactInfoUpdate");

		ApiResponse<ProvisionDetailTrazaDto> apiResponse;
		HttpStatus status;
		String errorInternal = "";
		String timestamp = "";

		try {

			ApiTrazaSetContactInfoUpdateRequest requestBody = request.getBody();

			// Validate PSICode
			if (requestBody.getPsiCode() == null || requestBody.getPsiCode().equals("")) {

				status = HttpStatus.BAD_REQUEST;
				errorInternal = InternalError.TRZ01.toString();
				errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
						.toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "PSICode obligatorio", null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				return ResponseEntity.status(status).body(apiResponse);
			}

			if (requestBody.getPsiCode() != null) {
				status = HttpStatus.BAD_REQUEST;
//				if (requestBody.getPsiCode().length() > 11) {
//					errorInternal = InternalError.TRZ02.toString();
//					errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
//							.toString();
//					timestamp = getTimestamp();
//					apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
//							Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "PSICode maximo 11 caracteres", null);
//					apiResponse.getHeader().setTimestamp(timestamp);
//					apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
//
//					return ResponseEntity.status(status).body(apiResponse);
//				}
				Boolean typedata = requestBody.getPsiCode() instanceof String;
				if (!typedata) {
					errorInternal = InternalError.TRZ03.toString();
					errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
							.toString();
					timestamp = getTimestamp();
					apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
							Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "PSICode debe ser una cadena", null);
					apiResponse.getHeader().setTimestamp(timestamp);
					apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

					return ResponseEntity.status(status).body(apiResponse);
				}

			}

			// Validate contact
			List<ContactRequest> contact = requestBody.getContacts();

			if (requestBody.getContacts().size() > 0) {

				status = HttpStatus.BAD_REQUEST;

				for (ContactRequest list : contact) {
					if (list.getFullName() == null) {
						// contactFullname;
						// contactCellphone;
						errorInternal = InternalError.TRZ01.toString();
						errorInternal = ErrorCode
								.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", "")).toString();

						timestamp = getTimestamp();
						apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
								Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "fullName obligatorio", null);

						apiResponse.getHeader().setTimestamp(timestamp);
						apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

						return ResponseEntity.status(status).body(apiResponse);
					}
					if (list.getPhoneNumber() == null) {
						errorInternal = InternalError.TRZ01.toString();
						errorInternal = ErrorCode
								.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", "")).toString();

						timestamp = getTimestamp();
						apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
								Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "phoneNumber obligatorio", null);
						apiResponse.getHeader().setTimestamp(timestamp);
						apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

						return ResponseEntity.status(status).body(apiResponse);
					}

					boolean typePhone = list.getPhoneNumber() instanceof Integer;
					if (!typePhone) {
						errorInternal = InternalError.TRZ03.toString();
						errorInternal = ErrorCode
								.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", "")).toString();

						timestamp = getTimestamp();
						apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
								Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "phoneNumber debe ser numerico",
								null);
						apiResponse.getHeader().setTimestamp(timestamp);
						apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

						return ResponseEntity.status(status).body(apiResponse);
					}

					String countPhone = list.getPhoneNumber().toString();
					if (countPhone.length() > 9) {
						errorInternal = InternalError.TRZ02.toString();
						errorInternal = ErrorCode
								.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", "")).toString();

						timestamp = getTimestamp();
						apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
								Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "phoneNumber maximo 9 caracteres",
								null);
						apiResponse.getHeader().setTimestamp(timestamp);
						apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

						return ResponseEntity.status(status).body(apiResponse);
					}

				}
			}

			if (!requestBody.isHolderWillReceive()
					&& (requestBody.getContacts().size() == 0 || requestBody.getContacts().size() > 4)) {

				status = HttpStatus.BAD_REQUEST;
				errorInternal = InternalError.TRZ04.toString();
				errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
						.toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, errorInternal,
						"Minimo 1 y maximo 4 datos datos de contacto", null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				return ResponseEntity.status(status).body(apiResponse);
			}

			ProvisionDetailTrazaDto result = provisionService.setContactInfoUpdateWeb(request.getBody());

			if (result != null) {
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, String.valueOf(status.value()), status.getReasonPhrase(),
						result);

				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
				timestamp = getTimestamp();
				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "OK",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			} else {
				status = HttpStatus.NOT_FOUND;
				errorInternal = InternalError.TRZ05.toString();
				errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
						.toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "No existe registro", null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			}

		} catch (BadRequest ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());
			
			status = HttpStatus.BAD_REQUEST;

			timestamp = getTimestamp();
			apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
					String.valueOf(status.value()), ex.getMessage().toString(), null);
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
			restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
					new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());

		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());
			
			if (ex instanceof FunctionalErrorException) {

				status = HttpStatus.BAD_REQUEST;

				String errorCode = ((FunctionalErrorException) ex).getErrorCode().replace("\"", "");
				if (errorCode.equals("ERR10") || errorCode.equals("ERR11") || errorCode.equals("ERR02")) {
					status = HttpStatus.BAD_REQUEST;
				} else if (errorCode.equals("ERR15")) {
					status = HttpStatus.UNAUTHORIZED;
				} else if (errorCode.equals("ERR03")) {
					status = HttpStatus.NOT_FOUND;
				} else if (errorCode.equals("ERR19")) {
					status = HttpStatus.CONFLICT;
				}

				errorCode = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorCode.replace("\"", "")).toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, errorCode,
						((FunctionalErrorException) ex).getMessage().replace("\"", ""), null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				timestamp = getTimestamp();
				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, String.valueOf(status.value()), ex.getMessage(),
						null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());
			}
		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/update-customer", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<String>> apiTrazaSetContactInfoUpdate(
			@RequestBody @Validated ApiRequest<ApiTrazaSetContactInfoUpdateRequest> request) {

		ApiResponse<String> apiResponse;
		HttpStatus status;
		String errorInternal = "";
		String timestamp = "";

		try {

			ApiTrazaSetContactInfoUpdateRequest requestBody = request.getBody();

			// Validate PSICode
			if (requestBody.getPsiCode() == null || requestBody.getPsiCode().equals("")) {

				status = HttpStatus.BAD_REQUEST;
				errorInternal = InternalError.TRZ01.toString();
				errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
						.toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
						errorInternal, "PSICode obligatorio", null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				return ResponseEntity.status(status).body(apiResponse);
			}

			if (requestBody.getPsiCode() != null) {
				status = HttpStatus.BAD_REQUEST;
				if (requestBody.getPsiCode().length() > 11) {
					errorInternal = InternalError.TRZ02.toString();
					errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
							.toString();
					timestamp = getTimestamp();
					apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION,
							Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "PSICode maximo 11 caracteres", null);
					apiResponse.getHeader().setTimestamp(timestamp);
					apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

					return ResponseEntity.status(status).body(apiResponse);
				}
				Boolean typedata = requestBody.getPsiCode() instanceof String;
				if (!typedata) {
					errorInternal = InternalError.TRZ03.toString();
					errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
							.toString();
					timestamp = getTimestamp();
					apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION,
							Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "PSICode debe ser una cadena", null);
					apiResponse.getHeader().setTimestamp(timestamp);
					apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

					return ResponseEntity.status(status).body(apiResponse);
				}

			}

			// Validate email

			if (requestBody.getEmail() != null && requestBody.getEmail().trim().length() > 0) {
				status = HttpStatus.BAD_REQUEST;
				if (requestBody.getEmail().length() > 100) {
					errorInternal = InternalError.TRZ02.toString();
					errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
							.toString();
					timestamp = getTimestamp();
					apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION,
							Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "email maximo 100 caracteres", null);
					apiResponse.getHeader().setTimestamp(timestamp);
					apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
					return ResponseEntity.status(status).body(apiResponse);
				}

				String regex = "^(.+)@(.+)$";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(requestBody.getEmail());
				if (!matcher.matches()) {
					errorInternal = InternalError.TRZ03.toString();
					errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
							.toString();

					timestamp = getTimestamp();
					apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION,
							Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "email formato invalido ", null);
					apiResponse.getHeader().setTimestamp(timestamp);
					apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
					return ResponseEntity.status(status).body(apiResponse);
				}

			}
			// Validate contact

			List<ContactRequest> contact = requestBody.getContacts();

			if (requestBody.getContacts().size() > 0) {
				status = HttpStatus.BAD_REQUEST;

				for (ContactRequest list : contact) {
					if (list.getFullName() == null) {
						errorInternal = InternalError.TRZ01.toString();
						errorInternal = ErrorCode
								.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", "")).toString();

						timestamp = getTimestamp();
						apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION,
								Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "fullName obligatorio", null);

						apiResponse.getHeader().setTimestamp(timestamp);
						apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

						return ResponseEntity.status(status).body(apiResponse);
					}
					if (list.getPhoneNumber() == null) {
						errorInternal = InternalError.TRZ01.toString();
						errorInternal = ErrorCode
								.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", "")).toString();

						timestamp = getTimestamp();
						apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION,
								Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "phoneNumber obligatorio", null);
						apiResponse.getHeader().setTimestamp(timestamp);
						apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

						return ResponseEntity.status(status).body(apiResponse);
					}

					boolean typePhone = list.getPhoneNumber() instanceof Integer;
					if (!typePhone) {
						errorInternal = InternalError.TRZ03.toString();
						errorInternal = ErrorCode
								.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", "")).toString();

						timestamp = getTimestamp();
						apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION,
								Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "phoneNumber debe ser numerico",
								null);
						apiResponse.getHeader().setTimestamp(timestamp);
						apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

						return ResponseEntity.status(status).body(apiResponse);
					}

					String countPhone = list.getPhoneNumber().toString();
					if (countPhone.length() > 9) {
						errorInternal = InternalError.TRZ02.toString();
						errorInternal = ErrorCode
								.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", "")).toString();

						timestamp = getTimestamp();
						apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION,
								Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "phoneNumber maximo 9 caracteres",
								null);
						apiResponse.getHeader().setTimestamp(timestamp);
						apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

						return ResponseEntity.status(status).body(apiResponse);
					}

				}
			}
			if (requestBody.getContacts().size() == 0 || requestBody.getContacts().size() > 4) {

				status = HttpStatus.BAD_REQUEST;
				errorInternal = InternalError.TRZ04.toString();
				errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
						.toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
						errorInternal, "Minimo 1 y maximo 4 datos datos de contacto", null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				return ResponseEntity.status(status).body(apiResponse);
			}

			Boolean result = provisionService.apiContactInfoUpdate(request.getBody());

			if (result) {

				status = HttpStatus.OK;
				apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
						String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody("OK");

				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
				timestamp = getTimestamp();
				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "OK",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			} else {
				status = HttpStatus.NOT_FOUND;
				errorInternal = InternalError.TRZ05.toString();
				errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
						.toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
						errorInternal, "No existe registro", null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			}

		} catch (BadRequest ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.BAD_REQUEST;

			timestamp = getTimestamp();
			apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
					String.valueOf(status.value()), ex.getMessage().toString(), null);
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
			restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
					new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());
		}

		catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			if (ex instanceof FunctionalErrorException) {

				status = HttpStatus.BAD_REQUEST;

				String errorCode = ((FunctionalErrorException) ex).getErrorCode().replace("\"", "");
				if (errorCode.equals("ERR10") || errorCode.equals("ERR11") || errorCode.equals("ERR02")) {
					status = HttpStatus.BAD_REQUEST;
				} else if (errorCode.equals("ERR15")) {
					status = HttpStatus.UNAUTHORIZED;
				} else if (errorCode.equals("ERR03")) {
					status = HttpStatus.NOT_FOUND;
				} else if (errorCode.equals("ERR19")) {
					status = HttpStatus.CONFLICT;
				}

				errorCode = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorCode.replace("\"", "")).toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
						errorCode, ((FunctionalErrorException) ex).getMessage().replace("\"", ""), null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				timestamp = getTimestamp();
				apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
						String.valueOf(status.value()), ex.getMessage().toString(), null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());
			}
		}

		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * 
	 * @param provisionId
	 * @return
	 */

	@RequestMapping(value = "/requestAddressUpdate", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<ProvisionDetailTrazaDto>> requestAddressUpdate(
			@RequestBody ApiRequest<AddressUpdateRequest> request) {

		String timestamp;
		ApiResponse<ProvisionDetailTrazaDto> apiResponse;
		HttpStatus status;
		try {

			ProvisionDetailTrazaDto result = provisionService.requestAddressUpdate(request.getBody().getProvisionId());

			if (result != null) {

				status = HttpStatus.OK;
				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESS,
						String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(result);

				timestamp = getTimestamp();

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_ADDRESS, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			} else {
				status = HttpStatus.OK;

				apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESS,
						String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(null);

				timestamp = getTimestamp();

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "NOT_MATCH", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_ADDRESS,
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp,
						request.getBody().getActivityType(), request.getHeader().getAppName());
			}

		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESS,
					String.valueOf(status.value()), ex.getMessage().toString(), null);

			timestamp = getTimestamp();

			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_UPDATE_ADDRESS, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());

		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */

	@RequestMapping(value = "/receiveAddressUpdateBO", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<Object>> receiveAddressUpdateBO(
			@RequestBody ApiRequest<ReceiveAddressUpdateBORequest> request) {

		String timestamp;
		ApiResponse<Object> apiResponse;
		HttpStatus status;

		try {
			Boolean result = provisionService.receiveAddressUpdateBO(request.getBody().getAction(),
					request.getBody().getProvisionId(), request.getBody().getNewDepartment(),
					request.getBody().getNewProvince(), request.getBody().getNewDistrict(),
					request.getBody().getNewAddress(), request.getBody().getNewReference(),
					request.getBody().getIsSMSRequired());

			if (result) {
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<Object>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESSBO,
						String.valueOf(status.value()), status.getReasonPhrase(), null);

				apiResponse.setBody(null);
				timestamp = getTimestamp();
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_ADDRESSBO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			} else {
				status = HttpStatus.BAD_REQUEST;
				apiResponse = new ApiResponse<Object>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESSBO,
						String.valueOf(status.value()), status.getReasonPhrase(), null);

				timestamp = getTimestamp();
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_ADDRESSBO,
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp,
						request.getBody().getActivityType(), request.getHeader().getAppName());

			}

		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Object>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESSBO,
					String.valueOf(status.value()), ex.getMessage(), null);

			timestamp = getTimestamp();
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_UPDATE_ADDRESSBO, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());

		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * 
	 * @param provisionId
	 * @return
	 */

	@RequestMapping(value = "/orderCancellation", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<ProvisionDetailTrazaDto>> orderCancellation(
			@RequestBody ApiRequest<CancelOrderRequest> request) {

		String timestamp;
		ApiResponse<ProvisionDetailTrazaDto> apiResponse;
		HttpStatus status;

		try {

			ProvisionDetailTrazaDto result = provisionService.orderCancellation(request.getBody().getProvisionId(),
					request.getBody().getCause(), request.getBody().getDetail(), request.getBody().getScheduler());

			if (result != null) {
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<ProvisionDetailTrazaDto>(Constants.APP_NAME_PROVISION,
						Constants.OPER_ORDER_CANCELLATION, String.valueOf(status.value()), status.getReasonPhrase(),
						null);
				apiResponse.setBody(result);

				timestamp = getTimestamp();
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_CANCEL, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());

			} else {
				status = HttpStatus.BAD_REQUEST;
				apiResponse = new ApiResponse<ProvisionDetailTrazaDto>(Constants.APP_NAME_PROVISION,
						Constants.OPER_ORDER_CANCELLATION, String.valueOf(status.value()), status.getReasonPhrase(),
						null);

				timestamp = getTimestamp();
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_CANCEL,
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp,
						request.getBody().getActivityType(), request.getHeader().getAppName());

			}

		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			if (ex instanceof FunctionalErrorException) {

				status = HttpStatus.BAD_REQUEST;

				String errorCode[] = ((FunctionalErrorException) ex).getErrorCode().split("_");
				Integer htttCode = Integer.parseInt(errorCode[0]);

				if (htttCode.equals(400)) {
					status = HttpStatus.BAD_REQUEST;
				} else if (htttCode.equals(401)) {
					status = HttpStatus.UNAUTHORIZED;
				} else if (htttCode.equals(404)) {
					status = HttpStatus.NOT_FOUND;
				} else if (htttCode.equals(409)) {
					status = HttpStatus.CONFLICT;
				}

				apiResponse = new ApiResponse<ProvisionDetailTrazaDto>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, errorCode[1], ((FunctionalErrorException) ex).getMessage(),
						null);

				timestamp = getTimestamp();
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_CANCEL,
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp,
						request.getBody().getActivityType(), request.getHeader().getAppName());

			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				apiResponse = new ApiResponse<ProvisionDetailTrazaDto>(Constants.APP_NAME_PROVISION,
						Constants.OPER_ORDER_CANCELLATION, String.valueOf(status.value()), ex.getMessage().toString(),
						null);

				timestamp = getTimestamp();
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_CANCEL,
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp,
						request.getBody().getActivityType(), request.getHeader().getAppName());
			}
		}

		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/validateContingency", method = RequestMethod.GET)
	public ResponseEntity<ProvisionResponse<Boolean>> getOrderStatus() {
		return ResponseEntity.ok(provisionService.validateQueue());
	}

	@PostMapping(value = "/updateOrderSchedule")
	public ResponseEntity<ProvisionResponse<Boolean>> updateOrderSchedule(@RequestBody ProvisionScheduler request)
			throws ParseException {

		ProvisionResponse<Boolean> apiResponse;
		HttpStatus status = null;
		try {
			status = HttpStatus.OK;
			String[] scheduledDateStrArr = request.getScheduleDate().split("/");
			LocalDate scheduledDate = LocalDate.of(Integer.parseInt(scheduledDateStrArr[2]),
					Integer.parseInt(scheduledDateStrArr[1]), Integer.parseInt(scheduledDateStrArr[0]));
			apiResponse = provisionService.updateOrderSchedule(request.getIdProvision(), scheduledDate,
					request.getScheduleRange(), request.getScheduleType());
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ProvisionResponse<Boolean>().setData(null);
		}

		return ResponseEntity.status(status).body(apiResponse);

	}

	@RequestMapping(value = "/getAllInTimeRange", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<GetAllInTimeRangeResponse>> getAllInTimeRange(
			@RequestBody ApiRequest<GetAllInTimeRangeRequest> request) {

		ApiResponse<GetAllInTimeRangeResponse> apiResponse;
		HttpStatus status;

		try {
			String[] startDateStrArr = request.getBody().getStartDateStr().split("-");
			String[] endDateStrArr = request.getBody().getEndDateStr().split("-");
			// year, month, day, hour, minutes, seconds
			LocalDateTime startDate = LocalDateTime.of(Integer.parseInt(startDateStrArr[0]),
					Integer.parseInt(startDateStrArr[1]), Integer.parseInt(startDateStrArr[2]), 0, 0, 0);
			LocalDateTime endDate = LocalDateTime.of(Integer.parseInt(endDateStrArr[0]),
					Integer.parseInt(endDateStrArr[1]), Integer.parseInt(endDateStrArr[2]), 23, 59, 59);

			List<Provision> provisions = provisionService.getAllInTimeRange(startDate, endDate);
			GetAllInTimeRangeResponse response = new GetAllInTimeRangeResponse();
			response.setProvisions(provisions);

			status = HttpStatus.OK;
			apiResponse = new ApiResponse<GetAllInTimeRangeResponse>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_ALL_IN_TIME_RANGE, String.valueOf(status.value()), status.getReasonPhrase(),
					response);
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<GetAllInTimeRangeResponse>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_ALL_IN_TIME_RANGE, String.valueOf(status.value()), ex.getMessage(), null);
		}

		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/getAllResendNotification", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<List<ProvisionCustomerDto>>> getAllResendNotification(
			@RequestBody ApiRequest<GetAllInTimeRangeRequest> request) {

		ApiResponse<List<ProvisionCustomerDto>> apiResponse;
		HttpStatus status;

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime startDate = LocalDateTime.parse(request.getBody().getStartDateStr(), formatter);
			LocalDateTime endDate = LocalDateTime.parse(request.getBody().getEndDateStr(), formatter);

			List<ProvisionCustomerDto> provisions = provisionService.getAllResendNotification(startDate, endDate);

			status = HttpStatus.OK;

			apiResponse = new ApiResponse<List<ProvisionCustomerDto>>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_ALL_RESEND_NOTIFICATION, String.valueOf(status.value()),
					status.getReasonPhrase(), provisions);
		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;

			apiResponse = new ApiResponse<List<ProvisionCustomerDto>>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_ALL_RESEND_NOTIFICATION, String.valueOf(status.value()), ex.getMessage(), null);
		}

		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/getProvisionByOrderCode", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<Provision>> getProvisionByOrderCode(
			@RequestBody ApiRequest<GetProvisionByOrderCodeRequest> request) {

		ApiResponse<Provision> apiResponse;
		Provision provision = null;
		HttpStatus status;
		String timestamp;

		try {

			provision = provisionService.getProvisionByOrderCode(request);
			// apiResponse.setBody(provision);

			status = HttpStatus.OK;
			apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_BY_ORDER_CODE, String.valueOf(status.value()),
					status.getReasonPhrase(), provision);

			timestamp = getTimestamp();
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_GET_BY_ORDER_CODE, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());

		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_BY_ORDER_CODE, String.valueOf(status.value()), ex.getMessage(), null);

			timestamp = getTimestamp();
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_GET_BY_ORDER_CODE, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());
		}

		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/getContacts", method = RequestMethod.GET)
	public ResponseEntity<ApiResponse<List<Contacts>>> getContacts(
			@RequestParam(value = "provisionId", required = true) String provisionId) {
		return ResponseEntity.ok(provisionService.getContactList(provisionId));
	}

	public String getTimestamp() {
		LocalDateTime dateNow = LocalDateTime.now(ZoneOffset.of("-05:00"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.S");
		String timeStamp = dateNow.format(formatter);
		return timeStamp;
	}

	@RequestMapping(value = "/getOrderToNotify", method = RequestMethod.GET)
	public ResponseEntity<ApiResponse<List<Provision>>> getOrderToNotify() {

		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;

		List<Provision> provisions = provisionService.getOrderToNotify();

		status = HttpStatus.OK;
		apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_GET_ORDER_TO_NOTIFY,
				String.valueOf(status.value()), status.getReasonPhrase(), provisions);

		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/updateShowLocation", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<String>> updateShowLocation(
			@RequestBody @Valid ApiRequest<ProvisionRequest> request) {

		Provision provision = new Provision();
		provision.setIdProvision(request.getBody().getIdProvision());

		ApiResponse<String> apiResponse;
		HttpStatus status;
		String timestamp;

		try {
			boolean estado = provisionService.updateShowLocation(provision);

			if (estado) {

				status = HttpStatus.OK;
				apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_SHOW_LOCATION,
						String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody("OK");

				timestamp = getTimestamp();
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.UPDATE_SHOW_LOCATION, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
						request.getHeader().getAppName());
			} else {

				status = HttpStatus.OK;

				apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_SHOW_LOCATION,
						String.valueOf(status.value()), "No se encontraron datos del cliente", null);
				apiResponse.setBody(null);

				timestamp = getTimestamp();
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "NOT_MATCH", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.UPDATE_SHOW_LOCATION,
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp,
						request.getBody().getActivityType(), request.getHeader().getAppName());
			}

		} catch (Exception ex) {
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_SHOW_LOCATION,
					String.valueOf(status.value()), ex.getMessage(), null);

			timestamp = getTimestamp();
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.UPDATE_SHOW_LOCATION, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());
		}

		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/getUpFrontProvisions", method = RequestMethod.GET)
	public ResponseEntity<ApiResponse<List<Provision>>> getUpFrontProvisions() {

		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;
		String timestamp;
		List<Provision> provisions=null;
		try {
			provisions = provisionService.getUpFrontProvisions();

			status = HttpStatus.OK;
			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_GET_ORDER_TO_NOTIFY,
					String.valueOf(status.value()), status.getReasonPhrase(), provisions);
			
			timestamp = getTimestamp();
			restSecuritySaveLogData.saveLogData("",
					"", "",
					"", "INFO", new Gson().toJson(provisions), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_GET_UP_FRONT_PROVISIONS, "",
					"", timestamp, "",
					"");
			
		}catch(Exception ex){
			log.error(this.getClass().getName() + " - Exception: " + ex.getMessage());

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_SHOW_LOCATION,
					String.valueOf(status.value()), ex.getMessage(), null);

			timestamp = getTimestamp();
			restSecuritySaveLogData.saveLogData("",
					"", "",
					"", "ERROR", new Gson().toJson(provisions), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_GET_UP_FRONT_PROVISIONS, "",
					"", timestamp, "",
					"");
		}
		

		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/updateActivity", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<ProvisionDetailTrazaDto>> updateActivity(
			@RequestBody @Valid ApiRequest<ProvisionRequest> request) {
		
		ApiResponse<ProvisionDetailTrazaDto> apiResponse;
		HttpStatus status;
		String timestamp = "";
		ProvisionDetailTrazaDto provision;
		
		try {
			provision = provisionService.getProvisionDetailById(request.getBody());
			if (provision == null) {
				status = HttpStatus.NOT_FOUND;
				apiResponse = new ApiResponse<ProvisionDetailTrazaDto>(Constants.APP_NAME_PROVISION,
						Constants.OPER_UPDATE_ACTIVITY, String.valueOf(status.value()),
						"No se encontró provisón", null);
				apiResponse.setBody(provision);

				timestamp = getTimestamp();
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				// Log
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.ACCESS_APP_TYPE_ORDER,
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp,
						request.getBody().getActivityType(), request.getHeader().getAppName());				
			} else if (provision.getActivityId() != null) {
				provision = provisionService.updateActivity(provision.getIdProvision(), provision.getActivityId(), request.getBody().getIndicador());
				if (provision.getIdProvision() == null) {
					status = HttpStatus.BAD_REQUEST;
					apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ACTIVITY,
							String.valueOf(status.value()), "Error Actualización actividad", null);
					apiResponse.setBody(null);					
				} else {
					status = HttpStatus.OK;
					apiResponse = new ApiResponse<>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ACTIVITY,
							String.valueOf(status.value()), "Actualización actividad", null);
					apiResponse.setBody(provision);
				}
			} else {
				status = HttpStatus.NOT_FOUND;
				apiResponse = new ApiResponse<ProvisionDetailTrazaDto>(Constants.APP_NAME_PROVISION,
						Constants.OPER_UPDATE_ACTIVITY, String.valueOf(status.value()),
						"No existe activity_id", null);
				apiResponse.setBody(null);

				timestamp = getTimestamp();
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				// Log
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.ACCESS_APP_TYPE_ORDER,
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp,
						request.getBody().getActivityType(), request.getHeader().getAppName());
			}
		} catch (Exception ex) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<ProvisionDetailTrazaDto>(Constants.APP_NAME_PROVISION,
					Constants.OPER_UPDATE_ACTIVITY, String.valueOf(status.value()), ex.getMessage().toString(),
					null);

			// Log
			timestamp = getTimestamp();
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.ACCESS_APP_TYPE_ORDER, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp, request.getBody().getActivityType(),
					request.getHeader().getAppName());						
		}
		return ResponseEntity.status(status).body(apiResponse);
	}
		
}