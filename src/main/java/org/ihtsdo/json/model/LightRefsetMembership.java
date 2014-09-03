package org.ihtsdo.json.model;

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
	private String referencedComponentId;
	private String refset;
	private String cidValue;
	private String otherValue;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getReferencedComponentId() {
		return referencedComponentId;
	}
	public void setReferencedComponentId(String referencedComponentId) {
		this.referencedComponentId = referencedComponentId;
	}
	public String getRefset() {
		return refset;
	}
	public void setRefset(String refset) {
		this.refset = refset;
	}
	public String getCidValue() {
		return cidValue;
	}
	public void setCidValue(String cidValue) {
		this.cidValue = cidValue;
	}
	public String getOtherValue() {
		return otherValue;
	}
	public void setOtherValue(String otherValue) {
		this.otherValue = otherValue;
	}

}
