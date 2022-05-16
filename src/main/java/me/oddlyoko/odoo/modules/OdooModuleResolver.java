package me.oddlyoko.odoo.modules;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.impl.PyImportResolver;
import com.jetbrains.python.psi.resolve.PyQualifiedNameResolveContext;
import com.jetbrains.python.psi.resolve.PyResolveImportUtil;
import me.oddlyoko.odoo.modules.models.OdooModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdooModuleResolver implements PyImportResolver {
    @Nullable
    @Override
    public PsiElement resolveImportReference(@NotNull QualifiedName name, @NotNull PyQualifiedNameResolveContext context, boolean withRoots) {
        List<String> components = name.getComponents();
        if (components.size() < 3)
            return null;
        if ("odoo".equals(components.get(0)) && "addons".equals(components.get(1))) {
            String moduleName = components.get(2);
            OdooModule module = OdooModuleUtil.getModule(moduleName, context.getProject());
            if (module == null)
                return null;
            return PyResolveImportUtil.resolveModuleAt(name.subQualifiedName(3, name.getComponentCount()), module.getDirectory(), context)
                    .stream().findFirst().orElse(null);
        }
        return null;
    }
}
