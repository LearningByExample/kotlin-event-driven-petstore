create table IF NOT EXISTS categories
(
    id serial not null
        constraint categories_pk
            primary key,
    name varchar(15) not null
);;

create index if not exists categories_name__index
    on categories (name);

create unique index if not exists  categories_id_index
    on categories (id);

create table IF NOT EXISTS breeds
(
    id serial not null
        constraint breeds_pk
            primary key,
    name varchar(25) not null
);;

create table IF NOT EXISTS pets
(
    id varchar(36) not null
        constraint pets_pk
            primary key,
    name varchar(20) not null,
     category integer not null
         constraint pets_categories_id_fk
             references categories,
     breed integer not null
         constraint pets_breeds_id_fk
             references breeds,
    dob timestamp not null,
    creation timestamp default now() not null
);;

create unique index if not exists  pets_id_uindex
    on pets (id);;

create index if not exists breed_name__index
    on breeds (name);;

create unique index if not exists  breeds_id_uindex
    on breeds (id);;

create table IF NOT EXISTS vaccines
(
    id serial not null
        constraint vaccines_pk
            primary key,
    name varchar(50) not null
);;

create unique index if not exists  vaccines_id_uindex
    on vaccines (id);;

create index if not exists vaccines_name__index
    on vaccines (name);;

create table IF NOT EXISTS tags
(
    id serial not null
        constraint tags_pk
            primary key,
    name varchar(15) not null
);;

create unique index if not exists  tags_id_uindex
    on tags (id);;

create index if not exists tags_name__index
    on tags (name);;

create table IF NOT EXISTS pets_vaccines
(
    id_pet varchar(36) not null
        constraint pets_vaccines_pets_id_fk
            references pets,
    id_vaccine integer not null
        constraint pets_vaccines_vaccines_id_fk
            references vaccines,
    constraint pets_vaccines_pk
        primary key (id_pet, id_vaccine)
);;

create table IF NOT EXISTS pets_tags
(
    id_pet varchar(36) not null
        constraint pets_tags_pets_id_fk
            references pets,
    id_tag integer not null
        constraint pets_tags_tags_id_fk
            references tags,
    constraint pets_tags_pk
        primary key (id_pet, id_tag)
);;

create or replace function insert_category(IN category_name varchar(15), OUT category_id int)
LANGUAGE plpgsql
AS $$
BEGIN

    INSERT
    INTO categories (name)
    SELECT category_name
    WHERE category_name NOT IN
    (
        SELECT name
        FROM categories
    );

    SELECT id into category_id
    FROM
        categories
    WHERE
        name = category_name;

END;
$$;;

create or replace function insert_breed(IN breed_name varchar(15), OUT breed_id int)
LANGUAGE plpgsql
AS $$
BEGIN

    INSERT
    INTO breeds (name)
    SELECT breed_name
    WHERE breed_name NOT IN
    (
        SELECT name
        FROM breeds
    );

    SELECT id into breed_id
    FROM
        breeds
    WHERE
        name = breed_name;

END;
$$;;

create or replace function insert_vaccine(IN vaccine_name varchar(15), OUT vaccine_id int)
LANGUAGE plpgsql
AS $$
BEGIN

    INSERT
    INTO vaccines (name)
    SELECT vaccine_name
    WHERE vaccine_name NOT IN
    (
        SELECT name
        FROM vaccines
    );

    SELECT id into vaccine_id
    FROM
        vaccines
    WHERE
        name = vaccine_name;

END;
$$;;

create or replace function insert_tag(IN tag_name varchar(15), OUT tag_id int)
LANGUAGE plpgsql
AS $$
BEGIN

    INSERT
    INTO tags (name)
    SELECT tag_name
    WHERE tag_name NOT IN
    (
        SELECT name
        FROM tags
    );

    SELECT id into tag_id
    FROM
        tags
    WHERE
        name = tag_name;

END;
$$;;
