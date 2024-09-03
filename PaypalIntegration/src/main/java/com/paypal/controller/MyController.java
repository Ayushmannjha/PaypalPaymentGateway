package com.paypal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.services.PaypalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MyController {
	
private final PaypalService psr;

@GetMapping("/")
public String home() {
	return "index";
}
	@PostMapping("/payment/create")
	public RedirectView createPayment() {
		try {
			String cacelUrl = "https://localhost:8083/payment/cancel";
			String successUrl = "https://localhost:8083/payment/success";
			Payment payment = psr.createPayment(10.0, "USD", "paypal", "sale", "payment description", cacelUrl, successUrl);
		for(Links links: payment.getLinks()) {
			if(links.getRel().equals("approval_url")) {
				return new RedirectView(links.getHref());
			}
		}
		} catch (PayPalRESTException e) {
			log.error("Error occurred:: ",e);
		}
		return new RedirectView("payment/error");
	}
	@GetMapping("/payment/success")
	public String paymentSuccess(
			@RequestParam("paymentId") String paymentId,
			@RequestParam("payerId") String payerId
			) {
		try {
			Payment payment = psr.executePayment(paymentId, payerId);
			if(payment.getState().equals("approved")) {
				return "paymentSuccess";
			}
		} catch (PayPalRESTException e) {
			log.error("Error occurred:: ",e);
		}
		return "paymentSuccess";
	}
	
	@GetMapping("/payment/cancel")
	public String paymentCacel() {
		return "paymentCancel";
	}
	
	
	@GetMapping("/payment/error")
	public String paymentError() {
		return "paymentError";
	}
}
