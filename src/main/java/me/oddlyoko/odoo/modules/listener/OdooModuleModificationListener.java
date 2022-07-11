package me.oddlyoko.odoo.modules.listener;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import me.oddlyoko.odoo.modules.tracker.OdooManifestModificationTracker;
import me.oddlyoko.odoo.modules.tracker.OdooModuleModificationTracker;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listen for file modification and update trackers
 */
public class OdooModuleModificationListener implements BulkFileListener {

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent e : events) {
            VirtualFile vFile = e.getFile();
            if (vFile == null)
                continue;
            VirtualFile odooDirectory = OdooModuleUtil.getOdooModuleDirectory(vFile);
            if (odooDirectory == null || !odooDirectory.exists())
                return;
            String name = odooDirectory.getName();
            OdooModuleModificationTracker.get(name).incModificationCount();
            if (OdooModuleUtil.isValidManifest(vFile))
                OdooManifestModificationTracker.get(name).incModificationCount();
        }
    }
}
