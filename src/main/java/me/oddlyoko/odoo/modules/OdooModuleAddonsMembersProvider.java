package me.oddlyoko.odoo.modules;

import com.jetbrains.python.codeInsight.PyCustomMember;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.types.PyModuleMembersProvider;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class OdooModuleAddonsMembersProvider extends PyModuleMembersProvider {
    @Override
    protected @NotNull Collection<PyCustomMember> getMembersByQName(@NotNull PyFile file, @NotNull String qName, @NotNull TypeEvalContext context) {
        if (!"odoo.addons".equals(qName))
            return List.of();
        return OdooModuleUtil.getModules(file.getProject())
                .stream()
                .map(odooModule -> new PyCustomMember(odooModule.getName(), odooModule.getDirectory()))
                .collect(Collectors.toUnmodifiableSet());
    }
}
