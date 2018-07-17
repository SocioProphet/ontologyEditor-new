package com.rmdc.ontologyEditor.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="change_on"
        ,catalog="ontologyeditor"
)
public class ChangeOn implements java.io.Serializable{
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;
    private String type;
    @OneToMany(fetch=FetchType.LAZY, mappedBy="changeOn")
    private Set<OntoChange> ontoChanges = new HashSet(0);

    public ChangeOn() {
    }

    public ChangeOn(String type, Set ontoChanges) {
        this.type = type;
        this.ontoChanges = ontoChanges;
    }

    @Column(name="id", unique=true, nullable=false)
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    @Column(name="type", length=120)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public Set<OntoChange> getOntoChanges() {
        return this.ontoChanges;
    }

    public void setOntoChanges(Set<OntoChange> ontoChanges) {
        this.ontoChanges = ontoChanges;
    }
}
