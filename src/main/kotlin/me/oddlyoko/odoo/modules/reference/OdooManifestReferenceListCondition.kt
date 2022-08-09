package me.oddlyoko.odoo.modules.reference

import com.intellij.patterns.PatternCondition
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyKeyValueExpression
import com.jetbrains.python.psi.PyListLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralExpression
import me.oddlyoko.odoo.modules.OdooModuleUtil

class OdooManifestReferenceListCondition(private val acceptedKeys: List<String>): PatternCondition<PyStringLiteralExpression>("__manifest__") {

    override fun accepts(pyString: PyStringLiteralExpression, context: ProcessingContext?): Boolean {
        val file = pyString.containingFile ?: return false
        if (!OdooModuleUtil.MANIFEST_FILES.contains(file.name))
            return false

        var parent = pyString.parent
        if (parent is PyListLiteralExpression) {
            parent = parent.parent
            if (parent is PyKeyValueExpression) {
                val key = parent.key
                if (key is PyStringLiteralExpression)
                    return acceptedKeys.contains(key.stringValue)
            }
        }
        return false
    }
}
