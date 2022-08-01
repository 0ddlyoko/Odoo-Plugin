package me.oddlyoko.odoo.modules.models;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyDictLiteralExpression;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PySequenceExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.PyUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A description of a module.<br />
 * Data is taken from manifest file
 */
public record ModuleDescriptor(PsiDirectory directory, PsiFile manifest, String id, String name, String version,
                               String description, List<String> author, String website, List<String> depends,
                               List<String> data, List<String> demo, String license) {


    @Override
    public String toString() {
        return id;
    }

    public static ModuleDescriptor parseFile(@NotNull PsiFile manifest) {
        PsiDirectory module = manifest.getParent();
        if (module == null)
            return null;
        PyDictLiteralExpression dict = PsiTreeUtil.findChildOfType(manifest, PyDictLiteralExpression.class);
        if (dict == null)
            return null;
        String id = module.getName();
        String name = "";
        String version = "";
        String description = "";
        List<String> authors = new ArrayList<>();
        String website = "";
        List<String> depends = new ArrayList<>();
        List<String> data = new ArrayList<>();
        List<String> demo = new ArrayList<>();
        String license = "";
        Map<String, PyExpression> map = PyUtil.dictValue(dict);
        for (Map.Entry<String, PyExpression> entry : map.entrySet()) {
            String key = entry.getKey();
            PyExpression value = entry.getValue();
            switch (key) {
                case "name" -> {
                    if (value instanceof PyStringLiteralExpression stringExpression)
                        name = stringExpression.getStringValue();
                }
                case "version" -> {
                    if (value instanceof PyStringLiteralExpression stringExpression)
                        version = stringExpression.getStringValue();
                }
                case "description" -> {
                    if (value instanceof PyStringLiteralExpression stringExpression)
                        description = stringExpression.getStringValue();
                }
                case "author" -> {
                    if (value instanceof PyStringLiteralExpression stringExpression)
                        authors = List.of(stringExpression.getStringValue());
                    else if (value instanceof PySequenceExpression sequenceExpression)
                        authors = sequenceToList(sequenceExpression);
                }
                case "website" -> {
                    if (value instanceof PyStringLiteralExpression stringExpression)
                        website = stringExpression.getStringValue();
                }
                case "depends" -> {
                    if (value instanceof PySequenceExpression sequenceExpression)
                        depends = sequenceToList(sequenceExpression);
                }
                case "data" -> {
                    if (value instanceof PySequenceExpression sequenceExpression)
                        data = sequenceToList(sequenceExpression);
                }
                case "demo" -> {
                    if (value instanceof PySequenceExpression sequenceExpression)
                        demo = sequenceToList(sequenceExpression);
                }
                case "license" -> {
                    if (value instanceof PyStringLiteralExpression stringExpression)
                        license = stringExpression.getStringValue();
                }
            }
        }
        return new ModuleDescriptor(module,
                manifest,
                id,
                name,
                version,
                description,
                authors,
                website,
                depends,
                data,
                demo,
                license);
    }

    private static List<String> sequenceToList(PySequenceExpression pySequence) {
        List<String> lst = PyUtil.strListValue(pySequence);
        // Don't ask me why PyUtil.strListValue returns null and not an empty List
        if (lst == null)
            return List.of();
        else
            return Collections.unmodifiableList(lst);
    }
}
