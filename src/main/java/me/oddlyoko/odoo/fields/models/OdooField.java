package me.oddlyoko.odoo.fields.models;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyTargetExpression;
import me.oddlyoko.odoo.models.OdooModelUtil;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import me.oddlyoko.odoo.modules.models.OdooModule;
import me.oddlyoko.odoo.models.models.OdooModel;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represent an odoo field
 */
public final class OdooField {
    public static final Key<CachedValue<OdooField>> FIELD_KEY = new Key<>("field");

    private final PyTargetExpression expr;
    private final String fieldName;
    private final boolean valid;

    private OdooField(PyTargetExpression expr, String fieldName) {
        this.expr = expr;
        this.fieldName = fieldName;
        this.valid = true;
    }

    public OdooModule getOdooModule() {
        PsiFile file = getFile();
        if (file == null)
            return null;
        return OdooModuleUtil.getModuleFromFile(file);
    }

    public PsiFile getFile() {
        return expr.getContainingFile();
    }

    public PyClass getPyClass() {
        return expr.getContainingClass();
    }

    public PyTargetExpression getExpr() {
        return expr;
    }

    public String getFieldName() {
        return fieldName;
    }

    public OdooModel getOdooModel() {
        PyClass pyClass = getPyClass();
        if (OdooModelUtil.isInvalidOdooPyClass(pyClass))
            return null;
        return (OdooModel) pyClass;
    }

    public boolean isValidOdooField() {
        return valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooField odooField = (OdooField) o;
        return Objects.equals(expr, odooField.expr) &&
                Objects.equals(fieldName, odooField.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr, fieldName);
    }

    @Override
    public String toString() {
        PyClass pyClass = getPyClass();
        return pyClass == null ? "???" : pyClass.getName() + "." + fieldName;
    }

    public static OdooField fromPyExpression(@NotNull PyTargetExpression expr) {
        return CachedValuesManager.getCachedValue(expr, FIELD_KEY,
                () -> CachedValueProvider.Result.create(parsePyExpression(expr), expr));
    }

    /**
     * Create a new {@link OdooField} based on given {@link PyTargetExpression}
     *
     * @param expr The expression that will be used to create the {@link OdooField}
     * @return Created {@link OdooField}, or null if given expression is not a valid {@link OdooField} or is not in a valid odoo module
     */
    private static OdooField parsePyExpression(@NotNull PyTargetExpression expr) {
        String fieldName = expr.getName();
        if (fieldName == null)
            return null;
        PyClass pyClass = expr.getContainingClass();
        if (pyClass == null)
            return null;
        if (OdooModelUtil.isInvalidOdooPyClass(pyClass))
            return null;
        OdooField field = new OdooField(expr, fieldName);
        return field.isValidOdooField() ? field : null;
    }
}
