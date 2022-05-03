package me.oddlyoko.odoo.modules.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import org.jetbrains.annotations.NotNull;

public class OdooFileReference extends FileReferenceSet {
    public OdooFileReference(@NotNull PsiElement element) {
        super(element);
    }
}
