package me.oddlyoko.odoo.models.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElementVisitor;
import me.oddlyoko.odoo.models.OdooModelFilter;
import me.oddlyoko.odoo.models.OdooModelUtil;
import me.oddlyoko.odoo.models.models.ModelDescriptor;
import me.oddlyoko.odoo.models.models.OdooModel;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Index containing all odoo modules
 */
public class OdooModelIndex extends ScalarIndexExtension<String> {
    private static final ID<String, Void> NAME = ID.create("odoo.model");

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

                    OdooModel model = OdooModel.fromPyClass(node);
                    if (model == null)
                        return;
                    ModelDescriptor descriptor = ModelDescriptor.fromPyClass(node);
                    if (descriptor == null)
                        return;
                    result.put(descriptor.getOdooModel(), null);
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

    public static Set<OdooModel> getModelsForModule(@NotNull String module, @NotNull Project project) {
        return getAllModels(project)
                .stream()
                .filter(odooModel -> module.equals(odooModel.getOdooModel()))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Retrieves all {@link OdooModel} instance from given id for given {@link Project}
     *
     * @param odooModel The id of the model
     * @param project   The project
     * @return A {@link Set} containing all {@link OdooModel} instance of given id for given {@link Project}
     */
    public static Set<OdooModel> getModels(@NotNull String odooModel, @NotNull Project project) {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(NAME, odooModel, scope);
        return files
                .stream()
                .flatMap(file -> OdooModelUtil.getModels(file, project)
                        .stream()
                        .filter(odooModel1 -> odooModel.equals(odooModel1.getOdooModel())))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Retrieves all {@link OdooModel} of given {@link Project}
     *
     * @param project The project
     * @return A {@link Set} containing all models for given {@link Project}
     */
    public static Set<OdooModel> getAllModels(@NotNull Project project) {
        return getAllStringModels(project)
                .stream()
                .flatMap(model -> getModels(model, project).stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Retrieves all models as string of given {@link Project}
     *
     * @param project The project
     * @return A {@link Collection} of string containing all models for given {@link Project}
     */
    public static Collection<String> getAllStringModels(@NotNull Project project) {
        return FileBasedIndex.getInstance().getAllKeys(NAME, project);
    }
}
