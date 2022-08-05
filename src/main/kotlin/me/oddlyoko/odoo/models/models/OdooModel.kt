package me.oddlyoko.odoo.models.models

import com.intellij.lang.ASTNode
import com.jetbrains.python.psi.impl.PyClassImpl
import me.oddlyoko.odoo.models.OdooModelUtil
import me.oddlyoko.odoo.modules.OdooModuleUtil
import me.oddlyoko.odoo.modules.models.OdooModule

class OdooModel(astNode: ASTNode): PyClassImpl(astNode) {

    fun getOdooModule(): OdooModule? = OdooModuleUtil.getModuleFromFile(containingFile)

    fun getOdooModel(): String? = getModelDescriptor()?.odooModel

    fun getModelDescriptor(): ModelDescriptor? = OdooModelUtil.getDescriptor(this)

    fun isInvalidOdooClass(): Boolean = getOdooModel() == null

    override fun toString(): String = "OdooPyClass: ${getOdooModel()}"
}
