package com.marcusdacoregio.rinhaapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientesController.class)
@DisabledInAotMode
class ClientesControllerTests {

	@MockBean
	Transacoes transacoes;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	void novaTransacaoQuandoDescricaoMuitoLongaEntao422() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				1200, TipoTransacao.CREDITO.getSimbolo(), "muito longa essa descriçao, tá louco");
		this.mockMvc.perform(post("/clientes/1/transacoes")
						.content(this.objectMapper.writeValueAsBytes(request))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isUnprocessableEntity());
	}

	@Test
	void novaTransacaoQuandoDescricaoEmBrancoEntao422() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				1200, TipoTransacao.CREDITO.getSimbolo(), "");
		this.mockMvc.perform(post("/clientes/1/transacoes")
						.content(this.objectMapper.writeValueAsBytes(request))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isUnprocessableEntity());
	}

	@Test
	void novaTransacaoQuandoDescricaoNullEntao422() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				1200, TipoTransacao.CREDITO.getSimbolo(), null);
		this.mockMvc.perform(post("/clientes/1/transacoes")
						.content(this.objectMapper.writeValueAsBytes(request))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isUnprocessableEntity());
	}

	@Test
	void novaTransacaoQuandoValorNegativoEntao422() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				-100, TipoTransacao.DEBITO.getSimbolo(), "desc");
		this.mockMvc.perform(post("/clientes/1/transacoes")
						.content(this.objectMapper.writeValueAsBytes(request))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isUnprocessableEntity());
	}

	@Test
	void novaTransacaoQuandoValorZeroEntao422() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				0, TipoTransacao.CREDITO.getSimbolo(), "desc");
		this.mockMvc.perform(post("/clientes/1/transacoes")
						.content(this.objectMapper.writeValueAsBytes(request))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isUnprocessableEntity());
	}

	@Test
	void novaTransacaoQuandoTipoNullEntao422() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				0, null, "desc");
		this.mockMvc.perform(post("/clientes/1/transacoes")
						.content(this.objectMapper.writeValueAsBytes(request))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isUnprocessableEntity());
	}

	@Test
	void novaTransacaoQuandoTipoInexistenteEntao422() throws Exception {
		ClientesController.NovaTransacaoRequest request = new ClientesController.NovaTransacaoRequest(
				0, "x", "desc");
		this.mockMvc.perform(post("/clientes/1/transacoes")
						.content(this.objectMapper.writeValueAsBytes(request))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isUnprocessableEntity());
	}

	@Test
	void novaTransacaoQuandoValorDecimalEntao422() throws Exception {
		String request = """
				{
					"valor": 1.2,
					"tipo": "d",
					"descricao": "desc"
				}
				""";
		this.mockMvc.perform(post("/clientes/1/transacoes")
						.content(request)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isUnprocessableEntity());
	}

}