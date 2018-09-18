package jpa.entity;

public class XMLEntity {
    private int id;
    private String strData;

    public XMLEntity() {

    }

    public XMLEntity(int id, String strData) {
        this.id = id;
        this.strData = strData;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStrData() {
        return strData;
    }

    public void setStrData(String strData) {
        this.strData = strData;
    }
}
