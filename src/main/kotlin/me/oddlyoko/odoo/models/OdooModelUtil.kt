package me.oddlyoko.odoo.models

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFile
import me.oddlyoko.odoo.models.indexes.OdooModelIndex
import me.oddlyoko.odoo.models.models.ModelDescriptor
import me.oddlyoko.odoo.models.models.OdooModel
import me.oddlyoko.odoo.modules.models.OdooModule

object OdooModelUtil {
    val NAME_KEY = "_name"
    val DESCRIPTION_KEY = "_description"
    val INHERIT_KEY = "_inherit"
    val INHERITS_KEY = "_inherits"

    fun isInvalidOdooPyClass(pyClass: PyClass): Boolean = getOdooPyClass(pyClass) == null

    fun getOdooPyClass(pyClass: PyClass): OdooModel? {
        if (pyClass is OdooModel)
            return pyClass
        return OdooModel(pyClass.node).takeIf { !it.isInvalidOdooClass() }
    }

    fun getDescriptor(pyClass: PyClass): ModelDescriptor? = ModelDescriptor.fromPyClass(pyClass)

    fun getModelName(pyClass: PyClass): String? = getOdooPyClass(pyClass)?.getModelDescriptor()?.odooModel

    fun getClasses(vFile: VirtualFile, project: Project): List<PyClass> {
        val file = PsiManager.getInstance(project).findFile(vFile) ?: return listOf()
        return if (file is PyFile) file.topLevelClasses else listOf()
    }

    fun getOdooModels(pyClasses: List<PyClass>): List<OdooModel> = pyClasses.mapNotNull { getOdooPyClass(it) }

    fun getOdooModels(vFile: VirtualFile, project: Project): List<OdooModel> = getOdooModels(getClasses(vFile, project))

    fun getAllModels(project: Project): List<String> = OdooModelIndex.getAllModels(project)

    fun getAllModels(project: Project, scope: GlobalSearchScope): List<String> = OdooModelIndex.getAllModels(project, scope)

    fun getAllModels(odooModule: OdooModule, project: Project): List<String> =
        OdooModelIndex.getAllModels(project, odooModule.getOdooPythonModuleScope(true))

    fun getClassesByModelName(odooModel: String, project: Project): List<PyClass> =
        OdooModelIndex.getClassesByModelName(odooModel, project)

    fun getClassesByModelName(odooModel: String, project: Project, scope: GlobalSearchScope): List<PyClass> =
        OdooModelIndex.getClassesByModelName(odooModel, project, scope)

    fun getClassesByModelName(odooModel: String, project: Project, odooModule: OdooModule): List<PyClass> =
        OdooModelIndex.getClassesByModelName(odooModel, project, odooModule.getOdooPythonModuleScope(true))
}
