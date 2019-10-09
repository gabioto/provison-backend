package pe.telefonica.provision.api;

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

import pe.telefonica.provision.api.request.AddressUpdateRequest;
import pe.telefonica.provision.api.request.CancelOrderRequest;
import pe.telefonica.provision.api.request.ProvisionRequest;
import pe.telefonica.provision.api.request.ReceiveAddressUpdateBORequest;
import pe.telefonica.provision.api.request.SetContactInfoUpdateRequest;
import pe.telefonica.provision.api.request.ValidateDataRequest;
import pe.telefonica.provision.api.response.ProvisionArrayResponse;
import pe.telefonica.provision.api.response.ProvisionHeaderResponse;
import pe.telefonica.provision.api.response.ProvisionResponse;
import pe.telefonica.provision.api.response.ReceiveAddressUpdateBOResponse;
import pe.telefonica.provision.api.response.ResponseHeader;
import pe.telefonica.provision.dto.Customer;
import pe.telefonica.provision.dto.Provision;
import pe.telefonica.provision.restclient.RestSecuritySaveLogData;
import pe.telefonica.provision.service.ProvisionService;
import pe.telefonica.provision.util.ConstantsLogData;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("provision")
public class ProvisionApi {

	private static final Log log = LogFactory.getLog(ProvisionApi.class);

	@Autowired
	ProvisionService contactService;

	@Autowired
	RestSecuritySaveLogData restSecuritySaveLogData;

	private final ProvisionService provisionService;

	@Autowired
	public ProvisionApi(ProvisionService provisionService) {
		this.provisionService = provisionService;
	}

	@RequestMapping(value = "/getCustomerByDocument", method = RequestMethod.POST)
	public ResponseEntity<ProvisionResponse<Customer>> getCustomerByDocument(
			@RequestBody @Valid ProvisionRequest provisionRequest) {
		return ResponseEntity.ok(provisionService.validateUser(provisionRequest));
	}

	/**
	 * 
	 * @param provisionRequest
	 * @return ProvisionResponse<Provision>
	 * @description get all provisions related to type and number of the document
	 */
	@RequestMapping(value = "/getOrders", method = RequestMethod.GET)
	public ResponseEntity<ProvisionArrayResponse<Provision>> getOrders(
			@RequestParam(value = "documentType", required = true) String documentType,
			@RequestParam(value = "documentNumber", required = true) String documentNumber) {
		return ResponseEntity.ok(provisionService.getAll(new ProvisionRequest(documentType, documentNumber)));
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
	@RequestMapping(value = "/insertOrders", method = RequestMethod.POST)
	public ResponseEntity<ProvisionArrayResponse<Provision>> insertOrders(
			@RequestBody @Valid List<Provision> provisionListReq) {
		return ResponseEntity.ok(provisionService.insertProvisionList(provisionListReq));
	}

	/**
	 * 
	 * @param provisionId
	 * @return
	 */
	@RequestMapping(value = "/setProvisionValidatedOLD", method = RequestMethod.POST)
	public ResponseEntity<ProvisionArrayResponse<Provision>> setProvisionValidatedOLD(
			@RequestParam(value = "provisionId", required = true) String provisionId) {
		log.info(this.getClass().getName() + " - " + "setProvisionValidated devops");
		ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<>();
		Provision result = provisionService.setProvisionIsValidated(provisionId);

		if (result != null) {
			List<Provision> provisions = new ArrayList<>();
			provisions.add(result);
			response.setHeader(
					new ProvisionHeaderResponse().generateHeader(HttpStatus.OK.value(), HttpStatus.OK.name()));
			response.setData(provisions);
			return ResponseEntity.ok(response);
		} else {
			response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.name()));
			return ResponseEntity.badRequest().body(response);
		}
	}

	@RequestMapping(value = "/setProvisionValidated", method = RequestMethod.POST)
	public ResponseEntity<ProvisionArrayResponse<Provision>> setProvisionValidated(
			@RequestBody ValidateDataRequest request) {
		log.info(this.getClass().getName() + " - " + "setProvisionValidated");

		ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<>();

		try {
			Provision result = provisionService.setProvisionIsValidated(request.getProvisionId());

			if (result != null) {
				List<Provision> provisions = new ArrayList<>();
				provisions.add(result);
				response.setHeader(
						new ProvisionHeaderResponse().generateHeader(HttpStatus.OK.value(), HttpStatus.OK.name()));
				response.setData(provisions);

				restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
						request.getOrderCode(), request.getBucket(), "OK", new Gson().toJson(request),
						new Gson().toJson(response), ConstantsLogData.PROVISION_VALIDATE_DATA);

				return ResponseEntity.ok(response);
			} else {
				response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
						HttpStatus.BAD_REQUEST.name()));

				restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
						request.getOrderCode(), request.getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(response), ConstantsLogData.PROVISION_VALIDATE_DATA);

				return ResponseEntity.badRequest().body(response);
			}
		} catch (Exception ex) {
			restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
					request.getOrderCode(), request.getBucket(), "ERROR", new Gson().toJson(request),
					new Gson().toJson(ex.getMessage()), ConstantsLogData.PROVISION_VALIDATE_DATA);

			response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.name()));
			return ResponseEntity.badRequest().body(response);
		}

	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/setContactInfoUpdate", method = RequestMethod.POST)
	public ResponseEntity<ProvisionArrayResponse<Provision>> setContactInfoUpdate(
			@RequestBody SetContactInfoUpdateRequest request) {
		log.info(this.getClass().getName() + " - " + "setContactInfoUpdate");

		ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<>();

		try {

			Provision result = provisionService.setContactInfoUpdate(request.getProvisionId(),
					request.getContactFullname(), request.getContactCellphone(),
					request.getContactCellphoneIsMovistar());

			if (result != null) {
				List<Provision> provisions = new ArrayList<>();
				provisions.add(result);
				response.setHeader(
						new ProvisionHeaderResponse().generateHeader(HttpStatus.OK.value(), HttpStatus.OK.name()));
				response.setData(provisions);

				restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
						request.getOrderCode(), request.getBucket(), "OK", new Gson().toJson(request),
						new Gson().toJson(response), ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO);

				return ResponseEntity.ok(response);
			} else {
				response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
						HttpStatus.BAD_REQUEST.name()));

				restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
						request.getOrderCode(), request.getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(response), ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO);

				return ResponseEntity.badRequest().body(response);
			}
		} catch (Exception ex) {

			restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
					request.getOrderCode(), request.getBucket(), "ERROR", new Gson().toJson(request),
					new Gson().toJson(ex.getMessage()), ConstantsLogData.PROVISION_UPDATE_CONTACT_INFO);

			response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.name()));
			return ResponseEntity.badRequest().body(response);
		}

	}

	/**
	 * 
	 * @param provisionId
	 * @return
	 */
	@RequestMapping(value = "/requestAddressUpdateOLD", method = RequestMethod.POST)
	public ResponseEntity<ProvisionArrayResponse<Provision>> requestAddressUpdateOLD(
			@RequestParam(value = "provisionId", required = true) String provisionId) {
		log.info(this.getClass().getName() + " - " + "requestAddressUpdate");
		ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<>();

		try {

			Provision result = provisionService.requestAddressUpdate(provisionId);

			if (result != null) {
				List<Provision> provisions = new ArrayList<>();
				provisions.add(result);
				response.setHeader(
						new ProvisionHeaderResponse().generateHeader(HttpStatus.OK.value(), HttpStatus.OK.name()));
				response.setData(provisions);

				restSecuritySaveLogData.saveLogData(result.getCustomer().getDocumentNumber(),
						result.getCustomer().getDocumentType(), result.getOrderCode(), "bucket", "OK",
						provisionId.toString(), new Gson().toJson(response), ConstantsLogData.PROVISION_VALIDATE_DATA);

				return ResponseEntity.ok(response);
			} else {
				response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
						HttpStatus.BAD_REQUEST.name()));

				restSecuritySaveLogData.saveLogData(result.getCustomer().getDocumentNumber(),
						result.getCustomer().getDocumentType(), result.getOrderCode(), "bucket", "ERROR",
						provisionId.toString(), new Gson().toJson(response), ConstantsLogData.PROVISION_VALIDATE_DATA);

				return ResponseEntity.badRequest().body(response);
			}

		} catch (Exception ex) {

			response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.name()));

			restSecuritySaveLogData.saveLogData("", "", "", "bucket", "ERROR", provisionId.toString(),
					new Gson().toJson(ex.getMessage()), ConstantsLogData.PROVISION_VALIDATE_DATA);

			return ResponseEntity.badRequest().body(response);
		}

	}

	@RequestMapping(value = "/requestAddressUpdate", method = RequestMethod.POST)
	public ResponseEntity<ProvisionArrayResponse<Provision>> requestAddressUpdate(
			@RequestBody AddressUpdateRequest request) {

		log.info(this.getClass().getName() + " - " + "requestAddressUpdate");

		ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<>();

		try {

			Provision result = provisionService.requestAddressUpdate(request.getProvisionId());

			if (result != null) {
				List<Provision> provisions = new ArrayList<>();
				provisions.add(result);
				response.setHeader(
						new ProvisionHeaderResponse().generateHeader(HttpStatus.OK.value(), HttpStatus.OK.name()));
				response.setData(provisions);

				restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
						request.getOrderCode(), request.getBucket(), "OK", new Gson().toJson(request),
						new Gson().toJson(response), ConstantsLogData.PROVISION_UPDATE_ADDRESS);

				return ResponseEntity.ok(response);
			} else {
				response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
						HttpStatus.BAD_REQUEST.name()));

				restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
						request.getOrderCode(), request.getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(response), ConstantsLogData.PROVISION_UPDATE_ADDRESS);

				return ResponseEntity.badRequest().body(response);
			}

		} catch (Exception ex) {

			restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
					request.getOrderCode(), request.getBucket(), "ERROR", new Gson().toJson(request),
					new Gson().toJson(ex.getMessage()), ConstantsLogData.PROVISION_UPDATE_ADDRESS);

			response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.name()));

			return ResponseEntity.badRequest().body(response);
		}

	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/receiveAddressUpdateBO", method = RequestMethod.PUT)
	public ResponseEntity<ReceiveAddressUpdateBOResponse> receiveAddressUpdateBO(
			@RequestBody ReceiveAddressUpdateBORequest request) {
		log.info(this.getClass().getName() + " - " + "receiveAddressUpdateBO");
		log.info(this.getClass().getName() + " - " + request.toString());
		Boolean result = provisionService.receiveAddressUpdateBO(request.getAction(), request.getProvisionId(),
				request.getNewDepartment(), request.getNewProvince(), request.getNewDistrict(), request.getNewAddress(),
				request.getNewReference(), request.getIsSMSRequired());

		ReceiveAddressUpdateBOResponse response = new ReceiveAddressUpdateBOResponse();
		response.setResult(result);

		if (result) {
			response.setHeader(new ResponseHeader().generateHeader("ok", "ok"));
			return ResponseEntity.ok(response);
		} else {
			response.setHeader(new ResponseHeader().generateHeader("err", "error"));
			return ResponseEntity.badRequest().body(response);
		}
	}

	/**
	 * 
	 * @param provisionId
	 * @return
	 */
	@RequestMapping(value = "/orderCancellationOLD", method = RequestMethod.POST)
	public ResponseEntity<ProvisionArrayResponse<Provision>> orderCancellationOLD(
			@RequestParam(value = "provisionId", required = true) String provisionId) {
		log.info(this.getClass().getName() + " - " + "orderCancellation");
		ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<>();
		Provision result = provisionService.orderCancellation(provisionId);

		if (result != null) {
			List<Provision> provisions = new ArrayList<>();
			provisions.add(result);
			response.setHeader(
					new ProvisionHeaderResponse().generateHeader(HttpStatus.OK.value(), HttpStatus.OK.name()));
			response.setData(provisions);
			return ResponseEntity.ok(response);
		} else {
			response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.name()));
			return ResponseEntity.badRequest().body(response);
		}
	}

	@RequestMapping(value = "/orderCancellation", method = RequestMethod.POST)
	public ResponseEntity<ProvisionArrayResponse<Provision>> orderCancellation(
			@RequestBody CancelOrderRequest request) {
		log.info(this.getClass().getName() + " - " + "orderCancellation");

		ProvisionArrayResponse<Provision> response = new ProvisionArrayResponse<>();
		try {
			Provision result = provisionService.orderCancellation(request.getProvisionId());

			if (result != null) {
				List<Provision> provisions = new ArrayList<>();
				provisions.add(result);
				response.setHeader(
						new ProvisionHeaderResponse().generateHeader(HttpStatus.OK.value(), HttpStatus.OK.name()));
				response.setData(provisions);

				restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
						request.getOrderCode(), request.getBucket(), "OK", new Gson().toJson(request),
						new Gson().toJson(response), ConstantsLogData.PROVISION_CANCEL);

				return ResponseEntity.ok(response);
			} else {
				response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
						HttpStatus.BAD_REQUEST.name()));
				restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
						request.getOrderCode(), request.getBucket(), "ERROR", new Gson().toJson(request),
						new Gson().toJson(response), ConstantsLogData.PROVISION_CANCEL);

				return ResponseEntity.badRequest().body(response);
			}

		} catch (Exception ex) {

			restSecuritySaveLogData.saveLogData(request.getDocumentNumber(), request.getDocumentType(),
					request.getOrderCode(), request.getBucket(), "ERROR", new Gson().toJson(request),
					new Gson().toJson(ex.getMessage()), ConstantsLogData.PROVISION_CANCEL);

			response.setHeader(new ProvisionHeaderResponse().generateHeader(HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.name()));
			return ResponseEntity.badRequest().body(response);
		}

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
}
