package me.oddlyoko.odoo.modules.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import me.oddlyoko.odoo.modules.models.OdooModule;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OdooModuleReference extends PsiReferenceBase.Poly<PsiElement> {
    public OdooModuleReference(PsiElement psiElement) {
        super(psiElement);
    }

    public OdooModuleReference(PsiElement element, boolean soft) {
        super(element, soft);
    }

    public OdooModuleReference(PsiElement element, TextRange rangeInElement, boolean soft) {
        super(element, rangeInElement, soft);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        OdooModule module = OdooModuleUtil.getModule(getValue(), getElement().getProject());
        if (module == null)
            return ResolveResult.EMPTY_ARRAY;
        return PsiElementResolveResult.createResults(module.getDirectory());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        PsiElement element = getElement();
        List<OdooModule> modules = OdooModuleUtil.getModules(element.getProject());
        OdooModule currentModule = OdooModuleUtil.getModule(element);
        var a = modules.remove(currentModule);
        return modules.stream().map(OdooModule::getDirectory).toArray();
    }
}
