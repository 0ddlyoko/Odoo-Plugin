package me.oddlyoko.odoo.models.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.psi.*

class OdooModelReferenceContributor: PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val provider = object: PsiReferenceProvider() {
            override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
                arrayOf(OdooModelReference(element))
        }
        registrar.registerReferenceProvider(NAME_PATTERN, provider)
        registrar.registerReferenceProvider(INHERIT_PATTERN, provider)
        registrar.registerReferenceProvider(INHERIT_DICT_PATTERN, provider)
        registrar.registerReferenceProvider(INHERITS_PATTERN, provider)
    }

    companion object {
        private val NAME_PATTERN = PlatformPatterns.psiElement(PyStringLiteralExpression::class.java).afterSiblingSkipping(
            PlatformPatterns.psiElement().withElementType(PyTokenTypes.EQ),
            PlatformPatterns.psiElement(PyTargetExpression::class.java).withName("_name")
        )
        private val INHERIT_PATTERN = PlatformPatterns.psiElement(PyStringLiteralExpression::class.java).afterSiblingSkipping(
            PlatformPatterns.psiElement().withElementType(PyTokenTypes.EQ),
            PlatformPatterns.psiElement(PyTargetExpression::class.java).withName("_inherit")
        )
        private val INHERIT_DICT_PATTERN = PlatformPatterns.psiElement(PyStringLiteralExpression::class.java).withParent(
            PlatformPatterns.psiElement(PyListLiteralExpression::class.java).afterSiblingSkipping(
                PlatformPatterns.psiElement().withElementType(PyTokenTypes.EQ),
                PlatformPatterns.psiElement(PyTargetExpression::class.java).withName("_inherit")
            )
        )
        private val INHERITS_PATTERN = PlatformPatterns.psiElement(PyStringLiteralExpression::class.java)
            .withAncestor(2, PlatformPatterns.or(
                PlatformPatterns.psiElement(PyDictLiteralExpression::class.java).afterSiblingSkipping(
                    PlatformPatterns.psiElement().withElementType(PyTokenTypes.EQ),
                    PlatformPatterns.psiElement(PyTargetExpression::class.java).withName("_inherits")
                ),
                PlatformPatterns.psiElement(PySetLiteralExpression::class.java).afterSiblingSkipping(
                    PlatformPatterns.psiElement().withElementType(PyTokenTypes.EQ),
                    PlatformPatterns.psiElement(PyTargetExpression::class.java).withName("_inherits")
                )
            ))
    }
}
