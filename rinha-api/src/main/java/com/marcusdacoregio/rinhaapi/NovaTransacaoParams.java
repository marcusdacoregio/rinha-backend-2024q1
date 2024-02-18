package com.marcusdacoregio.rinhaapi;

import org.springframework.util.Assert;

record NovaTransacaoParams(long clienteId, long valor, TipoTransacao tipoTransacao, String descricao) {

	NovaTransacaoParams(long clienteId, long valor, TipoTransacao tipoTransacao, String descricao) {
		Assert.state(clienteId > 0, "ID do Cliente deve ser válido");
		Assert.state(valor > 0, "Valor é obrigatório");
		Assert.notNull(tipoTransacao, "Tipo da transação é obrigatório");
		Assert.hasText(descricao, "Descrição é obrigatória");
		this.clienteId = clienteId;
		this.valor = valor;
		this.tipoTransacao = tipoTransacao;
		this.descricao = descricao;
	}

}
