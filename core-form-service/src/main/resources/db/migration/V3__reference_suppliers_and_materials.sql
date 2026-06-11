create table reference_supplier
(
    id         uuid primary key,
    name       varchar(512) not null,
    inn        varchar(12),
    created_at timestamptz  not null default now(),
    updated_at timestamptz  not null default now(),
    check (inn is null or inn ~ '^[0-9]{10}([0-9]{2})?$')
);

create unique index uq_reference_supplier_inn
    on reference_supplier (inn)
    where inn is not null;

create unique index uq_reference_supplier_name_no_inn
    on reference_supplier (lower(trim(name)))
    where inn is null;

create index idx_reference_supplier_name_search
    on reference_supplier (lower(name) varchar_pattern_ops);

create table reference_material
(
    id         uuid primary key,
    name       varchar(512) not null,
    okpd_code  varchar(128),
    ekps_code  varchar(128),
    fnn        varchar(128),
    created_at timestamptz  not null default now(),
    updated_at timestamptz  not null default now()
);

create unique index uq_reference_material_name
    on reference_material (lower(trim(name)));

create index idx_reference_material_name_search
    on reference_material (lower(name) varchar_pattern_ops);
