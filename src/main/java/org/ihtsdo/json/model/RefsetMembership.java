package org.ihtsdo.json.model;

/**
 *
 * @author Alejandro Rodriguez
 */

public class RefsetMembership extends Component {
	
	public enum RefsetMembershipType {
		SIMPLEMAP("SIMPLEMAP"), ATTRIBUTE_VALUE("ATTRIBUTE_VALUE"), ASSOCIATION("ASSOCIATION"), SIMPLE_REFSET("SIMPLE_REFSET");
		private String type;
		
		RefsetMembershipType(String type) {
			this.type = type;
		}
		
		public String getType() {
			return this.type;
		}
		
		@Override
		public String toString() {
			return this.type;
		}
	}
	private String type;
	private String referencedComponentId;
	private LightConceptDescriptor refset;
	private LightConceptDescriptor cidValue;
	private String otherValue;


	public RefsetMembership() {
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOtherValue() {
		return otherValue;
	}

	public void setOtherValue(String otherValue) {
		this.otherValue = otherValue;
	}

	public String getReferencedComponentId() {
		return referencedComponentId;
	}

	public void setReferencedComponentId(String referencedComponentId) {
		this.referencedComponentId = referencedComponentId;
	}

	public LightConceptDescriptor getRefset() {
		return refset;
	}

	public void setRefset(LightConceptDescriptor refset) {
		this.refset = refset;
	}

	public LightConceptDescriptor getCidValue() {
		return cidValue;
	}

	public void setCidValue(LightConceptDescriptor cidValue) {
		this.cidValue = cidValue;
	}

}
