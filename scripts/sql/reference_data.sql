--
-- Reference (pre-canned) data for AATAMS.
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET SESSION AUTHORIZATION 'aatams';

SET search_path = aatams, pg_catalog;

INSERT INTO project_role_type (id, version, display_name)
    VALUES (1, 0, 'Principal Investigator');
INSERT INTO project_role_type (id, version, display_name)
    VALUES (2, 0, 'Co-Investigator');
INSERT INTO project_role_type (id, version, display_name)
    VALUES (3, 0, 'Research Assistant');
INSERT INTO project_role_type (id, version, display_name)
    VALUES (4, 0, 'Technical Assistant');
INSERT INTO project_role_type (id, version, display_name)
    VALUES (5, 0, 'Administrator');
INSERT INTO project_role_type (id, version, display_name)
    VALUES (6, 0, 'Student');


INSERT INTO system_role_type (id, version, display_name)
    VALUES (1, 0, 'eMII employee');
INSERT INTO system_role_type (id, version, display_name)
    VALUES (2, 0, 'eMII administrator');
INSERT INTO system_role_type (id, version, display_name)
    VALUES (3, 0, 'system administrator');
INSERT INTO system_role_type (id, version, display_name)
    VALUES (4, 0, 'normal');

INSERT INTO device_status (id, version, status)
    VALUES (1, 0, 'NEW');
INSERT INTO device_status (id, version, status)
    VALUES (2, 0, 'DEPLOYED');
INSERT INTO device_status (id, version, status)
    VALUES (3, 0, 'RECOVERED');
INSERT INTO device_status (id, version, status)
    VALUES (4, 0, 'RETIRED');
INSERT INTO device_status (id, version, status)
    VALUES (5, 0, 'LOST');
INSERT INTO device_status (id, version, status)
    VALUES (6, 0, 'STOLEN');
INSERT INTO device_status (id, version, status)
    VALUES (7, 0, 'DAMAGED');

INSERT INTO transmitter_type (id, version, transmitter_type_name)
    VALUES (1, 0, 'PING');
INSERT INTO transmitter_type (id, version, transmitter_type_name)
    VALUES (2, 0, 'DEPTH');
INSERT INTO transmitter_type (id, version, transmitter_type_name)
    VALUES (3, 0, 'TEMPERATURE');
INSERT INTO transmitter_type (id, version, transmitter_type_name)
    VALUES (4, 0, 'ACCELEROMETER');

INSERT INTO installation_configuration (id, version, type)
    VALUES (1, 0, 'ARRAY');
INSERT INTO installation_configuration (id, version, type)
    VALUES (2, 0, 'CURTAIN');

INSERT INTO mooring_type (id, version, type)
    VALUES (1, 0, 'CAR TYRE');
INSERT INTO mooring_type (id, version, type)
    VALUES (2, 0, 'CONCRETE BLOCK');
INSERT INTO mooring_type (id, version, type)
    VALUES (3, 0, 'DEEP WATER');

INSERT INTO sex (id, version, sex)
    VALUES (1, 0, 'MALE');
INSERT INTO sex (id, version, sex)
    VALUES (2, 0, 'FEMALE');
INSERT INTO sex (id, version, sex)
    VALUES (3, 0, 'UNKNOWN');

INSERT INTO surgery_type (id, version, type)
    VALUES (1, 0, 'INTERNAL');
INSERT INTO surgery_type (id, version, type)
    VALUES (2, 0, 'EXTERNAL');

INSERT INTO surgery_treatment_type (id, version, type)
    VALUES (1, 0, 'ANTIBIOTIC');
INSERT INTO surgery_treatment_type (id, version, type)
    VALUES (2, 0, 'ANISTHETIC');

INSERT INTO animal_measurement_type (id, version, type)
    VALUES (1, 0, 'LENGTH');
INSERT INTO animal_measurement_type (id, version, type)
    VALUES (2, 0, 'WEIGHT');

INSERT INTO measurement_unit (id, version, unit)
    VALUES (1, 0, 'gm');
INSERT INTO measurement_unit (id, version, unit)
    VALUES (2, 0, 'mm');


