package pe.telefonica.provision.api;

import java.util.List;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.telefonica.provision.api.request.ReceiveAddressUpdateBORequest;
import pe.telefonica.provision.api.request.SetContactInfoUpdateRequest;
import pe.telefonica.provision.api.response.OrderCancellationResponse;
import pe.telefonica.provision.api.response.ReceiveAddressUpdateBOResponse;
import pe.telefonica.provision.api.response.RequestAddressUpdateResponse;
import pe.telefonica.provision.api.response.ResponseHeader;
import pe.telefonica.provision.api.response.SetContactInfoUpdateResponse;
import pe.telefonica.provision.api.response.SetProvisionValidatedResponse;
import pe.telefonica.provision.dto.Provision;
import pe.telefonica.provision.service.ProvisionService;

@RestController
@CrossOrigin(origins = "*")
public class ProvisionApi {
	
	private static final Log log = LogFactory.getLog(ProvisionApi.class);
	
	@Autowired
	ProvisionService contactService;
	
	@Autowired
	Mapper mapper;
	
	private final ProvisionService provisionService;
   
    @Autowired
    public ProvisionApi(ProvisionService provisionService) {
        this.provisionService = provisionService;
    }
	
    
    /**
     * 
     * @param provisionRequest
     * @return ProvisionResponse<Provision>
     * @description get all provisions related to type and number of the document
     */
	@RequestMapping(value="/getOrders", method=RequestMethod.POST)
	public ResponseEntity<ProvisionResponse<Provision>> getOrders(@RequestBody @Valid ProvisionRequest provisionRequest){
		return ResponseEntity.ok(provisionService.getAll(provisionRequest));
	}
	
	/**
     * 
     * @param provisionListReq
     * @return ProvisionResponse<Provision>
     * @description insert a list of provisions
     */
	@RequestMapping(value="/insertOrders", method=RequestMethod.POST)
	public ResponseEntity<ProvisionResponse<Provision>> insertOrders(@RequestBody @Valid List<Provision> provisionListReq){
		return ResponseEntity.ok(provisionService.insertProvisionList(provisionListReq));
	}
	
	/**
	 * 
	 * @param provisionId
	 * @return
	 */
	@RequestMapping(value="/setProvisionValidated", method=RequestMethod.POST)
	public ResponseEntity<SetProvisionValidatedResponse> setProvisionValidated(
			@RequestParam(value = "provisionId", required = true) String provisionId){
		log.info(this.getClass().getName() + " - " + "setProvisionValidated");
		
		Boolean result = provisionService.setProvisionIsValidated(provisionId);
		
		SetProvisionValidatedResponse response = new SetProvisionValidatedResponse();
		response.setResult(result);
		
		if(result) {
			response.setHeader(new ResponseHeader().generateHeader("ok", "ok"));
			return ResponseEntity.ok(response);
		} else {
			response.setHeader(new ResponseHeader().generateHeader("err", "error"));
			return ResponseEntity.badRequest().body(response);
		}
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/setContactInfoUpdate", method=RequestMethod.POST)
	public ResponseEntity<SetContactInfoUpdateResponse> setContactInfoUpdate(
			@RequestBody SetContactInfoUpdateRequest request){
		log.info(this.getClass().getName() + " - " + "setContactInfoUpdate");
		
		Boolean result = provisionService.setContactInfoUpdate(request.getProvisionId(), request.getContactFullname(), request.getContactCellphone(), request.getContactCellphoneIsMovistar());
		
		SetContactInfoUpdateResponse response = new SetContactInfoUpdateResponse();
		response.setResult(result);
		
		if(result) {
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
	@RequestMapping(value="/requestAddressUpdate", method=RequestMethod.POST)
	public ResponseEntity<RequestAddressUpdateResponse> requestAddressUpdate(
			@RequestParam(value = "provisionId", required = true) String provisionId) {
		log.info(this.getClass().getName() + " - " + "requestAddressUpdate");
		
		Boolean result = provisionService.requestAddressUpdate(provisionId);
		
		RequestAddressUpdateResponse response = new RequestAddressUpdateResponse();
		response.setResult(result);
		
		if(result) {
			response.setHeader(new ResponseHeader().generateHeader("ok", "ok"));
			return ResponseEntity.ok(response);
		} else {
			response.setHeader(new ResponseHeader().generateHeader("err", "error"));
			return ResponseEntity.badRequest().body(response);
		}
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/receiveAddressUpdateBO", method=RequestMethod.PUT)
	public ResponseEntity<ReceiveAddressUpdateBOResponse> receiveAddressUpdateBO(@RequestBody ReceiveAddressUpdateBORequest request){
		log.info(this.getClass().getName() + " - " + "receiveAddressUpdateBO");
		
		Boolean result = provisionService.receiveAddressUpdateBO(request.getAction(), request.getProvisionId(), request.getNewDepartment(), 
				request.getNewProvince(), request.getNewDistrict(), request.getNewAddress(), request.getNewReference(), 
				request.getIsSMSRequired());
		
		ReceiveAddressUpdateBOResponse response = new ReceiveAddressUpdateBOResponse();
		response.setResult(result);
		
		if(result) {
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
	@RequestMapping(value="/orderCancellation", method=RequestMethod.POST)
	public ResponseEntity<OrderCancellationResponse> orderCancellation(
			@RequestParam(value = "provisionId", required = true) String provisionId){
		log.info(this.getClass().getName() + " - " + "orderCancellation");
		
		Boolean result = provisionService.orderCancellation(provisionId);
		
		OrderCancellationResponse response = new OrderCancellationResponse();
		response.setResult(result);
		
		if(result) {
			response.setHeader(new ResponseHeader().generateHeader("ok", "ok"));
			return ResponseEntity.ok(response);
		} else {
			response.setHeader(new ResponseHeader().generateHeader("err", "error"));
			return ResponseEntity.badRequest().body(response);
		}
	}
	
	/*
	@RequestMapping(value="/receiveCancellationBOResponse", method=RequestMethod.PUT)
	public ResponseEntity<ReceiveCancellationBOResponse> receiveCancellationBOResponse(
			@RequestParam(value = "provisionId", required = true) String provisionId){
		
		
		return null;
	}
	*/
	
	
	
}
