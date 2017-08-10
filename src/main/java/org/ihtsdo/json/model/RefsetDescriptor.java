package org.ihtsdo.json.model;


/**
 * Created by alo on 9/3/14.
 */
public class RefsetDescriptor extends ConceptDescriptor {

    Integer count;
    String type;

    public RefsetDescriptor(ConceptDescriptor concept, Integer count) {
        this.setDefinitionStatus(concept.getDefinitionStatus());
        this.setEffectiveTime(concept.getEffectiveTime());
        this.setCount(count);
        this.setConceptId(concept.getConceptId());
        this.setPreferredTerm(concept.getPreferredTerm());
        this.setModule(concept.getModule());
        this.setUuid(concept.getUuid());
        this.setActive(concept.isActive());
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

