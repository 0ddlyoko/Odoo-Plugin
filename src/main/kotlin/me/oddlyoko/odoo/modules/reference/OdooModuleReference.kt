package me.oddlyoko.odoo.modules.reference

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import me.oddlyoko.odoo.modules.OdooModuleUtil

class OdooModuleReference(psiElement: PsiElement): PsiReferenceBase.Poly<PsiElement>(psiElement) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val odooModule = OdooModuleUtil.getModule(value, element.project) ?: return ResolveResult.EMPTY_ARRAY
        return PsiElementResolveResult.createResults(odooModule.directory)
    }

    override fun getVariants(): Array<Any> {
        val currentModule = OdooModuleUtil.getModule(element)
        return OdooModuleUtil.getAllModules(element.project).filter { it != currentModule }.map { it.directory }.toTypedArray()
    }
}
