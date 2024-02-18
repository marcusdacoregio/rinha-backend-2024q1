package com.marcusdacoregio.rinhaapi;

enum TipoTransacao {

	CREDITO("c"),
	DEBITO("d");

	private final String simbolo;

	TipoTransacao(String simbolo) {
		this.simbolo = simbolo;
	}

	public String getSimbolo() {
		return simbolo;
	}

	static TipoTransacao fromSimbolo(String simbolo) {
		if (CREDITO.getSimbolo().equals(simbolo)) {
			return CREDITO;
		}
		if (DEBITO.getSimbolo().equals(simbolo)) {
			return DEBITO;
		}
		return null;
	}

}
