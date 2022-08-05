package me.oddlyoko.odoo

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker

object OdooUtil {

    fun <E, P> getData(psiElement: PsiElement, param: P, vararg depends: Any, supplier: () -> E?): E? =
        getData(psiElement, CachedValuesManager.getManager(psiElement.project).getKeyForClass(supplier.javaClass), param, depends, supplier=supplier)

    fun <E, P> getData(psiElement: PsiElement, key: Key<CachedValue<Map<P, E?>>>, param: P, vararg depends: Any, supplier: () -> E?): E? {
        val map: MutableMap<P, E?> = CachedValuesManager.getCachedValue(psiElement, key, CachedValueProvider {
            val objs = depends.takeIf { it.isNotEmpty() } ?: arrayOf(PsiModificationTracker.MODIFICATION_COUNT)
            return@CachedValueProvider CachedValueProvider.Result.create(LinkedHashMap(), objs)
        }) as MutableMap<P, E?>

        return map.getOrElse(param) {
            val value = supplier()
            map[param] = value
            return value
        }
    }
}
