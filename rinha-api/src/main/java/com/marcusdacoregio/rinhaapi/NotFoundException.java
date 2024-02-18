package com.marcusdacoregio.rinhaapi;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ResponseStatus;

class NotFoundException extends ErrorResponseException {

	public NotFoundException(String detail) {
		super(HttpStatus.NOT_FOUND);
		setTitle("Não encontrado");
		setDetail(detail);
	}

}
