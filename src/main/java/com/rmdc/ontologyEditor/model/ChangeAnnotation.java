package com.rmdc.ontologyEditor.model;

import javax.persistence.*;

@Entity
@Table(name="change_annotation"
        ,catalog="ontologyeditor"
)
public class ChangeAnnotation  implements java.io.Serializable {

    @Id @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;
    @ManyToOne(fetch=FetchType.LAZY)
    private OntoChange ontoChange;
    private String annKey;
    private String annValue;
    private byte[] object;
    private String description;

    public ChangeAnnotation() {
    }

    public ChangeAnnotation(OntoChange changeDes, String annKey, String annValue, byte[] object, String description) {
        this.ontoChange = ontoChange;
        this.annKey = annKey;
        this.annValue = annValue;
        this.object = object;
        this.description = description;
    }


    @Column(name = "id", unique = true, nullable = false)
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_des_id", nullable = false)
    public OntoChange getOntoChange() {
        return this.ontoChange;
    }

    public void setOntoChange(OntoChange changeDes) {
        this.ontoChange = changeDes;
    }


    @Column(name = "ann_key", nullable = false, length = 64)
    public String getAnnKey() {
        return this.annKey;
    }

    public void setAnnKey(String annKey) {
        this.annKey = annKey;
    }


    @Column(name = "ann_value", nullable = false, length = 256)
    public String getAnnValue() {
        return this.annValue;
    }

    public void setAnnValue(String annValue) {
        this.annValue = annValue;
    }


    @Column(name = "object", nullable = false)
    public byte[] getObject() {
        return this.object;
    }

    public void setObject(byte[] object) {
        this.object = object;
    }


    @Column(name = "description", nullable = false, length = 256)
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}