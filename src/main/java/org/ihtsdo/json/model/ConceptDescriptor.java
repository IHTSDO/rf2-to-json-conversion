package org.ihtsdo.json.model;

public class ConceptDescriptor extends Component{

    String conceptId;
	String preferredTerm;
    String fullySpecifiedName;
    LightConceptDescriptor definitionStatus;
	int statedDescendants;
	int inferredDescendants;
	public String getConceptId() {
		return conceptId;
	}
	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
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
	public String getPreferredTerm() {
		return preferredTerm;
	}
	public void setPreferredTerm(String preferredTerm) {
		this.preferredTerm = preferredTerm;
	}
	public LightConceptDescriptor getDefinitionStatus() {
		return definitionStatus;
	}
	public void setDefinitionStatus(LightConceptDescriptor definitionStatus) {
		this.definitionStatus = definitionStatus;
	}
	public String getFullySpecifiedName() {
		return fullySpecifiedName;
	}
	public void setFullySpecifiedName(String fullySpecifiedName) {
		this.fullySpecifiedName = fullySpecifiedName;
	}
	
}
