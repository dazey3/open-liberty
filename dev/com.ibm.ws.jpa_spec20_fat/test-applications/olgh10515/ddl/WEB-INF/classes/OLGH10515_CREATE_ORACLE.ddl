CREATE TABLE ${schemaname}.criteria_car_origin (CAR_ID VARCHAR2(255), CAR_VER NUMBER, component VARCHAR2(255) NOT NULL, origin VARCHAR2(255));
CREATE TABLE ${schemaname}.SimpleEntityOLGH10515 (CAR_ID VARCHAR2(255) NOT NULL, CAR_VER NUMBER NOT NULL, PRIMARY KEY (CAR_ID, CAR_VER));
CREATE INDEX ${schemaname}.I_CRTRRGN_CAR_ID ON ${schemaname}.criteria_car_origin (CAR_ID, CAR_VER);