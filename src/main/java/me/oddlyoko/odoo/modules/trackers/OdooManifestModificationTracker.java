package me.oddlyoko.odoo.modules.trackers;

import com.intellij.openapi.util.SimpleModificationTracker;

import java.util.HashMap;
import java.util.Map;

public class OdooManifestModificationTracker extends SimpleModificationTracker {
    private static final Map<String, OdooManifestModificationTracker> trackers = new HashMap<>();

    public static OdooManifestModificationTracker get(String module) {
        OdooManifestModificationTracker tracker = trackers.get(module);
        if (tracker == null) {
            tracker = new OdooManifestModificationTracker();
            trackers.put(module, tracker);
        }
        return tracker;
    }
}
