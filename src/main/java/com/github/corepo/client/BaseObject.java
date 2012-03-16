package com.github.corepo.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class BaseObject {
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}

	public boolean equals(Object object) {
		return EqualsBuilder.reflectionEquals(this, object);
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
