package me.oddlyoko.odoo.modules

import com.intellij.psi.PsiFile
import com.jetbrains.python.codeInsight.PyCustomMember
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.types.PyModuleMembersProvider
import com.jetbrains.python.psi.types.TypeEvalContext
import me.oddlyoko.odoo.modules.models.OdooModule

class OdooModuleAddonsMembersProvider: PyModuleMembersProvider() {

    override fun getMembersByQName(module: PyFile, qName: String, context: TypeEvalContext): List<PyCustomMember> {
        if ("odoo.addons" != qName && "addons" != qName)
            return listOf()
        val origin: PsiFile = context.origin ?: return listOf()
        val odooModule: OdooModule = OdooModuleUtil.getModuleFromFile(origin.originalFile) ?: return listOf()
        return odooModule.getOdooModuleDepends().map { PyCustomMember(it.name, it.directory) }
    }
}
