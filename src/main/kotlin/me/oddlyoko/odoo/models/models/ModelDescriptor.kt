package me.oddlyoko.odoo.models.models

import com.intellij.psi.PsiInvalidElementAccessException
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyDictLiteralExpression
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PyUtil
import me.oddlyoko.odoo.OdooUtil
import me.oddlyoko.odoo.models.OdooModelUtil

data class ModelDescriptor(val pyClass: PyClass, val odooModel: String, val description: String,
                           val inherit: List<String>, val inherits: Map<String, String>) {

    override fun toString(): String = "$pyClass[$odooModel]"

    companion object {
        val MODEL_DESCRIPTOR_KEY = "model.descriptor"

        fun fromPyClass(pyClass: PyClass): ModelDescriptor? {
            return try {
                OdooUtil.getData(pyClass.originalElement, MODEL_DESCRIPTOR_KEY, pyClass) {
                    parsePyClass(pyClass)
                }
            } catch (e: PsiInvalidElementAccessException) {
                null
            }
        }

        fun parsePyClass(pyClass: PyClass): ModelDescriptor? {
            var odooModel: String? = null
            var description = ""
            val inherit = mutableListOf<String>()
            val inherits = mutableMapOf<String, String>()
            // _name
            pyClass.findClassAttribute(OdooModelUtil.NAME_KEY, false, null)?.let {
                val valueExpr = it.findAssignedValue()
                if (valueExpr is PyStringLiteralExpression)
                    odooModel = valueExpr.stringValue
            }
            // _description
            pyClass.findClassAttribute(OdooModelUtil.DESCRIPTION_KEY, false, null)?.let {
                val valueExpr = it.findAssignedValue()
                if (valueExpr is PyStringLiteralExpression)
                    description = valueExpr.stringValue
            }
            // _inherit
            pyClass.findClassAttribute(OdooModelUtil.INHERIT_KEY, false, null)?.let {
                val valueExpr = it.findAssignedValue()
                if (valueExpr is PyStringLiteralExpression)
                    inherit.add(valueExpr.stringValue)
                else
                    inherit.addAll(PyUtil.strListValue(valueExpr) ?: listOf())
                if (odooModel == null && inherit.isNotEmpty())
                    odooModel = inherit[0]
            }
            if (odooModel == null)
                return null
            // _inherits
            pyClass.findClassAttribute(OdooModelUtil.INHERITS_KEY, false, null)?.let {
                val valueExpr = it.findAssignedValue()
                if (valueExpr is PyDictLiteralExpression) {
                    PyUtil.dictValue(valueExpr).forEach {
                        if (it is PyStringLiteralExpression)
                            inherits[it.key] = it.stringValue
                    }
                }
            }
            return ModelDescriptor(pyClass, odooModel!!, description, inherit, inherits)
        }
    }
}
