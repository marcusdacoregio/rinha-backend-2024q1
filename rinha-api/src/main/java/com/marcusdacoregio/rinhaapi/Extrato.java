package com.marcusdacoregio.rinhaapi;

import java.time.Instant;
import java.util.List;

public record Extrato(Saldo saldo, List<Transacao> ultimasTransacoes) {

	public record Saldo(long total, long limite, Instant dataExtrato) {
	}

}
