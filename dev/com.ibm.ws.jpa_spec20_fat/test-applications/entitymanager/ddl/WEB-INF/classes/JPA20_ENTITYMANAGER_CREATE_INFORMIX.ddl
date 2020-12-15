CREATE TABLE ${schemaname}.EMDETACH_ENTAM2MLIST (JPA20ENTITYMANAGERDETACHENTITY_ID INTEGER, ENTAM2MLIST_ID INTEGER) LOCK MODE ROW;
CREATE TABLE ${schemaname}.EMDETACH_ENTAM2MLIST_CA (JPA20ENTITYMANAGERDETACHENTITY_ID INTEGER, ENTAM2MLIST_CA_ID INTEGER) LOCK MODE ROW;
CREATE TABLE ${schemaname}.EMDETACH_ENTAM2MLIST_CD (JPA20ENTITYMANAGERDETACHENTITY_ID INTEGER, ENTAM2MLIST_CD_ID INTEGER) LOCK MODE ROW;
CREATE TABLE ${schemaname}.EMDETACH_ENTAO2MLIST (JPA20ENTITYMANAGERDETACHENTITY_ID INTEGER, ENTAO2MLIST_ID INTEGER) LOCK MODE ROW;
CREATE TABLE ${schemaname}.EMDETACH_ENTAO2MLIST_CA (JPA20ENTITYMANAGERDETACHENTITY_ID INTEGER, ENTAO2MLIST_CA_ID INTEGER) LOCK MODE ROW;
CREATE TABLE ${schemaname}.EMDETACH_ENTAO2MLIST_CD (JPA20ENTITYMANAGERDETACHENTITY_ID INTEGER, ENTAO2MLIST_CD_ID INTEGER) LOCK MODE ROW;
CREATE TABLE ${schemaname}.JPA20EntityManagerDetachEntity (id INTEGER NOT NULL, strData VARCHAR(255), ENTAM2O_ID INTEGER, ENTAM2O_CA_ID INTEGER, ENTAM2O_CD_ID INTEGER, ENTAO2O_ID INTEGER, ENTAO2O_CA_ID INTEGER, ENTAO2O_CD_ID INTEGER, PRIMARY KEY (id)) LOCK MODE ROW;
CREATE TABLE ${schemaname}.JPA20EntityManagerEntityA (id INTEGER NOT NULL, strData VARCHAR(255), PRIMARY KEY (id)) LOCK MODE ROW;
CREATE TABLE ${schemaname}.JPA20EntityManagerEntityA_JPA20EntityManagerEntityB (ENTITYALIST_ID INTEGER, ENTITYBLIST_ID INTEGER) LOCK MODE ROW;
CREATE TABLE ${schemaname}.JPA20EntityManagerEntityB (id INTEGER NOT NULL, strData VARCHAR(255), PRIMARY KEY (id)) LOCK MODE ROW;
CREATE TABLE ${schemaname}.JPA20EntityManagerEntityC (id INTEGER NOT NULL, strData VARCHAR(255), ENTITYA_ID INTEGER, ENTITYALAZY_ID INTEGER, PRIMARY KEY (id)) LOCK MODE ROW;
CREATE TABLE ${schemaname}.JPA20EntityManagerFindEntity (id INTEGER NOT NULL, firstName VARCHAR(255), lastName VARCHAR(255), vacationDays INTEGER, PRIMARY KEY (id)) LOCK MODE ROW;
CREATE INDEX ${schemaname}.I_MDTCLST_ELEMENT ON ${schemaname}.EMDETACH_ENTAM2MLIST (ENTAM2MLIST_ID);
CREATE INDEX ${schemaname}.I_MDTCLST_JPA20ENTITYMANAGERDETACHENTITY_ID ON ${schemaname}.EMDETACH_ENTAM2MLIST (JPA20ENTITYMANAGERDETACHENTITY_ID);
CREATE INDEX ${schemaname}.I_MDTCT_C_ELEMENT ON ${schemaname}.EMDETACH_ENTAM2MLIST_CA (ENTAM2MLIST_CA_ID);
CREATE INDEX ${schemaname}.I_MDTCT_C_JPA20ENTITYMANAGERDETACHENTITY_ID ON ${schemaname}.EMDETACH_ENTAM2MLIST_CA (JPA20ENTITYMANAGERDETACHENTITY_ID);
CREATE INDEX ${schemaname}.I_MDTC_CD_ELEMENT ON ${schemaname}.EMDETACH_ENTAM2MLIST_CD (ENTAM2MLIST_CD_ID);
CREATE INDEX ${schemaname}.I_MDTC_CD_JPA20ENTITYMANAGERDETACHENTITY_ID ON ${schemaname}.EMDETACH_ENTAM2MLIST_CD (JPA20ENTITYMANAGERDETACHENTITY_ID);
CREATE INDEX ${schemaname}.I_MDTCLST_ELEMENT1 ON ${schemaname}.EMDETACH_ENTAO2MLIST (ENTAO2MLIST_ID);
CREATE INDEX ${schemaname}.I_MDTCLST_JPA20ENTITYMANAGERDETACHENTITY_ID1 ON ${schemaname}.EMDETACH_ENTAO2MLIST (JPA20ENTITYMANAGERDETACHENTITY_ID);
CREATE INDEX ${schemaname}.I_MDTCT_C_ELEMENT1 ON ${schemaname}.EMDETACH_ENTAO2MLIST_CA (ENTAO2MLIST_CA_ID);
CREATE INDEX ${schemaname}.I_MDTCT_C_JPA20ENTITYMANAGERDETACHENTITY_ID1 ON ${schemaname}.EMDETACH_ENTAO2MLIST_CA (JPA20ENTITYMANAGERDETACHENTITY_ID);
CREATE INDEX ${schemaname}.I_MDTC_CD_ELEMENT1 ON ${schemaname}.EMDETACH_ENTAO2MLIST_CD (ENTAO2MLIST_CD_ID);
CREATE INDEX ${schemaname}.I_MDTC_CD_JPA20ENTITYMANAGERDETACHENTITY_ID1 ON ${schemaname}.EMDETACH_ENTAO2MLIST_CD (JPA20ENTITYMANAGERDETACHENTITY_ID);
CREATE INDEX ${schemaname}.I_JP20TTY_ENTAM2O ON ${schemaname}.JPA20EntityManagerDetachEntity (ENTAM2O_ID);
CREATE INDEX ${schemaname}.I_JP20TTY_ENTAM2O_CA ON ${schemaname}.JPA20EntityManagerDetachEntity (ENTAM2O_CA_ID);
CREATE INDEX ${schemaname}.I_JP20TTY_ENTAM2O_CD ON ${schemaname}.JPA20EntityManagerDetachEntity (ENTAM2O_CD_ID);
CREATE INDEX ${schemaname}.I_JP20TTY_ENTAO2O ON ${schemaname}.JPA20EntityManagerDetachEntity (ENTAO2O_ID);
CREATE INDEX ${schemaname}.I_JP20TTY_ENTAO2O_CA ON ${schemaname}.JPA20EntityManagerDetachEntity (ENTAO2O_CA_ID);
CREATE INDEX ${schemaname}.I_JP20TTY_ENTAO2O_CD ON ${schemaname}.JPA20EntityManagerDetachEntity (ENTAO2O_CD_ID);
CREATE INDEX ${schemaname}.I_JP20TYB_ELEMENT ON ${schemaname}.JPA20EntityManagerEntityA_JPA20EntityManagerEntityB (ENTITYBLIST_ID);
CREATE INDEX ${schemaname}.I_JP20TYB_ENTITYALIST_ID ON ${schemaname}.JPA20EntityManagerEntityA_JPA20EntityManagerEntityB (ENTITYALIST_ID);
CREATE INDEX ${schemaname}.I_JP20TYC_ENTITYA ON ${schemaname}.JPA20EntityManagerEntityC (ENTITYA_ID);
CREATE INDEX ${schemaname}.I_JP20TYC_ENTITYALAZY ON ${schemaname}.JPA20EntityManagerEntityC (ENTITYALAZY_ID);