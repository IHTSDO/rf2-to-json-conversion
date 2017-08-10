package org.ihtsdo.json.model;

import java.util.List;

/**
 *
 * @author Alejandro Rodriguez
 */

public class Relationship extends Component {

	private String relationshipId;
    private LightConceptDescriptor type;
    private List<String> typeInferredAncestors;
    private List<String> typeStatedAncestors;
    private ConceptDescriptor destination;
    private List<String> targetInferredAncestors;
    private List<String> targetStatedAncestors;
    private String sourceId;
    private Integer relationshipGroup;
    private LightConceptDescriptor characteristicType;
    private LightConceptDescriptor modifier;
    private List<String> targetMemberships;

    public Relationship() {
        super();
    }

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
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

	public List<String> getTargetMemberships() {
		return targetMemberships;
	}

	public void setTargetMemberships(List<String> targetMemberships) {
		this.targetMemberships = targetMemberships;
	}

	public String getRelationshipId() {
		return relationshipId;
	}

	public void setRelationshipId(String relationshipId) {
		this.relationshipId = relationshipId;
	}

	public LightConceptDescriptor getType() {
		return type;
	}

	public void setType(LightConceptDescriptor type) {
		this.type = type;
	}

	public ConceptDescriptor getDestination() {
		return destination;
	}

	public void setDestination(ConceptDescriptor destination) {
		this.destination = destination;
	}

	public Integer getRelationshipGroup() {
		return relationshipGroup;
	}

	public void setRelationshipGroup(Integer relationshipGroup) {
		this.relationshipGroup = relationshipGroup;
	}

	public LightConceptDescriptor getCharacteristicType() {
		return characteristicType;
	}

	public void setCharacteristicType(LightConceptDescriptor characteristicType) {
		this.characteristicType = characteristicType;
	}

	public LightConceptDescriptor getModifier() {
		return modifier;
	}

	public void setModifier(LightConceptDescriptor modifier) {
		this.modifier = modifier;
	}
}
