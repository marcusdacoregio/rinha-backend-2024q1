package com.marcusdacoregio.rinhaapi;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

class SaldoInsuficienteException extends ErrorResponseException {

	public SaldoInsuficienteException(String detail) {
		super(HttpStatus.UNPROCESSABLE_ENTITY);
		setTitle("Saldo Insuficiente");
		setDetail(detail);
	}

}
