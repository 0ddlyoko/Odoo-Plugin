package me.oddlyoko.odoo.models

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.util.Processor
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.search.PyClassInheritorsSearch
import me.oddlyoko.odoo.models.models.OdooModel

class OdooPyClassInheritorsSearch: QueryExecutorBase<PyClass, PyClassInheritorsSearch.SearchParameters>() {
    override fun processQuery(
        queryParameters: PyClassInheritorsSearch.SearchParameters,
        consumer: Processor<in PyClass>
    ) {
        val clazz: PyClass = queryParameters.superClass
        val odooModel: OdooModel = OdooModelUtil.getOdooPyClass(clazz) ?: return
        val childrenClasses: List<PyClass> = odooModel.getChildrenClasses()
        for (childClass in childrenClasses) {
            if (!consumer.process(childClass))
                return
        }
    }
}
