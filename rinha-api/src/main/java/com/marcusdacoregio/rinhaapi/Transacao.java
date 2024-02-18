package com.marcusdacoregio.rinhaapi;

import java.time.Instant;

public record Transacao(long valor, String tipo, String descricao, Instant realizadaEm) {
}
