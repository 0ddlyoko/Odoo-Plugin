package me.oddlyoko.odoo.highlighting;

import com.intellij.psi.PsiFile;
import com.jetbrains.python.inspections.PyInspectionExtension;
import com.jetbrains.python.psi.PyExpressionStatement;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import org.jetbrains.annotations.NotNull;

public class OdooPyInspectionExtension extends PyInspectionExtension {

    @Override
    public boolean ignoreNoEffectStatement(@NotNull PyExpressionStatement expressionStatement) {
        PsiFile file = expressionStatement.getContainingFile();
        if (file == null)
            return true;
        if (OdooModuleUtil.MANIFEST_FILES.contains(file.getName()))
            return true;
        return super.ignoreNoEffectStatement(expressionStatement);
    }
}
