create table IF NOT EXISTS categories
(
    id serial not null
        constraint categories_pk
            primary key,
    name char(15) not null
);

create index if not exists categories_name__index
    on categories (name);

create unique index if not exists  categories_id_index
    on categories (id);

create table IF NOT EXISTS breeds
(
    id serial not null
        constraint breeds_pk
            primary key,
    name char(25) not null
);

create table IF NOT EXISTS pets
(
    id char(36) not null
        constraint pets_pk
            primary key,
    name char(20) not null,
--     category integer not null
--         constraint pets_categories_id_fk
--             references categories,
--     breed integer not null
--         constraint pets_breeds_id_fk
--             references breeds,
    dob timestamp not null,
    creation timestamp default now() not null
);

create unique index if not exists  pets_id_uindex
    on pets (id);

create index if not exists breed_name__index
    on breeds (name);

create unique index if not exists  breeds_id_uindex
    on breeds (id);

create table IF NOT EXISTS vaccines
(
    id serial not null
        constraint vaccines_pk
            primary key,
    name char(50) not null
);

create unique index if not exists  vaccines_id_uindex
    on vaccines (id);

create index if not exists vaccines_name__index
    on vaccines (name);

create table IF NOT EXISTS tags
(
    id serial not null
        constraint tags_pk
            primary key,
    name char(15) not null
);

create unique index if not exists  tags_id_uindex
    on tags (id);

create index if not exists tags_name__index
    on tags (name);

create table IF NOT EXISTS pets_vaccines
(
    id_pet char(36) not null
        constraint pets_vaccines_pets_id_fk
            references pets,
    id_vaccine integer not null
        constraint pets_vaccines_vaccines_id_fk
            references vaccines,
    constraint pets_vaccines_pk
        primary key (id_pet, id_vaccine)
);

create table IF NOT EXISTS pets_tags
(
    id_pet char(36) not null
        constraint pets_tags_pets_id_fk
            references pets,
    id_tag integer not null
        constraint pets_tags_tags_id_fk
            references tags,
    constraint pets_tags_pk
        primary key (id_pet, id_tag)
);
