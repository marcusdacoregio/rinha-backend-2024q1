package com.marcusdacoregio.rinhaapi;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
class Clientes {

	private final JdbcClient jdbcClient;

	private static final ClienteRowMapper mapper = new ClienteRowMapper();

	Clientes(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	Cliente getCliente(long clienteId) {
		List<Cliente> list = this.jdbcClient.sql("SELECT * FROM clientes c WHERE c.id = ?")
				.param(clienteId)
				.query(mapper)
				.list();
		if (list.isEmpty()) {
			return null;
		}
		return list.getFirst();
	}

	void creditarSaldo(long clienteId, long valorCredito) {
		this.jdbcClient.sql("UPDATE clientes SET saldo = (saldo + :valor) WHERE id = :clienteId")
				.param("valor", valorCredito)
				.param("clienteId", clienteId)
				.update();
	}

	void debitarSaldo(long clienteId, long valorDebito) {
		try {
			int updated = this.jdbcClient.sql("UPDATE clientes SET saldo = (saldo - :valor) WHERE id = :clienteId AND (saldo - :valor) + limite > 0")
					.param("valor", valorDebito)
					.param("clienteId", clienteId)
					.update();
			if (updated == 0) {
				throw new SaldoInsuficienteException("Saldo insuficiente para operação de débito");
			}
		} catch (DataIntegrityViolationException ex) {
			if (ex.getMessage().contains("saldo_minimo_check")) {
				throw new SaldoInsuficienteException("Saldo insuficiente para operação de débito");
			}
			throw ex;
		}
	}

	static class ClienteRowMapper implements RowMapper<Cliente> {

		@Override
		public Cliente mapRow(ResultSet rs, int rowNum) throws SQLException {
			if (rs.getRow() == 0) {
				return null;
			}
			long id = rs.getLong("id");
			long limite = rs.getLong("limite");
			long saldo = rs.getLong("saldo");
			return new Cliente(id, saldo, limite);
		}

	}
}
