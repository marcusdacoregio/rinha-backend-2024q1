package com.marcusdacoregio.rinhaapi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
class ClientesController {

	private final Transacoes transacoes;

	ClientesController(Transacoes transacoes) {
		this.transacoes = transacoes;
	}

	@PostMapping("/{clienteId}/transacoes")
	ResponseEntity<TransacaoClienteResult> novaTransacao(@PathVariable long clienteId, @RequestBody @Valid NovaTransacaoRequest request) {
		TipoTransacao tipoTransacao = TipoTransacao.fromSimbolo(request.tipo);
		if (tipoTransacao == null) {
			return ResponseEntity.unprocessableEntity().build();
		}
		NovaTransacaoParams params = new NovaTransacaoParams(clienteId, request.valor, tipoTransacao, request.descricao);
		return ResponseEntity.ok(this.transacoes.novaTransacao(params));
	}

	@GetMapping("/{clienteId}/extrato")
	ResponseEntity<Extrato> emitirExtrato(@PathVariable long clienteId) {
		return ResponseEntity.of(this.transacoes.emitirExtrato(clienteId));
	}

	record NovaTransacaoRequest (

		@Positive
		long valor,

		@NotEmpty
		String tipo,

		@NotEmpty @Length(min = 1, max = 10)
		String descricao

	) {}
}
