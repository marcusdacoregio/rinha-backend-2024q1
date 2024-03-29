CREATE TABLE IF NOT EXISTS clientes
(
    id     bigint generated by default as identity primary key,
    limite bigint not null default 0,
    saldo  bigint not null default 0,
    constraint saldo_minimo_check check ( saldo >= -(limite) )
);

CREATE TABLE IF NOT EXISTS transacoes
(
    id           bigint generated by default as identity primary key,
    cliente_id   bigint      not null,
    valor        bigint      not null,
    tipo         varchar(1)  not null,
    descricao    varchar(10) not null,
    realizada_em timestamptz not null default now(),
    constraint cliente_id_fk foreign key (cliente_id) references clientes (id)
);

create index if not exists transacoes_cliente_id_idx ON transacoes (cliente_id);