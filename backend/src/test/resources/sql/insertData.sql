-- insert initial test data
-- the IDs are hardcoded to enable references between further test data
-- negative IDs are used to not interfere with user-entered data and allow clean deletion of test data

DELETE FROM owner where id < 0;
DELETE FROM horse where id < 0;


INSERT INTO owner(id, first_name, last_name, email)
VALUES(-1, 'Owner1For', 'TestingPurpose', 'someone@example.cd');
INSERT INTO owner(id, first_name, last_name)
VALUES(-2, 'Owner2For', 'TestingPurpose');
INSERT INTO owner(id, first_name, last_name)
VALUES(-3, 'Owner3For', 'TestingPurpose');
INSERT INTO owner(id, first_name, last_name)
VALUES(-4, 'Owner4For', 'TestingPurpose');
INSERT INTO owner(id, first_name, last_name)
VALUES(-5, 'Owner5For', 'TestingPurpose');
INSERT INTO owner(id, first_name, last_name)
VALUES(-6, 'Owner6For', 'TestingPurpose');
INSERT INTO owner(id, first_name, last_name)
VALUES(-7, 'Owner7For', 'TestingPurpose');
INSERT INTO owner(id, first_name, last_name)
VALUES(-8, 'Owner8For', 'TestingPurpose');
INSERT INTO owner(id, first_name, last_name)
VALUES(-9, 'Owner9For', 'TestingPurpose');
INSERT INTO owner(id, first_name, last_name, email)
VALUES(-10, 'Owner10For', 'TestingPurpose', 'owner@owner.own');

INSERT INTO horse (id, name, description, date_of_birth, sex)
VALUES (-1, 'Wendy', 'The famous one!', '2012-12-12', 'FEMALE');
INSERT INTO horse (id, name, description, date_of_birth, sex)
VALUES (-2, 'SuitableMother', 'Lorem ipsum1', '2012-12-24', 'FEMALE');
INSERT INTO horse (id, name, description, date_of_birth, sex)
VALUES (-3, 'SuitableFather', 'Lorem ipsum2', '2012-12-24', 'MALE');
INSERT INTO horse (id, name, description, date_of_birth, sex)
VALUES (-4, 'SuitableChildForSuitableMotherAndSuitableFather', 'Lorem ipsum', '2013-12-24', 'MALE');
INSERT INTO horse (id, name, description, date_of_birth, sex)
VALUES (-5, 'HorseWithLongDescription', 'Lorem ipsum dolor sit amet, consectetur adipiscing elit,' ||
                                        ' sed do eiusmod tempor incididunt ut labore et dolore magna ' ||
                                        'aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco ' ||
                                        'laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor ' ||
                                        'in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. ' ||
                                        'Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit ' ||
                                        'anim id est laborum.', '2012-12-12', 'FEMALE');
INSERT INTO horse (id, name, description, date_of_birth, sex, mother_id, father_id, owner_id)
VALUES (-6, 'HorseGen2F', 'Lorem ipsum3', '2019-08-09', 'FEMALE', -2, -3, -1);
INSERT INTO horse (id, name, description, date_of_birth, sex, mother_id, father_id)
VALUES (-7, 'HorseGen2M', 'Lorem ipsum4', '2019-12-24', 'MALE', -2, -3);
INSERT INTO horse (id, name, description, date_of_birth, sex, mother_id, father_id)
VALUES (-8, 'HorseGen3F', 'Lorem ipsum5', '2020-12-24', 'FEMALE', -6, -7);
INSERT INTO horse (id, name,  date_of_birth, sex, mother_id, father_id)
VALUES (-9, 'HorseGen3M',  '2020-01-01', 'MALE', -6, -7);
INSERT INTO horse (id, name, description, date_of_birth, sex, owner_id, mother_id, father_id)
VALUES (-10, 'HorseGen4F', 'Lorem ipsum6', '2021-12-24', 'FEMALE', -1, -8, -9);

