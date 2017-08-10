/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.json.model;

import java.util.UUID;

/**
 *
 * @author Alejandro Rodriguez
 */
public class LangMembership extends Component {

    private String descriptionId;
	private LightConceptDescriptor languageReferenceSet;
    private LightConceptDescriptor acceptability;

	public LightConceptDescriptor getAcceptability() {
		return acceptability;
	}

	public void setAcceptability(LightConceptDescriptor acceptability) {
		this.acceptability = acceptability;
	}

    public LangMembership() {
        super();
    }

    public String getDescriptionId() {
        return descriptionId;
    }

    public void setDescriptionId(String descriptionId) {
        this.descriptionId = descriptionId;
    }

	public LightConceptDescriptor getLanguageReferenceSet() {
		return languageReferenceSet;
	}

	public void setLanguageReferenceSet(LightConceptDescriptor languageReferenceSet) {
		this.languageReferenceSet = languageReferenceSet;
	}

}
