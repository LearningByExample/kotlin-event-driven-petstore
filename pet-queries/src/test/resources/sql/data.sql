INSERT INTO breeds(id, name)
VALUES (1, 'german shepherd');

INSERT INTO categories(id, name)
VALUES (1, 'dog');

INSERT INTO vaccines(id, name)
VALUES (1, 'vaccine1'),
       (2, 'vaccine2'),
       (3, 'vaccine3');

INSERT INTO pets (id, name, id_category, id_breed, dob)
VALUES ('4cb5294b-1034-4bc4-9b3d-542adb232a21', 'fluffy', 1, 1, '2020-08-09 10:35:07.981845');

INSERT INTO pets_vaccines(id_pet, id_vaccine)
VALUES ('4cb5294b-1034-4bc4-9b3d-542adb232a21', 1),
       ('4cb5294b-1034-4bc4-9b3d-542adb232a21', 2),
       ('4cb5294b-1034-4bc4-9b3d-542adb232a21', 3);
