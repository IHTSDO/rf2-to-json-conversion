/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.json.model;

import java.util.List;

/**
 *
 * @author alo
 */
public class TextIndexDescription extends Description {
    
    private String fsn;
    private String semanticTag;
    private boolean conceptActive;
    private Long typeId;
    private List<String> words;
    private List<Long> refsetIds;
    String definitionStatus;

    public TextIndexDescription() {
    }
    
    private Long conceptModule;

	public Long getConceptModule() {
		return conceptModule;
	}

	public void setConceptModule(Long conceptModule) {
		this.conceptModule = conceptModule;
	}
    public String getFsn() {
        return fsn;
    }

    public void setFsn(String fsn) {
        this.fsn = fsn;
    }

    public String getSemanticTag() {
        return semanticTag;
    }

    public void setSemanticTag(String semanticTag) {
        this.semanticTag = semanticTag;
    }

    public boolean isConceptActive() {
        return conceptActive;
    }

    public void setConceptActive(boolean conceptActive) {
        this.conceptActive = conceptActive;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public List<Long> getRefsetIds() {
        return refsetIds;
    }

    public void setRefsetIds(List<Long> refsetIds) {
        this.refsetIds = refsetIds;
    }

    public String getDefinitionStatus() {
        return definitionStatus;
    }

    public void setDefinitionStatus(String definitionStatus) {
        this.definitionStatus = definitionStatus;
    }
}
