package me.oddlyoko.odoo.highlighting

import com.jetbrains.python.inspections.PyInspectionExtension
import com.jetbrains.python.psi.PyExpressionStatement
import me.oddlyoko.odoo.modules.OdooModuleUtil

class OdooPyInspectionExtension: PyInspectionExtension() {

    override fun ignoreNoEffectStatement(expressionStatement: PyExpressionStatement): Boolean {
        val file = expressionStatement.containingFile ?: return true
        if (OdooModuleUtil.MANIFEST_FILES.contains(file.name))
            return true
        return super.ignoreNoEffectStatement(expressionStatement)
    }
}
