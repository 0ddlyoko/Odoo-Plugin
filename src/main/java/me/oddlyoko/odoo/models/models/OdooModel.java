package me.oddlyoko.odoo.models.models;

import com.intellij.lang.ASTNode;
import com.jetbrains.python.psi.impl.PyClassImpl;
import me.oddlyoko.odoo.models.OdooModelUtil;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import me.oddlyoko.odoo.modules.models.OdooModule;
import org.jetbrains.annotations.NotNull;

public class OdooModel extends PyClassImpl {

    private final String odooModel;
    private final boolean invalid;

    public OdooModel(@NotNull ASTNode astNode) {
        super(astNode);
        ModelDescriptor descriptor = getModelDescriptor();
        this.odooModel = descriptor != null ? descriptor.getOdooModel() : null;
        this.invalid = odooModel == null;
    }

    public OdooModule getOdooModule() {
        return OdooModuleUtil.getModuleFromFile(getContainingFile());
    }

    public String getOdooModel() {
        return odooModel;
    }

    public ModelDescriptor getModelDescriptor() {
        return OdooModelUtil.getDescriptor(this);
    }

    public boolean isInvalidOdooClass() {
        return invalid;
    }

    @Override
    public String toString() {
        return "OdooPyClass: " + getOdooModel();
    }
}
