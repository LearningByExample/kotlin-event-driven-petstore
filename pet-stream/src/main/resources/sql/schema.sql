CREATE TABLE IF NOT EXISTS categories
(
    id SERIAL NOT NULL CONSTRAINT categories_pk PRIMARY KEY,
    name VARCHAR(15) NOT NULL,
    creation TIMESTAMP DEFAULT NOW() NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS categories_name_INDEX ON categories (name);

CREATE UNIQUE INDEX IF NOT EXISTS categories_id_INDEX ON categories (id);

CREATE TABLE IF NOT EXISTS breeds
(
    id SERIAL NOT NULL CONSTRAINT breeds_pk PRIMARY KEY,
    name VARCHAR(25) NOT NULL,
    creation TIMESTAMP DEFAULT NOW() NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS breeds_id_INDEX ON breeds (id);

CREATE UNIQUE INDEX IF NOT EXISTS breed_name_INDEX ON breeds (name);

CREATE TABLE IF NOT EXISTS vaccines
(
    id SERIAL NOT NULL CONSTRAINT vaccines_pk PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    creation TIMESTAMP DEFAULT NOW() NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS vaccines_id_INDEX ON vaccines (id);

CREATE UNIQUE INDEX IF NOT EXISTS vaccines_name_INDEX ON vaccines (name);

CREATE TABLE IF NOT EXISTS tags
(
    id SERIAL NOT NULL CONSTRAINT tags_pk PRIMARY KEY,
    name VARCHAR(15) NOT NULL,
    creation TIMESTAMP DEFAULT NOW() NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS tags_id_uINDEX ON tags (id);

CREATE UNIQUE INDEX IF NOT EXISTS tags_name__INDEX ON tags (name);

CREATE TABLE IF NOT EXISTS pets
(
    id VARCHAR(36) NOT NULL CONSTRAINT pets_pk PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    id_category INTEGER NOT NULL CONSTRAINT pets_categories_id_fk REFERENCES categories,
    id_breed INTEGER NOT NULL CONSTRAINT pets_breeds_id_fk REFERENCES breeds,
    dob TIMESTAMP NOT NULL,
    creation TIMESTAMP DEFAULT NOW() NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS pets_id_INDEX ON pets (id);

CREATE TABLE IF NOT EXISTS pets_vaccines
(
    id_pet VARCHAR(36) NOT NULL CONSTRAINT pets_vaccines_pets_id_fk REFERENCES pets,
    id_vaccine INTEGER NOT NULL CONSTRAINT pets_vaccines_vaccines_id_fk REFERENCES vaccines,
    CONSTRAINT pets_vaccines_pk PRIMARY KEY (id_pet, id_vaccine),
    creation TIMESTAMP DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS pets_tags
(
    id_pet VARCHAR(36) NOT NULL CONSTRAINT pets_tags_pets_id_fk REFERENCES pets,
    id_tag INTEGER NOT NULL CONSTRAINT pets_tags_tags_id_fk REFERENCES tags,
    CONSTRAINT pets_tags_pk PRIMARY KEY (id_pet, id_tag),
    creation TIMESTAMP DEFAULT NOW() NOT NULL
);
