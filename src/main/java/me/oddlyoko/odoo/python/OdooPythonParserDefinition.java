package me.oddlyoko.odoo.python;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.PythonParserDefinition;
import com.jetbrains.python.psi.impl.PyClassImpl;
import me.oddlyoko.odoo.models.models.OdooModel;
import org.jetbrains.annotations.NotNull;

public class OdooPythonParserDefinition extends PythonParserDefinition {

    @NotNull
    @Override
    public PsiElement createElement(@NotNull ASTNode node) {
        PsiElement element = super.createElement(node);
        if (element instanceof PyClassImpl)
            element = new OdooModel(node);
        return element;
    }
}
