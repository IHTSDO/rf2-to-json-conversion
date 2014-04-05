package org.ihtsdo.json.model;

import java.util.List;

public class ConceptAncestor {

	private Long conceptId;
	private List<Long> ancestor;
	
	public Long getConceptId() {
		return conceptId;
	}
	public void setConceptId(Long conceptId) {
		this.conceptId = conceptId;
	}
	public List<Long> getAncestor() {
		return ancestor;
	}
	public void setAncestor(List<Long> ancestor) {
		this.ancestor = ancestor;
	}
}
