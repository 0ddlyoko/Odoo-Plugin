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
public final class ModuleDescriptor {
    private final PsiDirectory directory;
    private final PsiFile manifest;
    private final String id;
    private final String name;
    private final String version;
    private final String description;
    private final List<String> author;
    private final String website;
    private final List<String> depends;
    private final List<String> data;
    private final List<String> demo;
    private final String license;

    public ModuleDescriptor(@NotNull PsiDirectory directory,
                            @NotNull PsiFile manifest,
                            @NotNull String id,
                            @NotNull String name,
                            @NotNull String version,
                            @NotNull String description,
                            @NotNull List<String> author,
                            @NotNull String website,
                            @NotNull List<String> depends,
                            @NotNull List<String> data,
                            @NotNull List<String> demo,
                            @NotNull String license) {
        this.directory = directory;
        this.manifest = manifest;
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.author = author;
        this.website = website;
        this.depends = depends;
        this.data = data;
        this.demo = demo;
        this.license = license;
    }

    @NotNull
    public PsiDirectory getDirectory() {
        return directory;
    }

    @NotNull
    public PsiFile getManifest() {
        return manifest;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getVersion() {
        return version;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public List<String> getAuthor() {
        return author;
    }

    @NotNull
    public String getWebsite() {
        return website;
    }

    @NotNull
    public List<String> getDepends() {
        return depends;
    }

    @NotNull
    public List<String> getData() {
        return data;
    }

    @NotNull
    public List<String> getDemo() {
        return demo;
    }

    @NotNull
    public String getLicense() {
        return license;
    }

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
                case "name":
                    if (value instanceof PyStringLiteralExpression)
                        name = ((PyStringLiteralExpression) value).getStringValue();
                    break;
                case "version":
                    if (value instanceof PyStringLiteralExpression)
                        version = ((PyStringLiteralExpression) value).getStringValue();
                    break;
                case "description":
                    if (value instanceof PyStringLiteralExpression)
                        description = ((PyStringLiteralExpression) value).getStringValue();
                    break;
                case "author":
                    if (value instanceof PyStringLiteralExpression)
                        authors = List.of(((PyStringLiteralExpression) value).getStringValue());
                    else if (value instanceof PySequenceExpression)
                        authors = sequenceToList((PySequenceExpression) value);
                    break;
                case "website":
                    if (value instanceof PyStringLiteralExpression)
                        website = ((PyStringLiteralExpression) value).getStringValue();
                    break;
                case "depends":
                    if (value instanceof PySequenceExpression)
                        depends = sequenceToList((PySequenceExpression) value);
                    break;
                case "data":
                    if (value instanceof PySequenceExpression)
                        data = sequenceToList((PySequenceExpression) value);
                    break;
                case "demo":
                    if (value instanceof PySequenceExpression)
                        demo = sequenceToList((PySequenceExpression) value);
                    break;
                case "license":
                    if (value instanceof PyStringLiteralExpression)
                        license = ((PyStringLiteralExpression) value).getStringValue();
                    break;
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
