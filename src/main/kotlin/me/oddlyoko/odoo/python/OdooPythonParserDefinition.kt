package me.oddlyoko.odoo.python

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.jetbrains.python.PythonParserDefinition
import com.jetbrains.python.psi.impl.PyClassImpl
import me.oddlyoko.odoo.models.models.OdooModel

class OdooPythonParserDefinition: PythonParserDefinition() {

    override fun createElement(node: ASTNode): PsiElement {
        var elem = super.createElement(node)
        if (elem is PyClassImpl)
            elem = OdooModel(node)
        return elem
    }
}
