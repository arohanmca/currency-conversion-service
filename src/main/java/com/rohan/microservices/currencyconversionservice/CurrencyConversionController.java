package com.rohan.microservices.currencyconversionservice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CurrencyConversionController {
	
	Logger logger = LoggerFactory.getLogger(CurrencyConversionController.class);
	
	@Autowired
	CurrencyExchangeServiceProxy proxy;

	@GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean convertCurrency(@PathVariable String from, @PathVariable String to,
			@PathVariable BigDecimal quantity) {

		Map<String, String> uriVariables = new HashMap<>();
		uriVariables.put("from", from);
		uriVariables.put("to", to);

		ResponseEntity<CurrencyConversionBean> responseEntity = new RestTemplate().getForEntity(
				"http://localhost:8000/currency-exchange/from/{from}/to/{to}", CurrencyConversionBean.class,
				uriVariables);

		CurrencyConversionBean responseBody = responseEntity.getBody();

		BigDecimal multiply = quantity.multiply(responseBody.getConversionMultiple());
		return new CurrencyConversionBean(responseBody.getId(), from, to, quantity, responseBody.getConversionMultiple(),
				multiply, 0);
	}
	
	
	@GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversionBean convertCurrencyFeign(@PathVariable String from, @PathVariable String to,
			@PathVariable BigDecimal quantity) {

		CurrencyConversionBean responseBody = proxy.retrieveExchangeValue(from, to);
		
		logger.info("--->>{}<<---", responseBody);

		BigDecimal multiply = quantity.multiply(responseBody.getConversionMultiple());
		return new CurrencyConversionBean(responseBody.getId(), from, to, quantity, responseBody.getConversionMultiple(),
				multiply, responseBody.getPort());
	}
}
