package me.oddlyoko.odoo.modules.trackers

import com.intellij.openapi.util.SimpleModificationTracker

object OdooModuleModificationTracker {
    private val trackers: MutableMap<String, SimpleModificationTracker> = HashMap()

    operator fun get(module: String) = trackers.getOrElse(module) {
        val tracker = SimpleModificationTracker()
        trackers[module] = tracker
        return tracker
    }
}
