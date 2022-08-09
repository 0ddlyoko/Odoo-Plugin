package me.oddlyoko.odoo.modules.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.util.ProcessingContext
import com.jetbrains.python.psi.PyStringLiteralExpression

class OdooManifestReferenceContributor: PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(DEPENDS_PATTERN, object: PsiReferenceProvider() {
            override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> =
                arrayOf(OdooModuleReference(element))
        })
        registrar.registerReferenceProvider(DATA_PATTERN, object: PsiReferenceProvider() {
            override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out FileReference> =
                OdooFileReference(element).allReferences
        })
    }

    companion object {
        private val DATA_CONDITION = OdooManifestReferenceListCondition(listOf(
            "data",
            "demo",
            "images",
            "qweb",
            "test",
        ))
        private val DATA_PATTERN = PlatformPatterns.psiElement(PyStringLiteralExpression::class.java).with(DATA_CONDITION)
        private val DEPENDS_CONDITION = OdooManifestReferenceListCondition(listOf(
            "depends",
        ))
        private val DEPENDS_PATTERN = PlatformPatterns.psiElement(PyStringLiteralExpression::class.java).with(DEPENDS_CONDITION)
    }
}
