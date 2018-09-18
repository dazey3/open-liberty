/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2014
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package jpa.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@EntityListeners({ WidgetEntityListener.class })
public class Widget {

    private int id;

    private String name;

    private String description;

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    @Id
    @GeneratedValue
    public int getId() {
        return id;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    @Column
    public String getName() {
        return name;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the description
     */
    @Column
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Widget) {
            Widget widget = (Widget) obj;
            if (getId() == widget.getId() &&
                getName().equals(widget.getName()) &&
                getDescription().equals(widget.getDescription())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[id=" + id + ", name=" + name + ", description=" + description + ']';
    }
}
