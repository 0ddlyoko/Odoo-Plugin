package me.oddlyoko.odoo.models.models;

import com.intellij.psi.PsiInvalidElementAccessException;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyDictLiteralExpression;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.PyUtil;
import me.oddlyoko.odoo.OdooUtil;
import me.oddlyoko.odoo.models.OdooModelUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A description of a model
 */
public record ModelDescriptor(PyClass pyClass, String odooModel, String description, List<String> inherit,
                              Map<String, String> inherits) {
    public static final String MODEL_DESCRIPTOR_KEY = "model.descriptor";

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
    public String toString() {
        return pyClass + "[" + odooModel + "]";
    }

    /**
     * Retrieves existing {@link ModelDescriptor} linked to given {@link OdooModel} or create it
     *
     * @param pyClass The {@link PyClass}
     * @return Existing {@link ModelDescriptor} linked to given {@link OdooModel} or new one
     */
    public static ModelDescriptor fromPyClass(PyClass pyClass) {
        try {
            return OdooUtil.getData(pyClass.getOriginalElement(), MODEL_DESCRIPTOR_KEY, () -> parsePyClass(pyClass), pyClass);
        } catch (PsiInvalidElementAccessException ex) {
            return null;
        }
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
        PyTargetExpression pyNameExpression = pyClass.findClassAttribute(OdooModelUtil.NAME_KEY, false, null);
        if (pyNameExpression != null) {
            PyExpression valueExpression = pyNameExpression.findAssignedValue();
            if (valueExpression instanceof PyStringLiteralExpression stringExpression)
                odooModel = stringExpression.getStringValue();
        }
        // _description
        PyTargetExpression pyDescriptionExpression = pyClass.findClassAttribute(OdooModelUtil.DESCRIPTION_KEY, false, null);
        if (pyDescriptionExpression != null) {
            PyExpression valueExpression = pyDescriptionExpression.findAssignedValue();
            if (valueExpression instanceof PyStringLiteralExpression stringExpression)
                description = stringExpression.getStringValue();
        }
        // _inherit
        PyTargetExpression pyInheritExpression = pyClass.findClassAttribute(OdooModelUtil.INHERIT_KEY, false, null);
        if (pyInheritExpression != null) {
            PyExpression valueExpression = pyInheritExpression.findAssignedValue();
            if (valueExpression instanceof PyStringLiteralExpression stringExpression) {
                String value = stringExpression.getStringValue();
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
        PyTargetExpression pyInheritsExpression = pyClass.findClassAttribute(OdooModelUtil.INHERITS_KEY, false, null);
        if (pyInheritsExpression != null) {
            PyExpression valueExpression = pyInheritsExpression.findAssignedValue();
            if (valueExpression instanceof PyDictLiteralExpression dictExpression) {
                Map<String, PyExpression> value = PyUtil.dictValue(dictExpression);
                for (Map.Entry<String, PyExpression> entry : value.entrySet())
                    if (entry.getValue() instanceof PyStringLiteralExpression stringExpression)
                        inherits.put(entry.getKey(), stringExpression.getStringValue());
            }
        }
        return new ModelDescriptor(pyClass, odooModel, description, inherit, inherits);
    }
}
