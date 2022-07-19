package me.oddlyoko.odoo.fields.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElementVisitor;
import com.jetbrains.python.psi.PyTargetExpression;
import me.oddlyoko.odoo.fields.OdooFieldUtil;
import me.oddlyoko.odoo.fields.models.OdooField;
import me.oddlyoko.odoo.models.OdooModelFilter;
import me.oddlyoko.odoo.models.OdooModelUtil;
import me.oddlyoko.odoo.models.models.ModelDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Index containing all odoo fields
 */
public class OdooFieldIndex extends ScalarIndexExtension<String> {
    private static final ID<String, Void> NAME = ID.create("odoo.field");

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Void> result = new HashMap<>();
            PsiFile file = inputData.getPsiFile();
            file.acceptChildren(new PyElementVisitor() {
                @Override
                public void visitPyClass(@NotNull PyClass node) {
                    super.visitPyClass(node);
                    node = (PyClass) node.getOriginalElement();
                    // Check if it's valid odoo model
                    if (OdooModelUtil.isInvalidOdooPyClass(node))
                        return;
                    ModelDescriptor descriptor = ModelDescriptor.fromPyClass(node);
                    if (descriptor == null)
                        return;
                    for (PyTargetExpression attr : node.getClassAttributes())
                        if (attr.getName() != null && OdooFieldUtil.isValidField(attr))
                            result.put(attr.getName(), null);
                }
            });
            return result;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return OdooModelFilter.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    /**
     * Retrieves all {@link OdooField} of given {@link Project}
     *
     * @param project The project
     * @return A {@link Set} containing all fields for given {@link Project}
     *//*
    public static Set<OdooField> getAllFields(@NotNull Project project) {
        return getAllStringFields(project)
                .stream()
                .flatMap(field -> getFields(field, project).stream())
                .collect(Collectors.toUnmodifiableSet());
    }*/

    /**
     * Retrieves all fields as string of given {@link Project}
     *
     * @param project The project
     * @return A {@link Collection} of string containing all fields for given {@link Project}
     */
    public static Collection<String> getAllStringFields(@NotNull Project project) {
        return FileBasedIndex.getInstance().getAllKeys(NAME, project);
    }
}
