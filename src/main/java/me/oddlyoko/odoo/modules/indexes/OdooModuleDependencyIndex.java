package me.oddlyoko.odoo.modules.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import me.oddlyoko.odoo.modules.OdooManifestFilter;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import me.oddlyoko.odoo.modules.models.ModuleDescriptor;
import me.oddlyoko.odoo.modules.models.OdooModule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Index containing which odoo module depends on which odoo module
 */
public class OdooModuleDependencyIndex extends ScalarIndexExtension<String> {
    private static final ID<String, Void> NAME = ID.create("odoo.module.dependency");

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
            VirtualFile manifest = inputData.getFile();
            PsiFile file = PsiManager.getInstance(inputData.getProject()).findFile(manifest);
            if (file == null)
                return result;
            ModuleDescriptor descriptor = ModuleDescriptor.parseFile(file);
            if (descriptor == null)
                return result;
            descriptor.depends().forEach(s -> result.put(s, null));
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
        return OdooManifestFilter.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    /**
     * Retrieves direct depend of given manifest file
     *
     * @param vManifestFile The manifest file
     * @param project       The project
     * @return A list of modules that depends on given manifest file
     */
    @NotNull
    public static List<String> getDepends(@NotNull VirtualFile vManifestFile, @NotNull Project project) {
        return new ArrayList<>(FileBasedIndex.getInstance().getFileData(NAME, vManifestFile, project).keySet());
    }

    @NotNull
    public static List<OdooModule> getOdooDepending(@NotNull String moduleName, @NotNull Project project) {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        Collection<VirtualFile> manifestFiles = FileBasedIndex.getInstance().getContainingFiles(NAME, moduleName, scope);
        List<OdooModule> dependencies = new ArrayList<>();
        for (VirtualFile manifest : manifestFiles) {
            OdooModule module = OdooModuleUtil.getModule(manifest, project);
            if (module != null)
                dependencies.add(module);
        }
        return dependencies;
    }
}
