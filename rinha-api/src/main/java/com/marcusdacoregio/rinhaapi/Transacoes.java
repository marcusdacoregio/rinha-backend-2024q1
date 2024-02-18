package com.marcusdacoregio.rinhaapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
class Transacoes {

	private final Clientes clientes;

	private final JdbcClient jdbcClient;

	private final ObjectMapper mapper;

	Transacoes(Clientes clientes, JdbcClient jdbcClient, ObjectMapper objectMapper) {
		this.clientes = clientes;
		this.jdbcClient = jdbcClient;
		this.mapper = objectMapper;
	}

	TransacaoClienteResult novaTransacao(NovaTransacaoParams params) {
		Cliente cliente = this.clientes.getCliente(params.clienteId());
		if (cliente == null) {
			throw new NotFoundException("Cliente não encontrado");
		}
		switch (params.tipoTransacao()) {
			case CREDITO -> {
				return creditar(cliente, params);
			}
			case DEBITO -> {
				return debitar(cliente, params);
			}
		}
		throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Requisição de transação inválida");
	}

	Optional<Extrato> emitirExtrato(long clienteId) {
		String sql = """
				WITH transacoes_ordenadas AS (
					SELECT 
						t.valor,
						t.tipo,
						t.descricao,
						t.realizada_em
					FROM 
						transacoes as t
					WHERE 
						t.cliente_id = :clienteId
					ORDER BY 
						t.id DESC
					LIMIT 10
				)
				SELECT
					c.id,
					c.limite,
					c.saldo,
					(
						SELECT coalesce(
							jsonb_agg(jsonb_build_object(
								'valor', ot.valor,
								'tipo', ot.tipo,
								'descricao', ot.descricao,
								'realizada_em', ot.realizada_em
							)),
							jsonb_build_array()
						)
						FROM transacoes_ordenadas as ot
					) as ultimas_transacoes
				FROM 
					clientes c
				WHERE 
					c.id = :clienteId;
				""";
		return this.jdbcClient.sql(sql).param("clienteId", clienteId)
				.query(new ExtratoRowMapper(this.mapper))
				.optional();
	}

	private TransacaoClienteResult debitar(Cliente cliente, NovaTransacaoParams params) {
		if (!cliente.possuiSaldoParaDebito(params.valor())) {
			throw new SaldoInsuficienteException("Saldo " + cliente.saldo() + " e limite " + cliente.limite() + " insuficientes para débito no valor de " + params.valor());
		}
		this.clientes.debitarSaldo(cliente.id(), params.valor());
		salvarTransacao(params);
		return new TransacaoClienteResult(cliente.limite(), cliente.saldo() - params.valor());
	}

	private TransacaoClienteResult creditar(Cliente cliente, NovaTransacaoParams params) {
		this.clientes.creditarSaldo(cliente.id(), params.valor());
		salvarTransacao(params);
		return new TransacaoClienteResult(cliente.limite(), cliente.saldo() + params.valor());
	}

	private void salvarTransacao(NovaTransacaoParams params) {
		this.jdbcClient
				.sql("""
						INSERT INTO transacoes (cliente_id, valor, tipo, descricao, realizada_em)
						VALUES (?, ?, ?, ?, ?)""")
				.param(params.clienteId())
				.param(params.valor())
				.param(params.tipoTransacao().getSimbolo())
				.param(params.descricao())
				.param(Timestamp.from(Instant.now()))
				.update();
	}

	static class ExtratoRowMapper implements RowMapper<Extrato> {

		final ObjectMapper mapper;

		ExtratoRowMapper(ObjectMapper mapper) {
			this.mapper = mapper;
		}

		@Override
		public Extrato mapRow(ResultSet rs, int rowNum) throws SQLException {
			long limite = rs.getLong("limite");
			long saldo = rs.getLong("saldo");

			Extrato.Saldo saldoExtrato = new Extrato.Saldo(saldo, limite, Instant.now());
			String ultimasTransacoesJson = rs.getString("ultimas_transacoes");
			List<Transacao> ultimasTransacoes = convertUltimasTransacoes(ultimasTransacoesJson);

			return new Extrato(saldoExtrato, ultimasTransacoes);
		}

		private List<Transacao> convertUltimasTransacoes(String json) {
			try {
				return this.mapper.readerForListOf(Transacao.class).readValue(json);
			} catch (JsonProcessingException e) {
				return Collections.emptyList();
			}
		}

	}

}
