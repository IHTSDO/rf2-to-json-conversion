package org.ihtsdo.json.model;


/**
 * Created by alo on 9/3/14.
 */
public class RefsetDescriptor extends ConceptDescriptor {

    Integer count;

    public RefsetDescriptor(ConceptDescriptor concept, Integer count) {
        this.setDefinitionStatus(concept.getDefinitionStatus());
        this.setEffectiveTime(concept.getEffectiveTime());
        this.setCount(count);
        this.setConceptId(concept.getConceptId());
        this.setDefaultTerm(concept.getDefaultTerm());
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
}

