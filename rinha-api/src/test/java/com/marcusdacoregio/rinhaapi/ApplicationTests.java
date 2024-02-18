package com.marcusdacoregio.rinhaapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
@Sql(scripts = "classpath:reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisabledInNativeImage
class ApplicationTests {

	@Autowired
	MockMvc mvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	Clientes clientes;

	@Test
	void novaTransacaoQuandoClienteNaoExisteEntao404() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				1200, TipoTransacao.CREDITO.getSimbolo(), "desc");
		executarTransacao(request, 99).andExpectAll(status().isNotFound());
	}

	@Test
	void novaTransacaoQuandoCreditoEntaoSaldoAtualizado() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				1200, TipoTransacao.CREDITO.getSimbolo(), "desc");
		executarTransacao(request, 1).andExpectAll(
				status().isOk(),
				jsonPath("$.limite").value(100000),
				jsonPath("$.saldo").value(1200));
	}

	@Test
	void novaTransacaoQuandoDebitoESaldoSuficienteEntaoSaldoAtualizado() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				1200, TipoTransacao.DEBITO.getSimbolo(), "desc");
		executarTransacao(request, 1).andExpectAll(
				status().isOk(),
				jsonPath("$.limite").value(100000),
				jsonPath("$.saldo").value(-1200));
	}

	@Test
	void novaTransacaoQuandoMultiplosDebitosResultandoEmSaldoSuficienteEntao422NoUltimoDebito() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				1200, TipoTransacao.DEBITO.getSimbolo(), "desc");
		executarTransacao(request, 1).andExpectAll(
				status().isOk(),
				jsonPath("$.limite").value(100000),
				jsonPath("$.saldo").value(-1200));
		request = new ClientesController.NovaTransacaoRequest(
				90000, TipoTransacao.DEBITO.getSimbolo(), "desc");
		executarTransacao(request, 1).andExpectAll(
				status().isOk(),
				jsonPath("$.limite").value(100000),
				jsonPath("$.saldo").value(-91200));
		request = new ClientesController.NovaTransacaoRequest(
				10000, TipoTransacao.DEBITO.getSimbolo(), "desc");
		executarTransacao(request, 1).andExpectAll(
				status().isUnprocessableEntity(),
				jsonPath("$.detail").value("Saldo -91200 e limite 100000 insuficientes para débito no valor de 10000"));
	}

	@Test
	void novaTransacaoQuandoDebitoESaldoInsuficienteEntao422ESaldoNaoAlterado() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				100001, TipoTransacao.DEBITO.getSimbolo(), "desc");
		executarTransacao(request, 1).andExpectAll(
				status().isUnprocessableEntity(),
				jsonPath("$.detail").value("Saldo 0 e limite 100000 insuficientes para débito no valor de 100001"));
		Cliente cliente = this.clientes.getCliente(1);
		assertThat(cliente.saldo()).isZero();
	}

	@Test
	void emitirExtrato() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				10, TipoTransacao.CREDITO.getSimbolo(), "desc");
		executarTransacao(request, 1).andExpectAll(
				status().isOk(),
				jsonPath("$.limite").value(100000),
				jsonPath("$.saldo").value(10));

		request = new ClientesController.NovaTransacaoRequest(
				90000, TipoTransacao.DEBITO.getSimbolo(), "desc");
		executarTransacao(request, 1).andExpectAll(
				status().isOk(),
				jsonPath("$.limite").value(100000),
				jsonPath("$.saldo").value(-89990));

		this.mvc.perform(get("/clientes/{id}/extrato", 1))
				.andExpectAll(
						status().isOk(),
						jsonPath("$.saldo.limite").value(100000),
						jsonPath("$.saldo.total").value(-89990),
						jsonPath("$.saldo.data_extrato").isNotEmpty(),
						jsonPath("$.ultimas_transacoes", hasSize(2)));
	}

	@Test
	void emitirExtratoQuandoClienteNaoExisteEntao404() throws Exception {
		this.mvc.perform(get("/clientes/{id}/extrato", 99))
				.andExpectAll(status().isNotFound());
	}

	private ResultActions executarTransacao(ClientesController.NovaTransacaoRequest request, long clienteId) throws Exception {
		return this.mvc.perform(
				post("/clientes/{id}/transacoes", clienteId)
						.content(this.objectMapper.writeValueAsBytes(request))
						.contentType(MediaType.APPLICATION_JSON));
	}

}
