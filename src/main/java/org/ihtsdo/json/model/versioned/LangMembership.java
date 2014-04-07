/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.json.model.versioned;

import java.util.UUID;

/**
 *
 * @author Alejandro Rodriguez
 */
public class LangMembership extends Component {

    private Long descriptionId;	
	private ConceptDescriptor refset;
    private ConceptDescriptor acceptability;
    
	public ConceptDescriptor getRefset() {
		return refset;
	}

	public void setRefset(ConceptDescriptor refset) {
		this.refset = refset;
	}

	public ConceptDescriptor getAcceptability() {
		return acceptability;
	}

	public void setAcceptability(ConceptDescriptor acceptability) {
		this.acceptability = acceptability;
	}


    public LangMembership() {
        super();
    }

    public Long getDescriptionId() {
        return descriptionId;
    }

    public void setDescriptionId(Long descriptionId) {
        this.descriptionId = descriptionId;
    }

}
