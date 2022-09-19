package me.oddlyoko.odoo.models

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.search.PySuperMethodsSearch
import me.oddlyoko.odoo.models.models.OdooModel

class OdooPySuperMethodsSearch: QueryExecutorBase<PsiElement, PySuperMethodsSearch.SearchParameters>() {
    override fun processQuery(
        queryParameters: PySuperMethodsSearch.SearchParameters,
        consumer: Processor<in PsiElement>
    ) {
        val func: PyFunction = queryParameters.derivedMethod
        val clazz: PyClass = func.containingClass ?: return
        val odooModel: OdooModel = OdooModelUtil.getOdooPyClass(clazz) ?: return
        val parentClasses: List<PyClass> = odooModel.getParentClasses()
        for (parentClass in parentClasses) {
            val parentFunc = parentClass.findMethodByName(func.name, false, queryParameters.context)
            if (parentFunc != null)
                if (!consumer.process(parentFunc))
                    return
        }
    }
}
