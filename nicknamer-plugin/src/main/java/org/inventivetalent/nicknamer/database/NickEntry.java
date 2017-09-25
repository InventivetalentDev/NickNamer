package org.inventivetalent.nicknamer.database;

import org.inventivetalent.data.ebean.KeyValueBean;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "nicknamer_data_nick")
@SuppressWarnings({"unused", "WeakerAccess"})
public class NickEntry extends KeyValueBean {

	public NickEntry() {
	}

	public NickEntry(String key, String value) {
		super(key, value);
	}
}
