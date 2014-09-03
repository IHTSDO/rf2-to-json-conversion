package org.ihtsdo.json.model;

import java.util.List;

public class ConceptAncestor {

	private String conceptId;
	private List<String> ancestor;
	
	public String getConceptId() {
		return conceptId;
	}
	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}
	public List<String> getAncestor() {
		return ancestor;
	}
	public void setAncestor(List<String> ancestor) {
		this.ancestor = ancestor;
	}
}
