package org.ihtsdo.json.model;

/**
 *
 * @author Alejandro Rodriguez
 */

public class LightRelationship extends Component {

    private Long type;
    private Long target;
    private Long sourceId;
    private Integer groupId;
    private Long charType;
    private Long modifier;
	public Long getType() {
		return type;
	}
	public void setType(Long type) {
		this.type = type;
	}
	public Long getTarget() {
		return target;
	}
	public void setTarget(Long target) {
		this.target = target;
	}
	public Long getSourceId() {
		return sourceId;
	}
	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public Long getCharType() {
		return charType;
	}
	public void setCharType(Long charType) {
		this.charType = charType;
	}
	public Long getModifier() {
		return modifier;
	}
	public void setModifier(Long modifier) {
		this.modifier = modifier;
	}



}
