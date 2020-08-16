-- Insert the breed we use in the tests
INSERT INTO breeds(id, name)
VALUES (1, 'german shepherd');

-- Insert the category we use in the tests
INSERT INTO categories(id, name)
VALUES (1, 'dog');

-- Insert the vaccines we use in the tests
INSERT INTO vaccines(id, name)
VALUES (1, 'vaccine1'),
       (2, 'vaccine2'),
       (3, 'vaccine3');

-- Insert the tags we use in the tests
INSERT INTO tags(id, name)
VALUES (1, 'brown'),
       (2, 'small');

-- Insert a pet with vaccines
INSERT INTO pets (id, name, id_category, id_breed, dob)
VALUES ('4cb5294b-1034-4bc4-9b3d-542adb232a21', 'fluffy', 1, 1, '2020-08-09 10:35:07.981845');

INSERT INTO pets_vaccines(id_pet, id_vaccine)
VALUES ('4cb5294b-1034-4bc4-9b3d-542adb232a21', 1),
       ('4cb5294b-1034-4bc4-9b3d-542adb232a21', 2),
       ('4cb5294b-1034-4bc4-9b3d-542adb232a21', 3);

-- Insert pet without vaccines
INSERT INTO pets (id, name, id_category, id_breed, dob)
VALUES ('4cb529ab-1034-4bc4-9b3d-542bdb232b21', 'lion', 1, 1, '2020-08-09 10:35:07.981845');

-- Insert a pet with vaccines and tags
INSERT INTO pets (id, name, id_category, id_breed, dob)
VALUES ('4cb5294b-1034-abcd-9b3d-542adb232a21', 'snowball', 1, 1, '2020-08-09 10:35:07.981845');

INSERT INTO pets_vaccines(id_pet, id_vaccine)
VALUES ('4cb5294b-1034-abcd-9b3d-542adb232a21', 1),
       ('4cb5294b-1034-abcd-9b3d-542adb232a21', 2);

INSERT INTO pets_tags(id_pet, id_tag)
VALUES ('4cb5294b-1034-abcd-9b3d-542adb232a21', 1),
       ('4cb5294b-1034-abcd-9b3d-542adb232a21', 2);
