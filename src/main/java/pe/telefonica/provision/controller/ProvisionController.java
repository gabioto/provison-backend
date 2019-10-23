package pe.telefonica.provision.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import pe.telefonica.provision.controller.common.ApiRequest;
import pe.telefonica.provision.controller.common.ApiResponse;
import pe.telefonica.provision.controller.request.AddressUpdateRequest;
import pe.telefonica.provision.controller.request.CancelOrderRequest;
import pe.telefonica.provision.controller.request.GetAllInTimeRangeRequest;
import pe.telefonica.provision.controller.request.ProvisionRequest;
import pe.telefonica.provision.controller.request.ReceiveAddressUpdateBORequest;
import pe.telefonica.provision.controller.request.SetContactInfoUpdateRequest;
import pe.telefonica.provision.controller.request.ValidateDataRequest;
import pe.telefonica.provision.controller.response.GetAllInTimeRangeResponse;
import pe.telefonica.provision.controller.response.ProvisionArrayResponse;
import pe.telefonica.provision.controller.response.ProvisionHeaderResponse;
import pe.telefonica.provision.controller.response.ProvisionResponse;
import pe.telefonica.provision.controller.response.ReceiveAddressUpdateBOResponse;
import pe.telefonica.provision.controller.common.ResponseHeader;
import pe.telefonica.provision.model.Customer;
import pe.telefonica.provision.model.Provision;
import pe.telefonica.provision.external.TrazabilidadSecurityApi;
import pe.telefonica.provision.service.ProvisionService;
import pe.telefonica.provision.util.constants.Constants;
import pe.telefonica.provision.util.constants.ConstantsLogData;
import pe.telefonica.provision.util.constants.ErrorCode;
import pe.telefonica.provision.util.exception.FunctionalErrorException;
import pe.telefonica.provision.util.exception.ServerNotFoundException;

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
				apiResponse = new ApiResponse<Customer>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_USER, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(customer);
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "OK", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_VALIDATE_USER);
			} else {
				
				status = HttpStatus.OK;
				
				apiResponse = new ApiResponse<Customer>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_USER, String.valueOf(status.value()), "No se encontraron datos del cliente", null);
				apiResponse.setBody(null);
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "NOT_MATCH", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_VALIDATE_USER);
				
			}
			
			
			
		} catch (Exception ex) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Customer>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_USER, String.valueOf(status.value()), ex.getMessage(), null);
			
			
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
					request.getBody().getOrderCode(), request.getBody().getBucket(),  "ERROR", new Gson().toJson(request),
					new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_VALIDATE_USER);
			
		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * 
	 * @param provisionRequest
	 * @return ProvisionResponse<Provision>
	 * @description get all provisions related to type and number of the document
	 */
	
	@RequestMapping(value = "/getOrders", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<List<Provision>>> getOrders(
			@RequestBody ApiRequest<ProvisionRequest> request) {
			
		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;
		
			try {
				List<Provision> provisions = provisionService.getAll(request);
				
				if (provisions != null) {
				
					status = HttpStatus.OK;
					apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()), status.getReasonPhrase(), null);
					apiResponse.setBody(provisions);
					
					restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
							request.getBody().getOrderCode(), request.getBody().getBucket(),  "OK", new Gson().toJson(request),
							new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_GET_PROVISION_ALL);
					
				} else {
					status = HttpStatus.NOT_FOUND;
					apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()), "No se encontraron provisiones", null);
					apiResponse.setBody(provisions);
					
					restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
							request.getBody().getOrderCode(), request.getBody().getBucket(),  "ERROR", new Gson().toJson(request),
							new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_GET_PROVISION_ALL);
				}
				
			} catch (Exception ex) {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()), ex.getMessage().toString(), null);
				
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_GET_PROVISION_ALL);
			}
			return ResponseEntity.status(status).body(apiResponse);
		//return ResponseEntity.ok(provisionService.getAll(new ProvisionRequest(documentType, documentNumber)));
		
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

	/**
	 * 
	 * @param provisionListReq
	 * @return ProvisionResponse<Provision>
	 * @description insert a list of provisions
	 */
	/*@RequestMapping(value = "/insertOrders", method = RequestMethod.POST)
	public ResponseEntity<ProvisionArrayResponse<Provision>> insertOrders(
			@RequestBody @Valid List<Provision> provisionListReq) {
		
		return ResponseEntity.ok(provisionService.insertProvisionList(provisionListReq));
	}*/
	@RequestMapping(value = "/insertOrders", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<List<Provision>>> insertOrders(
			@RequestBody @Valid ApiRequest<List<Provision>> provisionListReq) {
		log.info(this.getClass().getName() + " - " + "insertOrders: " + provisionListReq.toString());
		
		log.info(this.getClass().getName() + " - " + "provisionListReq: " + provisionListReq.toString());
		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;
		
		try {
			List<Provision> provisions = provisionService.insertProvisionList(provisionListReq.getBody());
			
			if (provisions != null) {
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_INSERT_PROVISION, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(provisions);
			} else {
				status = HttpStatus.NOT_FOUND;
				
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_INSERT_PROVISION, String.valueOf(status.value()), "No se encontraron provisiones", null);
				apiResponse.setBody(null);
			}
		} catch (Exception ex) {
			
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_GET_PROVISION_ALL, String.valueOf(status.value()), ex.getMessage().toString(), null);
			
		}
		return ResponseEntity.status(status).body(apiResponse);
		//return ResponseEntity.ok(provisionService.insertProvisionList(provisionListReq));
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
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_DATA, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(provisions);
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "OK", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_VALIDATE_DATA);
				
				
				/*List<Provision> provisions = new ArrayList<>();
				provisions.add(result);
				response.setHeader(
						new ProvisionHeaderResponse().generateHeader(HttpStatus.OK.value(), HttpStatus.OK.name()));
				response.setData(provisions);

				restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
						request.getOrderCode(), request.getBucket(), "OK", new Gson().toJson(request),
						new Gson().toJson(response), ConstantsLogData.PROVISION_VALIDATE_DATA);*/

				//return ResponseEntity.ok(response);
			} else {
				status = HttpStatus.BAD_REQUEST;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_DATA, String.valueOf(status.value()), status.getReasonPhrase(), null);
				
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_VALIDATE_DATA);
				
				/*
				response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
						HttpStatus.BAD_REQUEST.name()));

				restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
						request.getOrderCode(), request.getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(response), ConstantsLogData.PROVISION_VALIDATE_DATA);

				return ResponseEntity.badRequest().body(response);*/
			}
		} catch (Exception ex) {
			
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_VALIDATE_DATA, String.valueOf(status.value()), ex.getMessage().toString(), null);
			
			
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
					request.getBody().getOrderCode(), request.getBody().getBucket(),  "ERROR", new Gson().toJson(request),
					new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_VALIDATE_DATA);
			
			/*restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
					request.getOrderCode(), request.getBucket(), "ERROR", new Gson().toJson(request),
					new Gson().toJson(ex.getMessage()), ConstantsLogData.PROVISION_VALIDATE_DATA);

			response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.name()));
			return ResponseEntity.badRequest().body(response);*/
		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/setContactInfoUpdate", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<List<Provision>>> setContactInfoUpdate(
			@RequestBody ApiRequest<SetContactInfoUpdateRequest> request) {
		log.info(this.getClass().getName() + " - " + "setContactInfoUpdate");

		
		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;
		try {
			
			/*Provision provision = provisionService.setContactInfoUpdateNew(request);
		
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
					request.getBody().getOrderCode(), request.getBody().getBucket(), response.getHeader().getResultCode().equals("200") ? "OK": "ERROR", new Gson().toJson(request),
					new Gson().toJson(response), ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO);
			
			return ResponseEntity.ok(response);*/
			
			Provision result = provisionService.setContactInfoUpdate(request.getBody().getProvisionId(),
					request.getBody().getContactFullname(), request.getBody().getContactCellphone(),
					request.getBody().getContactCellphoneIsMovistar());

			if (result != null) {
				
				List<Provision> provisions = new ArrayList<>();
				provisions.add(result);
				
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(provisions);
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "OK", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO);

				
			} else {
				status = HttpStatus.BAD_REQUEST;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE, String.valueOf(status.value()), "No existe registro", null);
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO);
	
			}
		} catch (Exception ex) {
			
			
			if(ex instanceof FunctionalErrorException ) {
				
				status = HttpStatus.BAD_REQUEST;
				
				String errorCode = ((FunctionalErrorException) ex).getErrorCode().replace("\"", "");
				if(errorCode.equals("ERR10") || errorCode.equals("ERR11") || errorCode.equals("ERR02") ) {
					status = HttpStatus.BAD_REQUEST;
				} else if(errorCode.equals("ERR15") ) {
					status = HttpStatus.UNAUTHORIZED;
				}else if(errorCode.equals("ERR03") ) {
					status = HttpStatus.NOT_FOUND;
				}else if(errorCode.equals("ERR19") ) {
					status = HttpStatus.CONFLICT;
				} 
				
				errorCode = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorCode.replace("\"", "")).toString();
			
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE, errorCode, ((FunctionalErrorException) ex).getMessage().replace("\"", ""), null);
				
				
			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE, String.valueOf(status.value()), ex.getMessage().toString(), null);
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO);		
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
	public ResponseEntity<ApiResponse<List<Provision>>> requestAddressUpdate(
			@RequestBody ApiRequest<AddressUpdateRequest> request) {

		log.info(this.getClass().getName() + " - " + "requestAddressUpdate");
		
		
		//ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<>();
		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;
		try {

			Provision result = provisionService.requestAddressUpdate(request.getBody().getProvisionId());

			if (result != null) {
				
				List<Provision> provisions = new ArrayList<>();
				provisions.add(result);
				
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESS, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(provisions);
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "OK", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_ADDRESS);

				
				
			} else {
				
				status = HttpStatus.OK;
				
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESS, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(null);
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "NOT_MATCH", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_ADDRESS);
				
				
			}

		} catch (Exception ex) {
			
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESS, String.valueOf(status.value()), ex.getMessage().toString(), null);
			
			
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
					request.getBody().getOrderCode(), request.getBody().getBucket(),  "ERROR", new Gson().toJson(request),
					new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_ADDRESS);
			
			
			
		}
		return ResponseEntity.status(status).body(apiResponse);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
		
	@RequestMapping(value = "/receiveAddressUpdateBO", method = RequestMethod.PUT)
	public ResponseEntity<ApiResponse<Object>> receiveAddressUpdateBO(
			@RequestBody ApiRequest<ReceiveAddressUpdateBORequest> request) {
		log.info(this.getClass().getName() + " - " + "receiveAddressUpdateBO");
		log.info(this.getClass().getName() + " - " + request.toString());
		
		

		//ReceiveAddressUpdateBOResponse response = new ReceiveAddressUpdateBOResponse();
		
		ApiResponse<Object> apiResponse;
		HttpStatus status;
		
		try {
			
			Boolean result = provisionService.receiveAddressUpdateBO(request.getBody().getAction(), request.getBody().getProvisionId(),
					request.getBody().getNewDepartment(), request.getBody().getNewProvince(), request.getBody().getNewDistrict(), request.getBody().getNewAddress(),
					request.getBody().getNewReference(), request.getBody().getIsSMSRequired());
		
			if (result) {
				
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<Object>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESSBO, String.valueOf(status.value()), status.getReasonPhrase(), null);
				
				apiResponse.setBody(null);
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "OK", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_ADDRESSBO);
				
			} else {
				status = HttpStatus.BAD_REQUEST;
				apiResponse = new ApiResponse<Object>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESSBO, String.valueOf(status.value()), status.getReasonPhrase(), null);
				
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_ADDRESSBO);
				
			}
			
		} catch (Exception ex) {
			
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<Object>(Constants.APP_NAME_PROVISION, Constants.OPER_UPDATE_ADDRESSBO, String.valueOf(status.value()), ex.getMessage(), null);
			
			
			restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
					request.getBody().getOrderCode(), request.getBody().getBucket(),  "ERROR", new Gson().toJson(request),
					new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_UPDATE_ADDRESSBO);
			
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

		//ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<>();
		
		ApiResponse<List<Provision>> apiResponse;
		HttpStatus status;
		
		try {
			Provision result = provisionService.orderCancellation(request.getBody().getProvisionId());

			if (result != null) {
				
				List<Provision> provisions = new ArrayList<>();
				provisions.add(result);
				
				status = HttpStatus.OK;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_ORDER_CANCELLATION, String.valueOf(status.value()), status.getReasonPhrase(), null);
				apiResponse.setBody(provisions);
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "OK", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_CANCEL);
				
				
				
			} else {
				
				
				status = HttpStatus.BAD_REQUEST;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_ORDER_CANCELLATION, String.valueOf(status.value()), status.getReasonPhrase(), null);
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_CANCEL);
				
				
				
			}

		} catch (Exception ex) {
			
			if(ex instanceof FunctionalErrorException ) {
				
				status = HttpStatus.BAD_REQUEST;
				System.out.println(ex.getMessage());
				
				String errorCode[] = ((FunctionalErrorException) ex).getErrorCode().split("_");
				Integer htttCode = Integer.parseInt(errorCode[0]);
				
				if(htttCode.equals(400)) {
					status = HttpStatus.BAD_REQUEST;
				} else if(htttCode.equals(401) ) {
					status = HttpStatus.UNAUTHORIZED;
				}else if(htttCode.equals(404) ) {
					status = HttpStatus.NOT_FOUND;
				}else if(htttCode.equals(409) ) {
					status = HttpStatus.CONFLICT;
				} 
				
				//errorCode = ErrorCode.get(Constants.PSI_CODE_UPDATE_CONTACT + errorCode.replace("\"", "")).toString();
			
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_CONTACT_INFO_UPDATE, errorCode[1], ((FunctionalErrorException) ex).getMessage(), null);
				
				
			} else {
						
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				apiResponse = new ApiResponse<List<Provision>>(Constants.APP_NAME_PROVISION, Constants.OPER_ORDER_CANCELLATION, String.valueOf(status.value()), ex.getMessage().toString(), null);
				
				
				restSecuritySaveLogData.saveLogData(request.getBody().getDocumentNumber(), request.getBody().getDocumentType(),
						request.getBody().getOrderCode(), request.getBody().getBucket(),  "ERROR", new Gson().toJson(request),
						new Gson().toJson(apiResponse), ConstantsLogData.PROVISION_CANCEL);
			
			}
		}
		
		return ResponseEntity.status(status).body(apiResponse);
	}

	@RequestMapping(value = "/validateContingency", method = RequestMethod.GET)
	public ResponseEntity<ProvisionResponse<Boolean>> getOrderStatus() {
		return ResponseEntity.ok(provisionService.validateQueue());
	}

	@RequestMapping(value = "/updateOrderSchedule", method = RequestMethod.PUT)
	public ResponseEntity<ProvisionResponse<Boolean>> updateOrderSchedule(
			@RequestParam(value = "provisionId", required = true) String provisionId) {
		return ResponseEntity.ok(provisionService.updateOrderSchedule(provisionId));
	}
	
	
	@RequestMapping(value = "/getAllInTimeRange", method = RequestMethod.POST)
	public ResponseEntity<ApiResponse<GetAllInTimeRangeResponse>> getAllInTimeRange(@RequestBody ApiRequest<GetAllInTimeRangeRequest> request) {
		
		ApiResponse<GetAllInTimeRangeResponse> apiResponse;
		HttpStatus status;
		
		try {
			String[] startDateStrArr = request.getBody().getStartDateStr().split("-");
			String[] endDateStrArr = request.getBody().getEndDateStr().split("-");
			//year, month, day, hour, minutes, seconds
			LocalDateTime startDate = LocalDateTime.of(Integer.parseInt(startDateStrArr[0]), Integer.parseInt(startDateStrArr[1]), Integer.parseInt(startDateStrArr[2]), 0, 0, 0);
			LocalDateTime endDate = LocalDateTime.of(Integer.parseInt(endDateStrArr[0]), Integer.parseInt(endDateStrArr[1]), Integer.parseInt(endDateStrArr[2]), 23, 59, 59);
					
			List<Provision> provisions = provisionService.getAllInTimeRange(startDate, endDate);
			GetAllInTimeRangeResponse response = new GetAllInTimeRangeResponse();
			response.setProvisions(provisions);
			
			status = HttpStatus.OK;
			apiResponse = new ApiResponse<GetAllInTimeRangeResponse>(Constants.APP_NAME_PROVISION, Constants.OPER_GET_ALL_IN_TIME_RANGE, String.valueOf(status.value()), status.getReasonPhrase(), response);
		}catch(Exception e) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			apiResponse = new ApiResponse<GetAllInTimeRangeResponse>(Constants.APP_NAME_PROVISION, Constants.OPER_GET_ALL_IN_TIME_RANGE, String.valueOf(status.value()), e.getMessage(), null);
		}
		
		return ResponseEntity.status(status).body(apiResponse);
	}
}
