package me.oddlyoko.odoo.modules.reference;

import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyKeyValueExpression;
import com.jetbrains.python.psi.PyListLiteralExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class OdooManifestReferenceListCondition extends PatternCondition<PyStringLiteralExpression> {
    private final Set<String> acceptedKeys;

    public OdooManifestReferenceListCondition(Set<String> acceptedKeys) {
        super("__manifest__");
        this.acceptedKeys = acceptedKeys;
    }

    @Override
    public boolean accepts(@NotNull PyStringLiteralExpression pyString, ProcessingContext context) {
        PsiFile file = pyString.getContainingFile();
        // Only works in manifest file
        if (file == null || !OdooModuleUtil.MANIFEST_FILES.contains(file.getName()))
            return false;

        PsiElement parent = pyString.getParent();
        if (parent instanceof PyListLiteralExpression) {
            parent = parent.getParent();
            if (parent instanceof PyKeyValueExpression) {
                PyExpression key = ((PyKeyValueExpression) parent).getKey();
                if (key instanceof PyStringLiteralExpression)
                    return acceptedKeys.contains(((PyStringLiteralExpression) key).getStringValue());
            }
        }
        return false;
    }
}
