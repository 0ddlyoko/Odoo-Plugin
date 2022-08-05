package me.oddlyoko.odoo.modules.indexes

import me.oddlyoko.odoo.modules.OdooManifestFilter
import me.oddlyoko.odoo.modules.OdooModuleUtil
import me.oddlyoko.odoo.modules.models.OdooModule
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor

class OdooModuleIndex: ScalarIndexExtension<String>() {

    override fun getName(): ID<String, Void> = NAME

    override fun getIndexer(): DataIndexer<String, Void, FileContent> = DataIndexer {
        val manifest = it.file
        val parent = manifest.parent
        return@DataIndexer if (parent != null) mapOf(Pair(parent.name, null)) else mapOf()
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getVersion(): Int = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter = OdooManifestFilter

    override fun dependsOnFileContent(): Boolean = true

    companion object {
        val NAME = ID.create<String, Void>("odoo.module")

        fun getModule(moduleName: String, project: Project): OdooModule? {
            val scope = GlobalSearchScope.projectScope(project)
            return FileBasedIndex.getInstance().getContainingFiles(NAME, moduleName, scope).firstNotNullOfOrNull { OdooModuleUtil.getModule(it, project) }
        }

        fun getAllOdooModules(project: Project): List<OdooModule> =
            getAllStringModules(project).mapNotNull { getModule(it, project) }

        fun getAllStringModules(project: Project): MutableCollection<String> = FileBasedIndex.getInstance().getAllKeys(NAME, project)
    }
}
