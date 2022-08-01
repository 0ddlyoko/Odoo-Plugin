package me.oddlyoko.odoo.models.models;

import com.intellij.lang.ASTNode;
import com.jetbrains.python.psi.impl.PyClassImpl;
import me.oddlyoko.odoo.models.OdooModelUtil;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import me.oddlyoko.odoo.modules.models.OdooModule;
import org.jetbrains.annotations.NotNull;

public class OdooModel extends PyClassImpl {

    public OdooModel(@NotNull ASTNode astNode) {
        super(astNode);
    }

    public OdooModule getOdooModule() {
        return OdooModuleUtil.getModuleFromFile(getContainingFile());
    }

    public String getOdooModel() {
        ModelDescriptor descriptor = getModelDescriptor();
        return descriptor != null ? descriptor.odooModel() : null;
    }

    public ModelDescriptor getModelDescriptor() {
        return OdooModelUtil.getDescriptor(this);
    }

    public boolean isInvalidOdooClass() {
        return getOdooModel() == null;
    }

    @Override
    public String toString() {
        return "OdooPyClass: " + getOdooModel();
    }
}
