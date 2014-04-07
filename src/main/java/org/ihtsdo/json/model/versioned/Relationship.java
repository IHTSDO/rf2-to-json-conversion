package org.ihtsdo.json.model.versioned;

/**
 *
 * @author Alejandro Rodriguez
 */

public class Relationship extends Component {

    private ConceptDescriptor type;
    private ConceptDescriptor target;
    private Long sourceId;
    private Integer groupId;
    private ConceptDescriptor charType;
    private String modifier;

    public Relationship() {
        super();
    }

	public ConceptDescriptor getType() {
		return type;
	}

	public void setType(ConceptDescriptor type) {
		this.type = type;
	}

	public ConceptDescriptor getTarget() {
		return target;
	}

	public void setTarget(ConceptDescriptor target) {
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

	public ConceptDescriptor getCharType() {
		return charType;
	}

	public void setCharType(ConceptDescriptor charType) {
		this.charType = charType;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}


}
