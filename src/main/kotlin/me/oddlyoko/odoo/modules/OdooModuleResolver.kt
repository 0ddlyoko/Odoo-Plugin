package me.oddlyoko.odoo.modules

import com.intellij.psi.PsiElement
import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.impl.PyImportResolver
import com.jetbrains.python.psi.resolve.PyQualifiedNameResolveContext
import com.jetbrains.python.psi.resolve.ResolveImportUtil
import me.oddlyoko.odoo.modules.models.OdooModule

class OdooModuleResolver: PyImportResolver {
    override fun resolveImportReference(name: QualifiedName, context: PyQualifiedNameResolveContext, withRoots: Boolean): PsiElement? {
        val components = name.components
        if (components.size < 3)
            return null
        if ("odoo" == components[0] && "addons" == components[1]) {
            val targetName = components[2]
            val target: OdooModule = OdooModuleUtil.getModule(targetName, context.project) ?: return null

            // 'import odoo.addons.xxx' or 'from odoo.addons.xxx import yyy'
            if (components.size == 3)
                return target.directory

            // 'import odoo.addons.xxx.yyy.zzz' or 'from odoo.addons.xxx.yyy import zzz'
            return resolveOdooAddonImportReference(name.removeHead(3), target, context)
        }
        return null
    }

    private fun resolveOdooAddonImportReference(name: QualifiedName, module: OdooModule, context: PyQualifiedNameResolveContext): PsiElement? {
        var parent: PsiElement? = module.directory
        for (component in name.components)
            parent = ResolveImportUtil.resolveChildren(parent, component, context.footholdFile, !context.withMembers, !context.withPlainDirectories, context.withoutStubs, context.withoutForeign)
                .firstOrNull()?.element
        return parent
    }
}
