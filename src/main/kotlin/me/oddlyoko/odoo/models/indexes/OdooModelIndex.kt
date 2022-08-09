package me.oddlyoko.odoo.models.indexes

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElementVisitor
import me.oddlyoko.odoo.models.OdooModelFilter
import me.oddlyoko.odoo.models.OdooModelUtil
import me.oddlyoko.odoo.models.models.ModelDescriptor
import me.oddlyoko.odoo.modules.OdooModuleUtil

class OdooModelIndex: ScalarIndexExtension<String>() {

    override fun getName(): ID<String, Void> = NAME

    override fun getIndexer(): DataIndexer<String, Void, FileContent> = DataIndexer {
        val result = mutableMapOf<String, Void?>()
        val psiFile = it.psiFile.originalFile
        val vFile = psiFile.virtualFile ?: return@DataIndexer result
        if (!OdooModuleUtil.isInOdooModule(vFile))
            return@DataIndexer result
        psiFile.acceptChildren(object: PyElementVisitor() {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                val elem = node.originalElement as PyClass
                if (OdooModelUtil.isInvalidOdooPyClass(elem))
                    return
                ModelDescriptor.fromPyClass(elem)?.let { result.put(it.odooModel, null) }
            }
        })
        return@DataIndexer result
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getVersion(): Int = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter = OdooModelFilter

    override fun dependsOnFileContent(): Boolean = true

    companion object {
        val NAME = ID.create<String, Void>("odoo.model")

        fun getClassesByModelName(odooModel: String, project: Project): List<PyClass> =
            getClassesByModelName(odooModel, project, GlobalSearchScope.projectScope(project))

        fun getClassesByModelName(odooModel: String, project: Project, scope: GlobalSearchScope): List<PyClass> {
            val vFiles = mutableListOf<PyClass>()
            val psiManager = PsiManager.getInstance(project)
            FileBasedIndex.getInstance().getContainingFiles(NAME, odooModel, scope).forEach {
                psiManager.findFile(it)?.acceptChildren(object: PyElementVisitor() {
                    override fun visitPyClass(node: PyClass) {
                        super.visitPyClass(node)
                        ModelDescriptor.fromPyClass(node)?.let {
                            if (odooModel == it.odooModel)
                                vFiles.add(node)
                        }
                    }
                })
            }
            return vFiles
        }

        fun getAllModels(project: Project): List<String> = FileBasedIndex.getInstance().getAllKeys(NAME, project).toList()

        fun getAllModels(project: Project, scope: GlobalSearchScope): List<String> {
            val allModels = getAllModels(project)
            val index = FileBasedIndex.getInstance()
            val result = mutableListOf<String>()
            allModels.forEach { index.processValues(NAME, it, null, { _, _ -> result.add(it) }, scope) }
            return result
        }

        fun getAllStringModels(project: Project): List<String> = FileBasedIndex.getInstance().getAllKeys(NAME, project).toList()
    }
}
