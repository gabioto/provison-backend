package pe.telefonica.provision.controller;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import pe.telefonica.provision.controller.request.order.OrderRequest;
import pe.telefonica.provision.service.NotifyOrderService;
import pe.telefonica.provision.service.OrderService;
import pe.telefonica.provision.service.RetreiveOrderService;

@RestController
//@EnableWebMvc
@Validated
@CrossOrigin(origins = "*")
@RequestMapping("orders/v1/order")

public class OrderController {

	private static final Log log = LogFactory.getLog(OrderController.class);

	@Autowired
	private OrderService orderService;

	@Autowired
	private RetreiveOrderService retrieveOrderService;

	@Autowired
	private NotifyOrderService notifyOrderService;

	// Creación de ordenes
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Object> createOrder(@Valid @RequestBody OrderRequest request) throws Exception {
		log.info(new Gson().toJson(request));
		return orderService.createOrder(request);
	}

	@GetMapping("/getProvision")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Object> getOrder(
			@RequestHeader(value = "UNICA-ServiceId", required = false) String uServiceId,
			@RequestHeader(value = "UNICA-Application", required = false) String uApplication,
			@RequestHeader(value = "UNICA-PID", required = false) String uPid,
			@RequestHeader(value = "UNICA-User", required = false) String uUser,
			@RequestParam(name = "codigo", required = false, defaultValue = "") String code,
			@RequestParam(name = "originSystem", required = false, defaultValue = "") String originSystem,
			@RequestParam(name = "PublicId", required = false, defaultValue = "") String publicId,
			@RequestParam(name = "orden", required = false, defaultValue = "") String order,
			@RequestParam(name = "codigoCuenta", required = false, defaultValue = "") String orderCode,
			@RequestParam(name = "codigoCliente", required = false, defaultValue = "") String customerCode,
			@RequestParam(name = "fechaRegistroInicio", required = false, defaultValue = "") String startDate,
			@RequestParam(name = "fechaRegistroFin", required = false, defaultValue = "") String endDate)
			throws Exception {

		return retrieveOrderService.getOrder(uServiceId, uPid, code, originSystem, publicId, order, orderCode,
				customerCode, startDate, endDate);
	}

	@GetMapping("/getOrdersToNotify")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Object> getOrdersToNotify(
			@RequestHeader(value = "UNICA-ServiceId", required = false) String uServiceId,
			@RequestHeader(value = "UNICA-Application", required = false) String uApplication,
			@RequestHeader(value = "UNICA-PID", required = false) String uPid,
			@RequestHeader(value = "UNICA-User", required = false) String uUser) throws Exception {

		return notifyOrderService.getOrders(uServiceId, uPid);
	}
}
