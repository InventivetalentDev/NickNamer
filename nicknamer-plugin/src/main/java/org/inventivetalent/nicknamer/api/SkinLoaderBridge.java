package org.inventivetalent.nicknamer.api;

import com.google.gson.JsonObject;
import org.inventivetalent.data.DataProvider;

public class SkinLoaderBridge {

	public static DataProvider<JsonObject> getSkinProvider() {
		return SkinLoader.skinDataProvider;
	}

}
