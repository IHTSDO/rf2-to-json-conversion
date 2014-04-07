package org.ihtsdo.json.model.versioned;

public class ConceptDescriptor extends Component{

	Long conceptId;
	String defaultTerm;
    String definitionStatus;
	public Long getConceptId() {
		return conceptId;
	}
	public void setConceptId(Long conceptId) {
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
	
}
