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
import pe.telefonica.provision.controller.request.ProvisionRequest;
import pe.telefonica.provision.controller.request.ReceiveAddressUpdateBORequest;
import pe.telefonica.provision.controller.request.UpdateFromToaRequest;
import pe.telefonica.provision.controller.request.ValidateDataRequest;
import pe.telefonica.provision.controller.response.GetAllInTimeRangeResponse;
import pe.telefonica.provision.controller.response.ProvisionResponse;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.model.Contacts;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.model.ProvisionScheduler;
import pe.telefonica.provision.service.ProvisionService;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.constants.ConstantsLogData;
import pe.telefonica.provision.util.constants.ErrorCode;
import pe.telefonica.provision.util.constants.InternalError;
import pe.telefonica.provision.util.exception.FunctionalErrorException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("provision")
public class ProvisionController {

	private static final Log log = LogFactory.getLog(ProvisionController.class);

	@Autowired
	ProvisionService contactService;

	@Autowired
	TrazabilidadSecurityApi restSecuritySaveLogData;

	private final ProvisionService provisionService;

	@Autowired
	public ProvisionController(ProvisionService provisionService) {
		this.provisionService = provisionService;
	}

	@RequestMapping(value = "/getCustomerByDocument", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<Customer>> getCustomerByDocument(
			@RequestBody @Valid ApiRequest<ProvisionRequest> request) {

		ApiResponse<Customer> apiResponse;
		HttpStatus status;

		try {
			Customer customer = provisionService.validateUser(request);

			if (customer != null) {

				status = HttpStatus.OK;
				apiResponse = new ApiResponse<Customer>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_USER,
						String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(customer);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_VALIDATE_USER, "", "", "");
			} else {

				status = HttpStatus.OK;

				apiResponse = new ApiResponse<Customer>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_USER,
						String.valueOf(status.value()), "No se encontraron datos del cliente", null);
				apiResponse.setBody(null);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "NOT_MATCH", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_VALIDATE_USER, "", "", "");
			}

		} catch (Exception ex) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Customer>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_USER,
					String.valueOf(status.value()), ex.getMessage(), null);

			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_VALIDATE_USER, "", "", "");
		}

		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * 
	 * @param provisionRequest
	 * @return ProvisionResponse<Provision>
	 * @description get all provisions related to type and number of the document
	 */

	@RequestMapping(value = "/aftersales/services-contracted-by-customer", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<List<Provision>>> getOrders(@RequestBody ApiRequest<ProvisionRequest> request) {

		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;
		String errorInternal = "";
		String timestamp = "";

		// Validate documentType
		if (request.getBody().getDocumentType() == null || request.getBody().getDocumentType().equals("")) {

			status = HttpStatus.BAD_REQUEST;
			errorInternal = InternalError.TRZ06.toString();
			errorInternal = ErrorCode.get(Constants.GET_ORDERS + errorInternal.replace("\"", "")).toString();

			timestamp = getTimestamp();

			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
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
			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
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
			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_ALL, errorInternal, "Tipo de documento debe ser cadena", null);
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
			return ResponseEntity.status(status).body(apiResponse);

		}

		try {
			List<Provision> provisions = provisionService.getAll(request);

			if (provisions != null) {

				status = HttpStatus.OK;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
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
						request.getHeader().getTimestamp(), timestamp);

			} else {
				status = HttpStatus.NOT_FOUND;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
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
						request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp);
			}

		} catch (Exception ex) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()), ex.getMessage().toString(), null);

			timestamp = getTimestamp();
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_GET_PROVISION_ALL, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp);
		}
		return ResponseEntity.status(status).body(apiResponse);
		// return ResponseEntity.ok(provisionService.getAll(new
		// ProvisionRequest(documentType, documentNumber)));
	}

	/**
	 * 
	 * @param provisionId
	 * @return
	 */
	@RequestMapping(value = "/getOrderStatus", method = RequestMethod.GET)
	public ResponseEntity<ProvisionResponse<String>> getOrderStatus(
			@RequestParam(value = "provisionId", required = true) String provisionId) {
		return ResponseEntity.ok(provisionService.getStatus(provisionId));
	}

	/*
	 * @RequestMapping(value = "/insertOrdersOld", method = RequestMethod.POST)
	 * public ResponseEntity<ApiResponse<List<Provision>>> insertOrdersOld(
	 * 
	 * @RequestBody @Valid ApiRequest<List<Provision>> provisionListReq) {
	 * ApiResponse<List<Provision>> apiResponse; HttpStatus status;
	 * 
	 * try { List<Provision> provisions =
	 * provisionService.insertProvisionList(provisionListReq.getBody());
	 * 
	 * if (provisions != null) { status = HttpStatus.OK; apiResponse = new
	 * ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
	 * Constants.OPER_INSERT_PROVISION, String.valueOf(status.value()),
	 * status.getReasonPhrase(), null); apiResponse.setBody(provisions); } else {
	 * status = HttpStatus.NOT_FOUND;
	 * 
	 * apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
	 * Constants.OPER_INSERT_PROVISION, String.valueOf(status.value()),
	 * "No se encontraron provisiones", null); apiResponse.setBody(null); } } catch
	 * (Exception ex) {
	 * 
	 * status = HttpStatus.INTERNAL_SERVER_ERROR; apiResponse = new
	 * ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
	 * Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()),
	 * ex.getMessage().toString(), null);
	 * 
	 * } return ResponseEntity.status(status).body(apiResponse); // return //
	 * ResponseEntity.ok(provisionService.insertProvisionList(provisionListReq)); }
	 */

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

			Boolean provisions = provisionService.insertProvision(request.getBody());

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

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION, Constants.OPER_INSERT_PROVISION,
					String.valueOf(status.value()), ex.getMessage().toString(), null);

		}
		return ResponseEntity.status(status).body(apiResponse);
		// return
		// ResponseEntity.ok(provisionService.insertProvisionList(provisionListReq));
	}

	@RequestMapping(value = "/updateOrderFromTOA", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<Provision>> updateOrderFromTOA(
			@RequestBody @Valid ApiRequest<UpdateFromToaRequest> request) {
		ApiResponse<Provision> apiResponse;
		HttpStatus status;
		String separador = Pattern.quote(Constants.BARRA_VERTICAL);
		String[] parts = request.getBody().getData().split(separador);
		Boolean provisions = false;

		try {
			// Lógica diferencia Averias - Provision
			Object[] obj = new Object[2];
			obj = validateActivityType(parts);

			boolean provision = (boolean) obj[0];
			String xaRequest = (String) obj[1];
			String xaRequirementNumber = (String) obj[2];

			if (provision) {
				provisions = provisionService.provisionUpdateFromTOA(request.getBody(), xaRequest, xaRequirementNumber);
			} else {
				// Averia
			}

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

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
					Constants.OPER_PROVISION_UPDATE_FROM_TOA, String.valueOf(status.value()),
					ex.getMessage().toString(), null);

		}
		return ResponseEntity.status(status).body(apiResponse);
		// return
		// ResponseEntity.ok(provisionService.insertProvisionList(provisionListReq));
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

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
					Constants.OPER_INSERT_PROVISION_CODE_FICT, String.valueOf(status.value()),
					ex.getMessage().toString(), null);

		}
		return ResponseEntity.status(status).body(apiResponse);
		// return
		// ResponseEntity.ok(provisionService.insertProvisionList(provisionListReq));
	}

	/**
	 * 
	 * @param provisionId
	 * @return
	 */

	@RequestMapping(value = "/setProvisionValidated", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<List<Provision>>> setProvisionValidated(
			@RequestBody ApiRequest<ValidateDataRequest> request) {
		log.info(this.getClass().getName() + " - " + "setProvisionValidated");

		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;

		try {
			Provision result = provisionService.setProvisionIsValidated(request.getBody().getProvisionId());

			if (result != null) {

				List<Provision> provisions = new ArrayList<>();
				provisions.add(result);

				status = HttpStatus.OK;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_VALIDATE_DATA, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(provisions);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_VALIDATE_DATA, "", "", "");

				/*
				 * List<Provision> provisions = new ArrayList<>(); provisions.add(result);
				 * response.setHeader( new
				 * ProvisionHeaderResponse().generateHeader(HttpStatus.OK.value(),
				 * HttpStatus.OK.name())); response.setData(provisions);
				 * 
				 * restSecuritySaveLogData.saveLogData(request.getDocumentNumber(),
				 * request.getDocumentType(), request.getOrderCode(), request.getBucket(), "OK",
				 * new Gson().toJson(request), new Gson().toJson(response),
				 * ConstantsLogData.PROVISION_VALIDATE_DATA);
				 */

				// return ResponseEntity.ok(response);
			} else {
				status = HttpStatus.BAD_REQUEST;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_VALIDATE_DATA, String.valueOf(status.value()), status.getReasonPhrase(), null);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_VALIDATE_DATA, "", "", "");

				/*
				 * response.setHeader(new
				 * ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
				 * HttpStatus.BAD_REQUEST.name()));
				 * 
				 * restSecuritySaveLogData.saveLogData(request.getDocumentNumber(),
				 * request.getDocumentType(), request.getOrderCode(), request.getBucket(),
				 * "ERROR", new Gson().toJson(request), new Gson().toJson(response),
				 * ConstantsLogData.PROVISION_VALIDATE_DATA);
				 * 
				 * return ResponseEntity.badRequest().body(response);
				 */
			}
		} catch (Exception ex) {

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_DATA,
					String.valueOf(status.value()), ex.getMessage().toString(), null);

			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_VALIDATE_DATA, "", "", "");

			/*
			 * restSecuritySaveLogData.saveLogData(request.getDocumentNumber(),
			 * request.getDocumentType(), request.getOrderCode(), request.getBucket(),
			 * "ERROR", new Gson().toJson(request), new Gson().toJson(ex.getMessage()),
			 * ConstantsLogData.PROVISION_VALIDATE_DATA);
			 * 
			 * response.setHeader(new
			 * ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
			 * HttpStatus.BAD_REQUEST.name())); return
			 * ResponseEntity.badRequest().body(response);
			 */
		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	/*
	 * @RequestMapping(value = "/setContactInfoUpdate", method = RequestMethod.POST)
	 * public ResponseEntity<ApiResponse<List<Provision>>> setContactInfoUpdate(
	 * 
	 * @RequestBody ApiRequest<SetContactInfoUpdateRequest> request) {
	 * log.info(this.getClass().getName() + " - " + "setContactInfoUpdate");
	 * 
	 * ApiResponse<List<Provision>> apiResponse; HttpStatus status; try {
	 * 
	 * Provision result =
	 * provisionService.setContactInfoUpdate(request.getBody().getProvisionId(),
	 * request.getBody().getContactFullname(),
	 * request.getBody().getContactCellphone(),
	 * request.getBody().getContactCellphoneIsMovistar());
	 * 
	 * if (result != null) {
	 * 
	 * List<Provision> provisions = new ArrayList<>(); provisions.add(result);
	 * 
	 * status = HttpStatus.OK; apiResponse = new
	 * ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
	 * Constants.OPER_CONTACT_INFO_UPDATE, String.valueOf(status.value()),
	 * status.getReasonPhrase(), null); apiResponse.setBody(provisions);
	 * 
	 * restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
	 * request.getBody().getDocumentType(), request.getBody().getOrderCode(),
	 * request.getBody().getBucket(), "OK", new Gson().toJson(request), new
	 * Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO,
	 * "", "", "");
	 * 
	 * } else { status = HttpStatus.BAD_REQUEST; apiResponse = new
	 * ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
	 * Constants.OPER_CONTACT_INFO_UPDATE, String.valueOf(status.value()),
	 * "No existe registro", null);
	 * 
	 * restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
	 * request.getBody().getDocumentType(), request.getBody().getOrderCode(),
	 * request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new
	 * Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO,
	 * "", "", "");
	 * 
	 * }
	 * 
	 * } catch (Exception ex) { if (ex instanceof FunctionalErrorException) {
	 * 
	 * status = HttpStatus.BAD_REQUEST;
	 * 
	 * String errorCode = ((FunctionalErrorException)
	 * ex).getErrorCode().replace("\"", ""); if (errorCode.equals("ERR10") ||
	 * errorCode.equals("ERR11") || errorCode.equals("ERR02")) { status =
	 * HttpStatus.BAD_REQUEST; } else if (errorCode.equals("ERR15")) { status =
	 * HttpStatus.UNAUTHORIZED; } else if (errorCode.equals("ERR03")) { status =
	 * HttpStatus.NOT_FOUND; } else if (errorCode.equals("ERR19")) { status =
	 * HttpStatus.CONFLICT; }
	 * 
	 * errorCode = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT +
	 * errorCode.replace("\"", "")).toString();
	 * 
	 * apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
	 * Constants.OPER_CONTACT_INFO_UPDATE, errorCode, ((FunctionalErrorException)
	 * ex).getMessage().replace("\"", ""), null);
	 * 
	 * restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
	 * request.getBody().getDocumentType(), request.getBody().getOrderCode(),
	 * request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new
	 * Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO,
	 * "", "", "");
	 * 
	 * } else { status = HttpStatus.INTERNAL_SERVER_ERROR; apiResponse = new
	 * ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
	 * Constants.OPER_CONTACT_INFO_UPDATE, String.valueOf(status.value()),
	 * ex.getMessage().toString(), null);
	 * 
	 * restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
	 * request.getBody().getDocumentType(), request.getBody().getOrderCode(),
	 * request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new
	 * Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO,
	 * "", "", ""); } }
	 * 
	 * return ResponseEntity.status(status).body(apiResponse); }
	 */

	@RequestMapping(value = "/setContactInfoUpdate", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<Provision>> setContactInfoUpdate(
			@RequestBody @Validated ApiRequest<ApiTrazaSetContactInfoUpdateRequest> request) {
		log.info(this.getClass().getName() + " - " + "setContactInfoUpdate");

		ApiResponse<Provision> apiResponse;
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
				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "PSICode obligatorio", null);
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
					apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
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
					apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
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
						apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
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
						apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
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
						apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
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
						apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
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
				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, errorInternal,
						"Minimo 1 y maximo 4 datos datos de contacto", null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				return ResponseEntity.status(status).body(apiResponse);
			}

			Provision result = provisionService.setContactInfoUpdate(request.getBody());

			if (result != null) {

				status = HttpStatus.OK;
				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, String.valueOf(status.value()), status.getReasonPhrase(),
						result);

				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
				timestamp = getTimestamp();
				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "OK",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp);

			} else {
				status = HttpStatus.NOT_FOUND;
				errorInternal = InternalError.TRZ05.toString();
				errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
						.toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, errorInternal, "No existe registro", null);
				apiResponse.getHeader().setTimestamp(timestamp);
				log.info("timestamp => " + timestamp);
				log.info("MessageId => " + request.getHeader().getMessageId());
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp);

			}

		} catch (BadRequest ex) {
			System.out.println(ex.getMessage());
			status = HttpStatus.BAD_REQUEST;

			timestamp = getTimestamp();
			apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
					String.valueOf(status.value()), ex.getMessage().toString(), null);
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
			restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
					new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp);

		} catch (Exception ex) {
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
				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, errorCode,
						((FunctionalErrorException) ex).getMessage().replace("\"", ""), null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp);

			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				timestamp = getTimestamp();
				apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, String.valueOf(status.value()), ex.getMessage().toString(),
						null);
				apiResponse.getHeader().setTimestamp(timestamp);
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());

				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp);
			}
		}

		restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "OK", new Gson().toJson(request),
				new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO,
				request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp);

		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/update-customer", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<String>> apiTrazaSetContactInfoUpdate(
			@RequestBody @Validated ApiRequest<ApiTrazaSetContactInfoUpdateRequest> request) {
		log.info(this.getClass().getName() + " - " + "setContactInfoUpdate");
		

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
						// contactFullname;
						// contactCellphone;
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

			/*
			 * if(request.getBody().getContacts() instanceof List) {
			 * 
			 * status = HttpStatus.BAD_REQUEST; apiResponse = new
			 * ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
			 * Constants.OPER_CONTACT_INFO_UPDATE,
			 * String.valueOf(status.value()),"Array de contactos", null); }
			 */

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
						request.getHeader().getTimestamp(), timestamp);

			} else {
				status = HttpStatus.NOT_FOUND;
				errorInternal = InternalError.TRZ05.toString();
				errorInternal = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorInternal.replace("\"", ""))
						.toString();

				timestamp = getTimestamp();
				apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
						errorInternal, "No existe registro", null);
				apiResponse.getHeader().setTimestamp(timestamp);
				log.info("timestamp => " + timestamp);
				log.info("MessageId => " + request.getHeader().getMessageId());
				apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
				restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
						new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
						request.getHeader().getTimestamp(), timestamp);

			}

		} catch (BadRequest ex) {
			System.out.println(ex.getMessage());
			status = HttpStatus.BAD_REQUEST;

			timestamp = getTimestamp();
			apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE,
					String.valueOf(status.value()), ex.getMessage().toString(), null);
			apiResponse.getHeader().setTimestamp(timestamp);
			apiResponse.getHeader().setMessageId(request.getHeader().getMessageId());
			restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "ERROR",
					new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO, request.getHeader().getMessageId(),
					request.getHeader().getTimestamp(), timestamp);
		}

		catch (Exception ex) {
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
						request.getHeader().getTimestamp(), timestamp);

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
						request.getHeader().getTimestamp(), timestamp);
			}
		}

		restSecuritySaveLogData.saveLogData(request.getHeader().getUser(), "", "", "", "OK", new Gson().toJson(request),
				new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO,
				request.getHeader().getMessageId(), request.getHeader().getTimestamp(), timestamp);

		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * 
	 * @param provisionId
	 * @return
	 */

	@RequestMapping(value = "/requestAddressUpdate", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<List<Provision>>> requestAddressUpdate(
			@RequestBody ApiRequest<AddressUpdateRequest> request) {

		log.info(this.getClass().getName() + " - " + "requestAddressUpdate");

		// ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<>();
		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;
		try {

			Provision result = provisionService.requestAddressUpdate(request.getBody().getProvisionId());

			if (result != null) {

				List<Provision> provisions = new ArrayList<>();
				provisions.add(result);

				status = HttpStatus.OK;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_UPDATE_ADDRESS, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(provisions);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_ADDRESS, "", "", "");

			} else {

				status = HttpStatus.OK;

				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_UPDATE_ADDRESS, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(null);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "NOT_MATCH", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_ADDRESS, "", "", "");

			}

		} catch (Exception ex) {

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESS,
					String.valueOf(status.value()), ex.getMessage().toString(), null);

			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_UPDATE_ADDRESS, "", "", "");

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
		log.info(this.getClass().getName() + " - " + "receiveAddressUpdateBO error 405  ");
		log.info(this.getClass().getName() + " - " + request.toString());

		// ReceiveAddressUpdateBOResponse response = new
		// ReceiveAddressUpdateBOResponse();

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

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_UPDATE_ADDRESSBO, "", "", "");

			} else {
				status = HttpStatus.BAD_REQUEST;
				apiResponse = new ApiResponse<Object>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESSBO,
						String.valueOf(status.value()), status.getReasonPhrase(), null);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_ADDRESSBO, "", "", "");

			}

		} catch (Exception ex) {

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Object>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESSBO,
					String.valueOf(status.value()), ex.getMessage(), null);

			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_UPDATE_ADDRESSBO, "", "", "");

		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * 
	 * @param provisionId
	 * @return
	 */

	@RequestMapping(value = "/orderCancellation", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<List<Provision>>> orderCancellation(
			@RequestBody ApiRequest<CancelOrderRequest> request) {
		log.info(this.getClass().getName() + " - " + "orderCancellation");

		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;

		try {
			Provision result = provisionService.orderCancellation(request.getBody().getProvisionId(),
					request.getBody().getCause(), request.getBody().getDetail());

			if (result != null) {

				List<Provision> provisions = new ArrayList<>();
				provisions.add(result);

				status = HttpStatus.OK;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_ORDER_CANCELLATION, String.valueOf(status.value()), status.getReasonPhrase(),
						null);
				apiResponse.setBody(provisions);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_CANCEL, "", "", "");

			} else {

				status = HttpStatus.BAD_REQUEST;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_ORDER_CANCELLATION, String.valueOf(status.value()), status.getReasonPhrase(),
						null);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_CANCEL, "", "", "");

			}

		} catch (Exception ex) {

			if (ex instanceof FunctionalErrorException) {

				status = HttpStatus.BAD_REQUEST;
				System.out.println(ex.getMessage());

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

				// errorCode = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT +
				// errorCode.replace("\"", "")).toString();

				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_CONTACT_INFO_UPDATE, errorCode[1], ((FunctionalErrorException) ex).getMessage(),
						null);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_CANCEL, "", "", "");

			} else {

				System.out.println(ex.getMessage());

				status = HttpStatus.INTERNAL_SERVER_ERROR;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
						Constants.OPER_ORDER_CANCELLATION, String.valueOf(status.value()), ex.getMessage().toString(),
						null);

				System.out.println(apiResponse);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_CANCEL, "", "", "");

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

		// log.info("idProvision:" + request.getIdProvision());
		ProvisionResponse<Boolean> apiResponse;
		HttpStatus status = null;
		try {
			status = HttpStatus.OK;
			// log.info("Date Schedule:" + request.getScheduleDate());
			String[] scheduledDateStrArr = request.getScheduleDate().split("/");
			LocalDate scheduledDate = LocalDate.of(Integer.parseInt(scheduledDateStrArr[2]),
					Integer.parseInt(scheduledDateStrArr[1]), Integer.parseInt(scheduledDateStrArr[0]));
			apiResponse = provisionService.updateOrderSchedule(request.getIdProvision(), scheduledDate,
					request.getScheduleRange(), request.getScheduleType());
		} catch (Exception e) {
			log.info("Date Schedule:" + request.getScheduleDate());
			log.info("Error:" + e.getMessage());
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
		} catch (Exception e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<GetAllInTimeRangeResponse>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_ALL_IN_TIME_RANGE, String.valueOf(status.value()), e.getMessage(), null);
		}

		return ResponseEntity.status(status).body(apiResponse);
	}

	/*
	 * @RequestMapping(value = "/updateTrackingStatus", method = RequestMethod.POST)
	 * public ResponseEntity<ApiResponse<Boolean>> updateTrackingStatus(
	 * 
	 * @RequestBody @Valid ApiRequest<UpdateStatusRequest> request) {
	 * 
	 * ApiResponse<Boolean> apiResponse; StatusProvision statusProvision = null;
	 * boolean updated = false; HttpStatus status;
	 * 
	 * try { if (request.getBody().getXaRequest() != null &&
	 * request.getBody().getXaIdSt() != null) { statusProvision = new
	 * StatusProvision();
	 * statusProvision.setXaRequest(request.getBody().getXaRequest());
	 * statusProvision.setXaIdSt(request.getBody().getXaIdSt());
	 * statusProvision.setLabel(request.getBody().getStatus()); } else if
	 * (request.getBody().getStatus() != null) { switch
	 * (request.getBody().getStatus().toUpperCase()) { case "WO_PRESTART":
	 * WoPreStartProvision woPreStartProvision = new WoPreStartProvision();
	 * woPreStartProvision.mapObject(request.getBody().getStringSource());
	 * statusProvision = woPreStartProvision; break; case "WO_INIT": WoInitProvision
	 * woInitProvision = new WoInitProvision();
	 * woInitProvision.mapObject(request.getBody().getStringSource());
	 * statusProvision = woInitProvision; break; case "WO_COMPLETED":
	 * WoCompletedProvision woCompletedProvision = new WoCompletedProvision();
	 * woCompletedProvision.mapObject(request.getBody().getStringSource());
	 * statusProvision = woCompletedProvision; break; default: break; } }
	 * 
	 * if (statusProvision != null) { updated =
	 * provisionService.updateTrackingStatus(statusProvision.getXaRequest(),
	 * statusProvision.getXaIdSt(), request.getBody().getStatus(),
	 * false,null,null,null);
	 * 
	 * status = updated ? HttpStatus.OK : HttpStatus.NOT_FOUND; } else { status =
	 * HttpStatus.BAD_REQUEST; }
	 * 
	 * apiResponse = new ApiResponse<Boolean>(Constants.APP_NAME_PROVISION,
	 * Constants.OPER_UPDATE_STATUS, String.valueOf(status.value()),
	 * status.getReasonPhrase(), updated); } catch (Exception e) { status =
	 * HttpStatus.INTERNAL_SERVER_ERROR; apiResponse = new
	 * ApiResponse<Boolean>(Constants.APP_NAME_PROVISION,
	 * Constants.OPER_UPDATE_STATUS, String.valueOf(status.value()), e.getMessage(),
	 * null); }
	 * 
	 * return ResponseEntity.status(status).body(apiResponse); }
	 */

	@RequestMapping(value = "/getProvisionByOrderCode", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<Provision>> getProvisionByOrderCode(
			@RequestBody ApiRequest<GetProvisionByOrderCodeRequest> request) {

		ApiResponse<Provision> apiResponse;
		Provision provision = null;
		HttpStatus status;

		try {

			provision = provisionService.getProvisionByOrderCode(request);
			// apiResponse.setBody(provision);

			status = HttpStatus.OK;
			apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_BY_ORDER_CODE, String.valueOf(status.value()),
					status.getReasonPhrase(), provision);

			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_GET_BY_ORDER_CODE, "", "", "");

		} catch (Exception e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Provision>(Constants.APP_NAME_PROVISION,
					Constants.OPER_GET_PROVISION_BY_ORDER_CODE, String.valueOf(status.value()), e.getMessage(), null);

			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_GET_BY_ORDER_CODE, "", "", "");
		}

		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/getContacts", method = RequestMethod.GET)
	public ResponseEntity<ProvisionResponse<List<Contacts>>> getContacts(
			@RequestParam(value = "provisionId", required = true) String provisionId) {
		return ResponseEntity.ok(provisionService.getContactList(provisionId));
	}

	public String getTimestamp() {
		log.info("ProvisionController.getTimestamp()");
		LocalDateTime dateNow = LocalDateTime.now(ZoneOffset.of("-05:00"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.S");
		String timeStamp = dateNow.format(formatter);
		log.info("timeStamp => " + timeStamp);
		return timeStamp;
	}

	public Object[] validateActivityType(String[] parts) {
		Object[] obj = new Object[3];
		String status = parts[0] == null ? "" : parts[0], xaRequest = "", xaRequirementNumber = "";
		boolean provision = false;
		if (Constants.STATUS_WO_INIT.equalsIgnoreCase(status)) {
			if (Constants.ACTIVITY_TYPE_PROVISION.equalsIgnoreCase(parts[14])) {
				xaRequest = parts[5].trim() == "0" ? parts[8].trim() : parts[5].trim();
				provision = true;
				xaRequirementNumber = parts[8].trim();
			} else {
				provision = false;
				xaRequest = "";
				xaRequirementNumber = "";
			}
		} else if (Constants.STATUS_WO_COMPLETED.equalsIgnoreCase(status)) {
			if (Constants.ACTIVITY_TYPE_PROVISION.equalsIgnoreCase(parts[13])) {
				xaRequest = parts[6].trim() == "0" ? parts[9].trim() : parts[6].trim();
				provision = true;
				xaRequirementNumber = parts[9].trim();
			} else {
				provision = false;
				xaRequest = "";
				xaRequirementNumber = "";
			}
		} else if (Constants.STATUS_WO_NOTDONE.equalsIgnoreCase(status)) {
			if (Constants.ACTIVITY_TYPE_PROVISION.equalsIgnoreCase(parts[14])) {
				xaRequest = parts[7].trim() == "0" ? parts[10].trim() : parts[7].trim();
				provision = true;
				xaRequirementNumber = parts[10].trim();
			} else {
				provision = false;
				xaRequest = "";
				xaRequirementNumber = "";
			}
		} else if (Constants.STATUS_WO_PRESTART.equalsIgnoreCase(status)) {
			if (Constants.ACTIVITY_TYPE_PROVISION.equalsIgnoreCase(parts[5])) {
				xaRequest = parts[2].trim() == "0" ? parts[7].trim() : parts[2].trim();
				provision = true;
				xaRequirementNumber = parts[7].trim();
			} else {
				provision = false;
				xaRequest = "";
				xaRequirementNumber = "";
			}
		} else if (Constants.STATUS_IN_TOA.equalsIgnoreCase(status)) {
			if (Constants.ACTIVITY_TYPE_PROVISION.equalsIgnoreCase(parts[8])) {
				xaRequest = parts[2].trim() == "0" ? parts[5].trim() : parts[2].trim();
				provision = true;
				xaRequirementNumber = parts[5].trim();
			} else {
				provision = false;
				xaRequest = "";
				xaRequirementNumber = "";
			}
		} else if (Constants.STATUS_WO_RESCHEDULE.equalsIgnoreCase(status)) {
			if (Constants.ACTIVITY_TYPE_PROVISION.equalsIgnoreCase(parts[8])) {
				xaRequest = parts[2].trim() == "0" ? parts[5].trim() : parts[2].trim();
				provision = true;
				xaRequirementNumber = parts[5].trim();
			} else {
				provision = false;
				xaRequest = "";
				xaRequirementNumber = "";
			}
		} else if (Constants.STATUS_WO_CANCEL.equalsIgnoreCase(status)) {
			if (Constants.ACTIVITY_TYPE_PROVISION.equalsIgnoreCase(parts[8])) {
				xaRequest = parts[2].trim() == "0" ? parts[5].trim() : parts[2].trim();
				provision = true;
				xaRequirementNumber = parts[5].trim();
			} else {
				provision = false;
				xaRequest = "";
				xaRequirementNumber = "";
			}
		} else {
			provision = false;
			xaRequest = "";
			xaRequirementNumber = "";
		}
		
		obj[0] = provision;
		obj[1] = xaRequest;
		obj[2] = xaRequirementNumber;
		return obj;
	}
	
	@RequestMapping(value = "/getOrderToNotify", method = RequestMethod.GET)
	public ResponseEntity<ApiResponse<List<Provision>>> getOrderToNotify() {
		log.info("ProvisionController.getOrderToNotify()");
		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;
		
		List<Provision> provisions = provisionService.getOrderToNotify();

		status = HttpStatus.OK;
		apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION,
				Constants.OPER_GET_ORDER_TO_NOTIFY, String.valueOf(status.value()), status.getReasonPhrase(),
				provisions);
		
		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/updateShowLocation", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<String>> updateShowLocation(
			@RequestBody @Valid ApiRequest<ProvisionRequest> request) {

		Provision provision = new Provision();
		provision.setIdProvision(request.getBody().getIdProvision());
		
		ApiResponse<String> apiResponse;
		HttpStatus status;

		try {
			boolean estado = provisionService.updateShowLocation(provision);
			
			if (estado) {

				status = HttpStatus.OK;
				apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_SHOW_LOCATION,
						String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody("OK");

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "OK", new Gson().toJson(request), new Gson().toJson(apiResponse),
						ConstantsLogData.PROVISION_VALIDATE_USER, "", "", "");
			} else {

				status = HttpStatus.OK;

				apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_SHOW_LOCATION,
						String.valueOf(status.value()), "No se encontraron datos del cliente", null);
				apiResponse.setBody(null);

				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
						request.getBody().getDocumentType(), request.getBody().getOrderCode(),
						request.getBody().getBucket(), "NOT_MATCH", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_VALIDATE_USER, "", "", "");
			}

		} catch (Exception ex) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<String>(Constants.APP_NAME_PROVISION, Constants.OPER_SHOW_LOCATION,
					String.valueOf(status.value()), ex.getMessage(), null);

			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(),
					request.getBody().getDocumentType(), request.getBody().getOrderCode(),
					request.getBody().getBucket(), "ERROR", new Gson().toJson(request), new Gson().toJson(apiResponse),
					ConstantsLogData.PROVISION_VALIDATE_USER, "", "", "");
		}

		return ResponseEntity.status(status).body(apiResponse);
	}
	
}