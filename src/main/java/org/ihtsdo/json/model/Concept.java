/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.json.model;

import java.util.List;

/**
 *
 * @author Alejandro Rodriguez
 */
public class Concept extends ConceptDescriptor {
    
    List<RefsetMembership> memberships;
    List<Description> descriptions;
    List<Relationship> relationships;
    List<Relationship> statedRelationships;
    boolean isLeafInferred;
    boolean isLeafStated;
    String fsn;

    String semtag;
    List<String> inferredAncestors;
    List<String> statedAncestors;

    public Concept() {
    }

	public List<RefsetMembership> getMemberships() {
		return memberships;
	}

	public void setMemberships(List<RefsetMembership> memberships) {
		this.memberships = memberships;
	}

	public List<Description> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<Description> descriptions) {
		this.descriptions = descriptions;
	}

	public List<Relationship> getRelationships() {
		return relationships;
	}

	public void setRelationships(List<Relationship> relationships) {
		this.relationships = relationships;
	}

	public List<Relationship> getStatedRelationships() {
		return statedRelationships;
	}

	public void setStatedRelationships(List<Relationship> statedRelationships) {
		this.statedRelationships = statedRelationships;
	}

    public boolean isLeafInferred() {
        return isLeafInferred;
    }

    public void setLeafInferred(boolean isLeafInferred) {
        this.isLeafInferred = isLeafInferred;
    }

    public boolean isLeafStated() {
        return isLeafStated;
    }

    public void setLeafStated(boolean isLeafStated) {
        this.isLeafStated = isLeafStated;
    }

    public String getFsn() {
        return fsn;
    }

    public void setFsn(String fsn) {
        this.fsn = fsn;
    }

    public String getSemtag() {
        return semtag;
    }

    public void setSemtag(String semtag) {
        this.semtag = semtag;
    }

    public List<String> getInferredAncestors() {
        return inferredAncestors;
    }

    public void setInferredAncestors(List<String> inferredAncestors) {
        this.inferredAncestors = inferredAncestors;
    }

    public List<String> getStatedAncestors() {
        return statedAncestors;
    }

    public void setStatedAncestors(List<String> statedAncestors) {
        this.statedAncestors = statedAncestors;
    }
}
