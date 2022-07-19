package me.oddlyoko.odoo.modules.trackers;

import com.intellij.openapi.util.SimpleModificationTracker;

import java.util.HashMap;
import java.util.Map;

public class OdooModuleModificationTracker extends SimpleModificationTracker {
    private static final Map<String, OdooModuleModificationTracker> trackers = new HashMap<>();

    public static OdooModuleModificationTracker get(String module) {
        OdooModuleModificationTracker tracker = trackers.get(module);
        if (tracker == null) {
            tracker = new OdooModuleModificationTracker();
            trackers.put(module, tracker);
        }
        return tracker;
    }
}
