package org.ihtsdo.json.model;

public class ConceptDescriptor extends Component{

    String conceptId;
	String defaultTerm;
    String definitionStatus;
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
	
}
