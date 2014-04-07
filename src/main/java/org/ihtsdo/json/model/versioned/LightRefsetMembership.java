package org.ihtsdo.json.model.versioned;

/**
 *
 * @author Alejandro Rodriguez
 */

public class LightRefsetMembership extends Component {
	
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
	private Long referencedComponentId;
	private Long refset;
	private Long cidValue;
	private String otherValue;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getReferencedComponentId() {
		return referencedComponentId;
	}
	public void setReferencedComponentId(Long referencedComponentId) {
		this.referencedComponentId = referencedComponentId;
	}
	public Long getRefset() {
		return refset;
	}
	public void setRefset(Long refset) {
		this.refset = refset;
	}
	public Long getCidValue() {
		return cidValue;
	}
	public void setCidValue(Long cidValue) {
		this.cidValue = cidValue;
	}
	public String getOtherValue() {
		return otherValue;
	}
	public void setOtherValue(String otherValue) {
		this.otherValue = otherValue;
	}

}
