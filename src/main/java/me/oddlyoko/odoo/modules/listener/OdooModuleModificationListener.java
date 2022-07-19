package me.oddlyoko.odoo.modules.listener;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiTreeChangeEventImpl;
import com.intellij.psi.impl.PsiTreeChangePreprocessor;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import me.oddlyoko.odoo.modules.trackers.OdooManifestModificationTracker;
import me.oddlyoko.odoo.modules.trackers.OdooModuleModificationTracker;
import org.jetbrains.annotations.NotNull;

/**
 * Listen for file modification and update trackers
 */
public class OdooModuleModificationListener implements PsiTreeChangePreprocessor {

    @Override
    public void treeChanged(@NotNull PsiTreeChangeEventImpl e) {
        PsiFile file = e.getFile();
        if (file == null)
            return;
        VirtualFile vFile = file.getVirtualFile();
        if (vFile == null)
            return;
        VirtualFile odooDirectory = OdooModuleUtil.getOdooModuleDirectory(vFile);
        if (odooDirectory == null || !odooDirectory.exists())
            return;
        String name = odooDirectory.getName();
        OdooModuleModificationTracker.get(name).incModificationCount();
        if (OdooModuleUtil.isValidManifest(vFile))
            OdooManifestModificationTracker.get(name).incModificationCount();
    }
}
