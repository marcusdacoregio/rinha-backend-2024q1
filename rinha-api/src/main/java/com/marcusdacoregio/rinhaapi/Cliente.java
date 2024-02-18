package com.marcusdacoregio.rinhaapi;

public record Cliente(long id, long saldo, long limite) {

	boolean possuiSaldoParaDebito(long valorDebito) {
		return (this.saldo - valorDebito) >= -(limite);
	}

}
