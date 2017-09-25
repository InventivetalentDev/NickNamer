package org.inventivetalent.nicknamer.database;

import org.inventivetalent.data.ebean.KeyValueBean;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "nicknamer_data_skin")
@SuppressWarnings({"unused", "WeakerAccess"})
public class SkinEntry extends KeyValueBean {

	public SkinEntry() {
	}

	public SkinEntry(String key, String value) {
		super(key, value);
	}
}
