package me.oddlyoko.odoo.fields;

import com.jetbrains.python.psi.PyTargetExpression;
import me.oddlyoko.odoo.fields.models.OdooField;
import org.jetbrains.annotations.NotNull;

public final class OdooFieldUtil {

    private OdooFieldUtil() {}

    public static boolean isValidField(@NotNull PyTargetExpression expr) {
        OdooField field = getField(expr);
        if (field == null)
            return false;
        return field.isValidOdooField();
    }

    public static OdooField getField(@NotNull PyTargetExpression expr) {
        return OdooField.fromPyExpression((PyTargetExpression) expr.getOriginalElement());
    }
}
