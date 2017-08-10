package org.ihtsdo.json.model;

/**
 *
 * @author Alejandro Rodriguez
 */

public class LightRelationship extends Component {

	private String relationshipId;
    private String type;
    private String target;
    private String sourceId;
    private String stringModule;
    private Integer groupId;
    private String charType;
    private String modifier;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public String getCharType() {
		return charType;
	}
	public void setCharType(String charType) {
		this.charType = charType;
	}
	public String getModifier() {
		return modifier;
	}
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
	public String getRelationshipId() {
		return relationshipId;
	}
	public void setRelationshipId(String relationshipId) {
		this.relationshipId = relationshipId;
	}
	public String getStringModule() {
		return stringModule;
	}
	public void setStringModule(String stringModule) {
		this.stringModule = stringModule;
	}



}
