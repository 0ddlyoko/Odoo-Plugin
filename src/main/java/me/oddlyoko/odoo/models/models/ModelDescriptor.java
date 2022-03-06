package me.oddlyoko.odoo.models.models;

import com.intellij.openapi.util.Key;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyDictLiteralExpression;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.PyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A description of a model
 */
public final class ModelDescriptor {
    public static final Key<CachedValue<ModelDescriptor>> MODEL_DESCRIPTOR_KEY = new Key<>("modelDescriptor");

    private final PyClass pyClass;
    private final String odooModel;
    private final String description;
    private final List<String> inherit;
    private final Map<String, String> inherits;

    public ModelDescriptor(@NotNull PyClass pyClass,
                           @NotNull String odooModel,
                           @Nullable String description,
                           @NotNull List<String> inherit,
                           @NotNull Map<String, String> inherits) {
        this.pyClass = pyClass;
        this.odooModel = odooModel;
        this.description = description;
        this.inherit = List.copyOf(inherit);
        this.inherits = Map.copyOf(inherits);
    }

    public PyClass getPyClass() {
        return pyClass;
    }

    public String getOdooModel() {
        return odooModel;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getInherit() {
        return inherit;
    }

    public Map<String, String> getInherits() {
        return inherits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ModelDescriptor that = (ModelDescriptor) o;
        return pyClass.equals(that.pyClass)
                && odooModel.equals(that.odooModel)
                && Objects.equals(description, that.description)
                && inherit.equals(that.inherit)
                && inherits.equals(that.inherits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pyClass, odooModel, description, inherit, inherits);
    }

    @Override
    public String toString() {
        return pyClass + "[" + odooModel + "]";
    }

    /**
     * Retrieves existing {@link ModelDescriptor} linked to given {@link PyClass} or create it
     *
     * @param pyClass The {@link PyClass}
     * @return Existing {@link OdooModel} linked to given {@link PyClass} or new one
     */
    public static ModelDescriptor fromPyClass(PyClass pyClass) {
        return CachedValuesManager.getCachedValue(pyClass, MODEL_DESCRIPTOR_KEY,
                () -> CachedValueProvider.Result.create(parsePyClass(pyClass), pyClass));
    }

    /**
     * Create a new {@link ModelDescriptor} based on given {@link PyClass}
     *
     * @param pyClass The class that will be used to create the {@link ModelDescriptor}
     * @return Created {@link ModelDescriptor}, or null if given class is not a valid {@link ModelDescriptor}
     */
    public static ModelDescriptor parsePyClass(PyClass pyClass) {
        String odooModel = null;
        String description = null;
        List<String> inherit = new ArrayList<>();
        Map<String, String> inherits = new HashMap<>();
        // _name
        PyTargetExpression pyNameExpression = pyClass.findClassAttribute(OdooModel.NAME_KEY, false, null);
        if (pyNameExpression != null) {
            PyExpression valueExpression = pyNameExpression.findAssignedValue();
            if (valueExpression instanceof PyStringLiteralExpression)
                odooModel = ((PyStringLiteralExpression) valueExpression).getStringValue();
        }
        // _description
        PyTargetExpression pyDescriptionExpression = pyClass.findClassAttribute(OdooModel.DESCRIPTION_KEY, false, null);
        if (pyDescriptionExpression != null) {
            PyExpression valueExpression = pyDescriptionExpression.findAssignedValue();
            if (valueExpression instanceof PyStringLiteralExpression)
                description = ((PyStringLiteralExpression) valueExpression).getStringValue();
        }
        // _inherit
        PyTargetExpression pyInheritExpression = pyClass.findClassAttribute(OdooModel.INHERIT_KEY, false, null);
        if (pyInheritExpression != null) {
            PyExpression valueExpression = pyInheritExpression.findAssignedValue();
            if (valueExpression instanceof PyStringLiteralExpression) {
                String value = ((PyStringLiteralExpression) valueExpression).getStringValue();
                inherit.add(value);
            } else {
                List<String> lst = PyUtil.strListValue(valueExpression);
                if (lst != null)
                    inherit.addAll(lst);
            }
            if (odooModel == null && !inherit.isEmpty())
                odooModel = inherit.get(0);
        }
        if (odooModel == null)
            return null;
        // _inherits
        PyTargetExpression pyInheritsExpression = pyClass.findClassAttribute(OdooModel.INHERITS_KEY, false, null);
        if (pyInheritsExpression != null) {
            PyExpression valueExpression = pyInheritsExpression.findAssignedValue();
            if (valueExpression instanceof PyDictLiteralExpression) {
                Map<String, PyExpression> value = PyUtil.dictValue((PyDictLiteralExpression) valueExpression);
                for (Map.Entry<String, PyExpression> entry : value.entrySet())
                    if (entry.getValue() instanceof PyStringLiteralExpression)
                        inherits.put(entry.getKey(), ((PyStringLiteralExpression) entry.getValue()).getStringValue());
            }
        }
        return new ModelDescriptor(pyClass, odooModel, description, inherit, inherits);
    }
}
