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
public class Description extends Component {

    String descriptionId;
    String conceptId;
    LightConceptDescriptor type;
    String languageCode;
    String term;
    Integer length;
    LightConceptDescriptor caseSignificance;
    List<LangMembership> acceptability;
    List<RefsetMembership> refsetMemberships;

    private List<String> words;

    public Description() {
    }

    public String getDescriptionId() {
        return descriptionId;
    }

    public void setDescriptionId(String descriptionId) {
        this.descriptionId = descriptionId;
    }

    public String getConceptId() {
        return conceptId;
    }

    public void setConceptId(String conceptId) {
        this.conceptId = conceptId;
    }

    public LightConceptDescriptor getType() {
        return type;
    }

    public void setType(LightConceptDescriptor type) {
        this.type = type;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public List<LangMembership> getLangMemberships() {
        return acceptability;
    }

    public void setLangMemberships(List<LangMembership> langMemberships) {
        this.acceptability = langMemberships;
    }

	public List<RefsetMembership> getRefsetMemberships() {
		return refsetMemberships;
	}

	public void setRefsetMemberships(List<RefsetMembership> refsetMemberships) {
		this.refsetMemberships = refsetMemberships;
	}

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public LightConceptDescriptor getCaseSignificance() {
		return caseSignificance;
	}

	public void setCaseSignificance(LightConceptDescriptor caseSignificance) {
		this.caseSignificance = caseSignificance;
	}
}
