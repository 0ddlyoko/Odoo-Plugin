package me.oddlyoko.odoo.modules.indexes

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import me.oddlyoko.odoo.modules.OdooManifestFilter
import me.oddlyoko.odoo.modules.OdooModuleUtil
import me.oddlyoko.odoo.modules.models.ModuleDescriptor
import me.oddlyoko.odoo.modules.models.OdooModule

class OdooModuleDependencyIndex: ScalarIndexExtension<String>() {

    override fun getName(): ID<String, Void> = NAME

    override fun getIndexer(): DataIndexer<String, Void, FileContent> = DataIndexer {
        val manifest = it.file
        val file = PsiManager.getInstance(it.project).findFile(manifest) ?: return@DataIndexer mapOf()
        val descriptor = ModuleDescriptor.fromFile(file) ?: return@DataIndexer mapOf()
        return@DataIndexer descriptor.depends.associateWith { null }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getVersion(): Int = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter = OdooManifestFilter

    override fun dependsOnFileContent(): Boolean = true

    companion object {
        val NAME = ID.create<String, Void>("odoo.module.dependency")

        fun getDepends(vFile: VirtualFile, project: Project): List<String> =
            FileBasedIndex.getInstance().getFileData(NAME, vFile, project).keys.toList()

        fun getOdooDepending(moduleName: String, project: Project): List<OdooModule> {
            val scope = GlobalSearchScope.projectScope(project)
            return FileBasedIndex.getInstance().getContainingFiles(NAME, moduleName, scope).mapNotNull { OdooModuleUtil.getModule(it, project) }
        }
    }
}
