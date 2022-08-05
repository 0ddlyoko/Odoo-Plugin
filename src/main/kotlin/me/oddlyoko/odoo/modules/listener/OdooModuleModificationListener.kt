package me.oddlyoko.odoo.modules.listener

import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import me.oddlyoko.odoo.modules.OdooModuleUtil
import me.oddlyoko.odoo.modules.trackers.OdooManifestModificationTracker
import me.oddlyoko.odoo.modules.trackers.OdooModuleModificationTracker

class OdooModuleModificationListener: PsiTreeChangePreprocessor {

    override fun treeChanged(e: PsiTreeChangeEventImpl) {
        val file = e.file ?: return
        val vFile = file.virtualFile ?: return
        val odooVFile = OdooModuleUtil.getOdooModuleDirectory(vFile) ?: return
        OdooModuleModificationTracker[odooVFile.name].incModificationCount()
        if (OdooModuleUtil.isValidManifest(vFile))
            OdooManifestModificationTracker[odooVFile.name].incModificationCount()
    }
}
