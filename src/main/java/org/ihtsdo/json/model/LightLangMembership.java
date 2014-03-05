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
public class LightLangMembership extends Component {

    private Long descriptionId;	
	private Long refset;
    private Long acceptability;
	public Long getDescriptionId() {
		return descriptionId;
	}
	public void setDescriptionId(Long descriptionId) {
		this.descriptionId = descriptionId;
	}
	public Long getRefset() {
		return refset;
	}
	public void setRefset(Long refset) {
		this.refset = refset;
	}
	public Long getAcceptability() {
		return acceptability;
	}
	public void setAcceptability(Long acceptability) {
		this.acceptability = acceptability;
	}
    
}
