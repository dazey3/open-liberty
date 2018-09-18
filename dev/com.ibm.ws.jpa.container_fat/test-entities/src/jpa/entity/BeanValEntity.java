package jpa.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
public class BeanValEntity {
    private static final long serialVersionUID = 2772367752319571113L;

    public static final int PrePersist = 0x01;
    public static final int PreUpdate = 0x02;
    public static final int PreRemove = 0x04;
    public transient int mappedValidationState;

    @Id
    private int id;

    @Version
    private int version;

    @NotNull(message = "ValEntity.name is null")
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
