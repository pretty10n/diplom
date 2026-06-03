create table document
(
    id         uuid primary key,
    status     varchar(32)              not null default 'draft',
    created_at timestamptz              not null default now(),
    updated_at timestamptz              not null default now()
);

create table document_common_info
(
    document_id uuid primary key references document (id) on delete cascade,
    tru_name    varchar(512) not null,
    tru_code    varchar(128) not null,
    stage       varchar(128) not null,
    report_year integer      not null check (report_year between 1000 and 9999),
    plan_year   integer      not null check (plan_year between 1000 and 9999)
);

create table dictionary_section_key
(
    key        varchar(64) primary key,
    label      varchar(512) not null,
    form_no    integer      not null check (form_no in (4, 5, 6)),
    section_no integer      not null check (section_no > 0),
    version    integer      not null check (version > 0),
    active     boolean      not null default true
);

create table dictionary_value
(
    id              bigserial primary key,
    dictionary_type varchar(32)  not null check (dictionary_type in ('COL13_2', 'COL5_2')),
    code            varchar(128) not null,
    label           varchar(512) not null,
    version         integer      not null check (version > 0),
    active          boolean      not null default true,
    unique (dictionary_type, code, version)
);

create table document_entry
(
    id                uuid primary key,
    document_id       uuid        not null references document (id) on delete cascade,
    section_key       varchar(64) not null references dictionary_section_key (key),
    row_no            integer     not null check (row_no > 0),
    fields            jsonb       not null,
    computed          jsonb       not null,
    validation_status varchar(32) not null check (validation_status in ('VALID', 'INVALID', 'WARNING')),
    created_at        timestamptz not null default now(),
    updated_at        timestamptz not null default now(),
    unique (document_id, section_key, row_no),
    check (
        (fields ->> 'col15') is null
            or (fields ->> 'col15') ~ '^[0-9]{10}([0-9]{2})?$'
        )
);

create table document_totals_snapshot
(
    id         uuid primary key,
    document_id uuid        not null references document (id) on delete cascade,
    totals     jsonb        not null,
    created_at timestamptz  not null default now()
);

create index idx_document_entry_doc_section_status
    on document_entry (document_id, section_key, validation_status);

create index idx_document_entry_fields_gin
    on document_entry using gin (fields);

create index idx_document_entry_computed_gin
    on document_entry using gin (computed);

insert into dictionary_section_key (key, label, form_no, section_no, version, active)
values ('raw_materials', 'Сырье и основные материалы', 4, 1, 1, true),
       ('aux_materials', 'Вспомогательные материалы', 4, 2, 1, true),
       ('return_waste_f4', 'Возвратные отходы (вычитаются) [Форма 4]', 4, 3, 1, true),
       ('purchased_semi', 'Покупные полуфабрикаты', 5, 1, 1, true),
       ('return_waste_f5', 'Возвратные отходы (вычитаются) [Форма 5]', 5, 2, 1, true),
       ('components', 'Приобретение комплектующих изделий', 6, 1, 1, true);
