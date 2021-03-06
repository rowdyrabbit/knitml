package com.knitml.core.model.common;



/**
 * @author Jonathan Whitall (fiddlerpianist@gmail.com)
 * 
 */
public class Needle implements Identifiable {

	protected String id;
	protected String messageKey;
	protected NeedleType type;
	protected String label;
	
	private String labelOverride;
	
	public void setLabel(String newLabel) {
		this.labelOverride = newLabel;
	}

	public String getLabel() {
		return labelOverride == null ? label : labelOverride;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public NeedleType getType() {
		return type;
	}

	public String getId() {
		return id;
	}
	
	public Needle() {
	}
	
	public Needle(String id, NeedleType type, String label, String messageKey) {
		this.id = id;
		this.type = type;
		this.label = label;
		this.messageKey = messageKey;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	@Override
	public String toString() {
		return getLabel();
	}
	
	public void setType(NeedleType type) {
		this.type = type;
	}

}
