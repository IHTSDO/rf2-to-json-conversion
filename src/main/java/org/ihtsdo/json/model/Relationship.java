package org.ihtsdo.json.model;

import java.util.List;

/**
 *
 * @author Alejandro Rodriguez
 */

public class Relationship extends Component {

    private ConceptDescriptor type;
    private List<String> typeInferredAncestors;
    private List<String> typeStatedAncestors;
    private ConceptDescriptor target;
    private List<String> targetInferredAncestors;
    private List<String> targetStatedAncestors;
    private String sourceId;
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

    public List<String> getTypeInferredAncestors() {
        return typeInferredAncestors;
    }

    public void setTypeInferredAncestors(List<String> typeInferredAncestors) {
        this.typeInferredAncestors = typeInferredAncestors;
    }

    public List<String> getTypeStatedAncestors() {
        return typeStatedAncestors;
    }

    public void setTypeStatedAncestors(List<String> typeStatedAncestors) {
        this.typeStatedAncestors = typeStatedAncestors;
    }

    public List<String> getTargetInferredAncestors() {
        return targetInferredAncestors;
    }

    public void setTargetInferredAncestors(List<String> targetInferredAncestors) {
        this.targetInferredAncestors = targetInferredAncestors;
    }

    public List<String> getTargetStatedAncestors() {
        return targetStatedAncestors;
    }

    public void setTargetStatedAncestors(List<String> targetStatedAncestors) {
        this.targetStatedAncestors = targetStatedAncestors;
    }
}
