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
    private String stringModule;
    private String typeId;
    private List<String> refsetIds;
    String definitionStatus;

    public TextIndexDescription() {
    }
    
    private String conceptModule;

	public String getConceptModule() {
		return conceptModule;
	}

	public void setConceptModule(String conceptModule) {
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

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public List<String> getRefsetIds() {
        return refsetIds;
    }

    public void setRefsetIds(List<String> refsetIds) {
        this.refsetIds = refsetIds;
    }

    public String getDefinitionStatus() {
        return definitionStatus;
    }

    public void setDefinitionStatus(String definitionStatus) {
        this.definitionStatus = definitionStatus;
    }

	public String getStringModule() {
		return stringModule;
	}

	public void setStringModule(String stringModule) {
		this.stringModule = stringModule;
	}
}
