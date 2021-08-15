package org.inventivetalent.nicknamer.api;

import net.kyori.adventure.text.Component;
import org.inventivetalent.reflection.resolver.ClassResolver;

import java.util.List;
import java.util.Locale;

public class AdventureHelper {

    public static String asJsonString(Component component) {
        try {
            return (String) PaperAdventure.getMethod("asJsonString", Component.class, Locale.class)
                    .invoke(null, component, Locale.getDefault());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String asJsonString(Object iChatBaseComponent) {
        try {
            return (String) PaperAdventure.getMethod("asJsonString", ClassBuilder.IChatBaseComponent, Locale.class)
                    .invoke(null, iChatBaseComponent, Locale.getDefault());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Component> asAdventureFromJson(List<String> json) {
        try {
            return (List<Component>) PaperAdventure.getMethod("asAdventureFromJson", List.class)
                    .invoke(null, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static final ClassResolver CLASS_RESOLVER = new ClassResolver();

    static final Class<?> PaperAdventure = CLASS_RESOLVER.resolveSilent("io.papermc.paper.adventure.PaperAdventure");

}
