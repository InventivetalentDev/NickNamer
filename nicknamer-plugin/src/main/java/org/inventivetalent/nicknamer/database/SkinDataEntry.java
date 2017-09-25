package org.inventivetalent.nicknamer.database;

import org.inventivetalent.data.ebean.KeyValueBean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "nicknamer_skins")
@SuppressWarnings({"unused", "WeakerAccess"})
public class SkinDataEntry extends KeyValueBean {

	@Column(updatable = false)
	long loadTime;

	public SkinDataEntry() {
	}

	public SkinDataEntry(String key, String value) {
		super(key, value);
	}

	public long getLoadTime() {
		return loadTime;
	}

	public void setLoadTime(long loadTime) {
		this.loadTime = loadTime;
	}
}
