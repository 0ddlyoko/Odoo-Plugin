package me.oddlyoko.odoo.models.reference

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import me.oddlyoko.odoo.models.OdooModelUtil
import me.oddlyoko.odoo.modules.OdooModuleUtil

class OdooModelReference(psiElement: PsiElement): PsiReferenceBase.Poly<PsiElement>(psiElement) {

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val odooModule = OdooModuleUtil.getModule(element) ?: return ResolveResult.EMPTY_ARRAY
        val pyClasses = OdooModelUtil.getClassesByModelName(value, element.project, odooModule)
        return PsiElementResolveResult.createResults(pyClasses)
    }

    override fun getVariants(): Array<Any> {
        val odooModule = OdooModuleUtil.getModule(element) ?: return arrayOf()
        return OdooModelUtil.getAllModels(odooModule, element.project).map(LookupElementBuilder::create).toTypedArray()
    }
}
