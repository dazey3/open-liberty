CREATE TABLE AttrOColE (id INTEGER NOT NULL, name VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE AttrOColE_nonInsertAnnoElem (ATTRIBUTEORDERCOLUMNENTITY_ID INTEGER, element VARCHAR(255), NONINSERTABLE_ORDER INTEGER);
CREATE TABLE AttrOColE_nonNullAnnoElem (ATTRIBUTEORDERCOLUMNENTITY_ID INTEGER, element VARCHAR(255), NONNULLABLE_ORDER INTEGER NOT NULL);
CREATE TABLE AttrOColE_nonUpdateAnnoElem (ATTRIBUTEORDERCOLUMNENTITY_ID INTEGER, element VARCHAR(255), NONUPDATABLE_ORDER INTEGER);
CREATE TABLE AttrOColE_oNameTypeElem (ATTRIBUTEORDERCOLUMNENTITY_ID INTEGER, element VARCHAR(255), orderNameTypeElements_ORDER INTEGER);
CREATE TABLE BONameE (id INTEGER NOT NULL, name VARCHAR(255), COLUMN_ID INTEGER, bo2mNames_ORDER INTEGER, PRIMARY KEY (id));
CREATE TABLE BONameXE (id INTEGER NOT NULL, name VARCHAR(255), XMLCOLUMN_ID INTEGER, bo2mNames_ORDER INTEGER, PRIMARY KEY (id));
CREATE TABLE DefOColE (id INTEGER NOT NULL, name VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE DefOColE_oColDefElem (DEFINITIONORDERCOLUMNENTITY_ID INTEGER, element VARCHAR(255), OCDefElements_ODR INTEGER);
CREATE TABLE DefOColE_ovrOColDefElem (DEFINITIONORDERCOLUMNENTITY_ID INTEGER, element VARCHAR(255), OVROCDefElements_ODR SMALLINT);
CREATE TABLE Diff_Table_Name (NAMETABLEORDERCOLUMNENTITY_ID INTEGER, element VARCHAR(255), Diff_OrderColumn_Name INTEGER);
CREATE TABLE NTblOColE (id INTEGER NOT NULL, name VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE NTblOColE_oNameTypeElem (NAMETABLEORDERCOLUMNENTITY_ID INTEGER, element VARCHAR(255), orderNameTypeElements_ORDER INTEGER);
CREATE TABLE OColE (id INTEGER NOT NULL, PRIMARY KEY (id));
CREATE TABLE OColE_BONameE (COLUMNS_ID INTEGER, BM2MNAMES_ID INTEGER, bm2mNames_ORDER INTEGER);
CREATE TABLE OColE_listElements (ORDERCOLUMNENTITY_ID INTEGER, element VARCHAR(255), OVRELEMENTS_ORDER INTEGER, LISTELEMENTS VARCHAR(255));
CREATE TABLE OColE_UONameE (ORDERCOLUMNENTITY_ID INTEGER, UM2MNAMES_ID INTEGER, um2mNames_ORDER INTEGER, UO2MNAMES_ID INTEGER, uo2mNames_ORDER INTEGER);
CREATE TABLE UONameE (id INTEGER NOT NULL, name VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE XOColE (id INTEGER NOT NULL, PRIMARY KEY (id));
CREATE TABLE XOColE_BONameXE (XMLCOLUMNS_ID INTEGER, BM2MNAMES_ID INTEGER, bm2mNames_ORDER INTEGER);
CREATE TABLE XOColE_listElements (XMLORDERCOLUMNENTITY_ID INTEGER, element VARCHAR(255), OVRELEMENTS_ORDER INTEGER, LISTELEMENTS VARCHAR(255));
CREATE TABLE XOColE_UONameE (XMLORDERCOLUMNENTITY_ID INTEGER, UM2MNAMES_ID INTEGER, um2mNames_ORDER INTEGER, UO2MNAMES_ID INTEGER, uo2mNames_ORDER INTEGER);
CREATE INDEX I_TTRCNLM_ATTRIBUTEORDERCOLUMNENTITY_ID ON AttrOColE_nonInsertAnnoElem (ATTRIBUTEORDERCOLUMNENTITY_ID);
CREATE INDEX I_TTRCNLM_ATTRIBUTEORDERCOLUMNENTITY_ID1 ON AttrOColE_nonNullAnnoElem (ATTRIBUTEORDERCOLUMNENTITY_ID);
CREATE INDEX I_TTRCNLM_ATTRIBUTEORDERCOLUMNENTITY_ID2 ON AttrOColE_nonUpdateAnnoElem (ATTRIBUTEORDERCOLUMNENTITY_ID);
CREATE INDEX I_TTRCPLM_ATTRIBUTEORDERCOLUMNENTITY_ID ON AttrOColE_oNameTypeElem (ATTRIBUTEORDERCOLUMNENTITY_ID);
CREATE INDEX I_BONAMEE_COLUMN ON BONameE (COLUMN_ID);
CREATE INDEX I_BONMEXE_XMLCOLUMN ON BONameXE (XMLCOLUMN_ID);
CREATE INDEX I_DFCLFLM_DEFINITIONORDERCOLUMNENTITY_ID ON DefOColE_oColDefElem (DEFINITIONORDERCOLUMNENTITY_ID);
CREATE INDEX I_DFCLFLM_DEFINITIONORDERCOLUMNENTITY_ID1 ON DefOColE_ovrOColDefElem (DEFINITIONORDERCOLUMNENTITY_ID);
CREATE INDEX I_DFF__NM_NAMETABLEORDERCOLUMNENTITY_ID ON Diff_Table_Name (NAMETABLEORDERCOLUMNENTITY_ID);
CREATE INDEX I_NTBLPLM_NAMETABLEORDERCOLUMNENTITY_ID ON NTblOColE_oNameTypeElem (NAMETABLEORDERCOLUMNENTITY_ID);
CREATE INDEX I_CL_BONM_COLUMNS_ID ON OColE_BONameE (COLUMNS_ID);
CREATE INDEX I_CL_BONM_ELEMENT ON OColE_BONameE (BM2MNAMES_ID);
CREATE INDEX I_CL_LNTS_ORDERCOLUMNENTITY_ID ON OColE_listElements (ORDERCOLUMNENTITY_ID);
CREATE INDEX I_CL_UONM_ELEMENT ON OColE_UONameE (UM2MNAMES_ID);
CREATE INDEX I_CL_UONM_ELEMENT1 ON OColE_UONameE (UO2MNAMES_ID);
CREATE INDEX I_CL_UONM_ORDERCOLUMNENTITY_ID ON OColE_UONameE (ORDERCOLUMNENTITY_ID);
CREATE INDEX I_XCL_NMX_ELEMENT ON XOColE_BONameXE (BM2MNAMES_ID);
CREATE INDEX I_XCL_NMX_XMLCOLUMNS_ID ON XOColE_BONameXE (XMLCOLUMNS_ID);
CREATE INDEX I_XCL_NTS_XMLORDERCOLUMNENTITY_ID ON XOColE_listElements (XMLORDERCOLUMNENTITY_ID);
CREATE INDEX I_XCL_UNM_ELEMENT ON XOColE_UONameE (UM2MNAMES_ID);
CREATE INDEX I_XCL_UNM_ELEMENT1 ON XOColE_UONameE (UO2MNAMES_ID);
CREATE INDEX I_XCL_UNM_XMLORDERCOLUMNENTITY_ID ON XOColE_UONameE (XMLORDERCOLUMNENTITY_ID);