package pe.telefonica.provision.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import pe.telefonica.provision.controller.request.order.OrderRequest;
import pe.telefonica.provision.controller.response.order.OrderResponse;
import pe.telefonica.provision.service.OrderService;

@RestController
@EnableWebMvc
@Validated
@CrossOrigin(origins = "*")
@RequestMapping("orders/v1/order")

public class OrderController {

	@Autowired
	private OrderService orderService;

	// Creación de ordenes
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Object> createOrder(@Valid @RequestBody OrderRequest request) throws Exception {
		return orderService.createOrder(request);
	}
}
