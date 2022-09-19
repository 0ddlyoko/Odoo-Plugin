package me.oddlyoko.odoo.models.models

import com.intellij.lang.ASTNode
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.impl.PyClassImpl
import me.oddlyoko.odoo.models.OdooModelUtil
import me.oddlyoko.odoo.modules.OdooModuleUtil
import me.oddlyoko.odoo.modules.models.OdooModule

class OdooModel(astNode: ASTNode): PyClassImpl(astNode) {

    val odooModule: OdooModule?
        get() = OdooModuleUtil.getModuleFromFile(containingFile)
    val modelDescriptor: ModelDescriptor?
        get() = OdooModelUtil.getDescriptor(this)
    val odooModel: String?
        get() = modelDescriptor?.odooModel


    fun isInvalidOdooClass(): Boolean = odooModel == null

    fun getParentClasses(): List<PyClass> {
        val odooModule = odooModule ?: return listOf()
        val odooModel = odooModel ?: return listOf()
        return OdooModelUtil.getClassesByModelName(odooModel, odooModule.project, odooModule).filter { it != this }
    }

    fun getChildrenClasses(): List<PyClass> {
        val odooModule = odooModule ?: return listOf()
        val odooModel = odooModel ?: return listOf()
        return OdooModelUtil.getDependingClassesByModelName(odooModel, odooModule.project, odooModule)
    }

    override fun toString(): String = "OdooPyClass: $odooModel"
}
