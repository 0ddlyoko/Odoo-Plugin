package me.oddlyoko.odoo.modules;

import com.intellij.psi.PsiFile;
import com.jetbrains.python.codeInsight.PyCustomMember;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.types.PyModuleMembersProvider;
import com.jetbrains.python.psi.types.TypeEvalContext;
import me.oddlyoko.odoo.modules.models.OdooModule;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Import auto-completion
 */
public class OdooModuleAddonsMembersProvider extends PyModuleMembersProvider {

    @NotNull
    @Override
    protected Collection<PyCustomMember> getMembersByQName(@NotNull PyFile file, @NotNull String qName, @NotNull TypeEvalContext context) {
        if (!"odoo.addons".equals(qName) && !"addons".equals(qName))
            return List.of();
        PsiFile origin = context.getOrigin();
        if (origin == null)
            return List.of();
        OdooModule module = OdooModuleUtil.getModuleFromFile(origin.getOriginalFile());
        if (module == null)
            return List.of();
        return OdooModuleUtil.getAvailableModules(module)
                .stream()
                .filter(Objects::nonNull)
                .map(m -> new PyCustomMember(m.getName(), m.getDirectory()))
                .collect(Collectors.toUnmodifiableSet());
    }
}
