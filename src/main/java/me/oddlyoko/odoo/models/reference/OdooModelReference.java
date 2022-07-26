package me.oddlyoko.odoo.models.reference;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.jetbrains.python.psi.PyClass;
import me.oddlyoko.odoo.models.OdooModelUtil;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import me.oddlyoko.odoo.modules.models.OdooModule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OdooModelReference extends PsiReferenceBase.Poly<PsiElement> {
    public OdooModelReference(PsiElement psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        PsiElement element = getElement();
        OdooModule odooModule = OdooModuleUtil.getModule(element);
        if (odooModule == null)
            return ResolveResult.EMPTY_ARRAY;
        List<PyClass> pyClasses = OdooModelUtil.getModelsByName(getValue(), element.getProject(), odooModule);
        return PsiElementResolveResult.createResults(pyClasses);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> elements = new ArrayList<>();
        PsiElement element = getElement();
        OdooModule odooModule = OdooModuleUtil.getModule(element);
        if (odooModule == null)
            return new Object[0];

        OdooModelUtil.getAllModels(element.getProject(), odooModule).forEach(s ->
                elements.add(LookupElementBuilder.create(s)));
        Collections.reverse(elements);
        return elements.toArray();
    }
}
