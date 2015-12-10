package org.ihtsdo.json.model;

public class ConceptDescriptor extends Component{

    String conceptId;
	String defaultTerm;
    String definitionStatus;
	int statedDescendants;
	int inferredDescendants;
	public String getConceptId() {
		return conceptId;
	}
	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}
	public String getDefaultTerm() {
		return defaultTerm;
	}
	public void setDefaultTerm(String defaultTerm) {
		this.defaultTerm = defaultTerm;
	}
	public String getDefinitionStatus() {
		return definitionStatus;
	}
	public void setDefinitionStatus(String definitionStatus) {
		this.definitionStatus = definitionStatus;
	}
	public int getStatedDescendants() {
		return statedDescendants;
	}
	public void setStatedDescendants(int statedDescendants) {
		this.statedDescendants = statedDescendants;
	}
	public int getInferredDescendants() {
		return inferredDescendants;
	}
	public void setInferredDescendants(int inferredDescendants) {
		this.inferredDescendants = inferredDescendants;
	}
	
}
