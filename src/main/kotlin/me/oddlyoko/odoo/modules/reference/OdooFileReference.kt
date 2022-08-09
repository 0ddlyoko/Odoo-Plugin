package me.oddlyoko.odoo.modules.reference

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet

class OdooFileReference(psiElement: PsiElement): FileReferenceSet(psiElement)
