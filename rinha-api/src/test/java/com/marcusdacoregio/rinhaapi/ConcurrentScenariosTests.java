package com.marcusdacoregio.rinhaapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
@Sql(scripts = "classpath:reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisabledInAotMode
@ActiveProfiles("test")
class ConcurrentScenariosTests {

	@Autowired
	MockMvc mvc;

	@Autowired
	ObjectMapper objectMapper;

	@SpyBean
	Clientes clientes;

	@Test
	void novaTransacaoQuandoDebitoESaldoInsuficienteAlteradoDuranteTransacaoEntao422() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				1000, TipoTransacao.DEBITO.getSimbolo(), "desc");

		// alterar o valor do saldo do cliente após buscar o mesmo para simular uma request concorrente
		when(this.clientes.getCliente(anyLong())).thenAnswer(invocation -> {
			Object cliente = invocation.callRealMethod();
			this.clientes.debitarSaldo(1, 99999);
			return cliente;
		});

		this.mvc.perform(
				post("/clientes/{id}/transacoes", 1)
						.content(this.objectMapper.writeValueAsBytes(request))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(
						status().isUnprocessableEntity(),
						jsonPath("$.detail").value("Saldo insuficiente para operação de débito"));
	}

}
