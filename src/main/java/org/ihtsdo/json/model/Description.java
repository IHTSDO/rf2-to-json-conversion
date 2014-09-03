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
    ConceptDescriptor type;
    String lang;
    String term;
    Integer length;
    ConceptDescriptor ics;
    List<LangMembership> langMemberships;
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

    public ConceptDescriptor getType() {
        return type;
    }

    public void setType(ConceptDescriptor type) {
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

    public ConceptDescriptor getIcs() {
        return ics;
    }

    public void setIcs(ConceptDescriptor ics) {
        this.ics = ics;
    }

    public List<LangMembership> getLangMemberships() {
        return langMemberships;
    }

    public void setLangMemberships(List<LangMembership> langMemberships) {
        this.langMemberships = langMemberships;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
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
}
