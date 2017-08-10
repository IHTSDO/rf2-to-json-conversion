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

    private String descriptionId;
	private String refset;
    private String acceptability;
    private String stringModule;
	public String getDescriptionId() {
		return descriptionId;
	}
	public void setDescriptionId(String descriptionId) {
		this.descriptionId = descriptionId;
	}
	public String getRefset() {
		return refset;
	}
	public void setRefset(String refset) {
		this.refset = refset;
	}
	public String getAcceptability() {
		return acceptability;
	}
	public void setAcceptability(String acceptability) {
		this.acceptability = acceptability;
	}
	public String getStringModule() {
		return stringModule;
	}
	public void setStringModule(String stringModule) {
		this.stringModule = stringModule;
	}
    
}
