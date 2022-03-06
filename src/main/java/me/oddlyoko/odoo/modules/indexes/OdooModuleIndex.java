package me.oddlyoko.odoo.modules.indexes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
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
import me.oddlyoko.odoo.modules.models.OdooModule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Index containing all odoo modules
 */
public class OdooModuleIndex extends ScalarIndexExtension<String> {
    private static final ID<String, Void> NAME = ID.create("odoo.module");

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
            VirtualFile module = manifest.getParent();
            if (module != null)
                result.put(module.getName(), null);
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

    public static OdooModule getModule(@NotNull String moduleName, @NotNull Project project) {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        Collection<VirtualFile> manifestFiles = FileBasedIndex.getInstance().getContainingFiles(NAME, moduleName, scope);
        // Retrieves first module with this name
        for (VirtualFile manifest : manifestFiles) {
            OdooModule module = OdooModuleUtil.getModule(manifest, project);
            if (module != null)
                return module;
        }
        return null;
    }

    public static List<OdooModule> getAllModules(@NotNull Project project) {
        List<OdooModule> modules = new ArrayList<>();
        Collection<String> moduleNames = FileBasedIndex.getInstance().getAllKeys(NAME, project);
        for (String moduleName : moduleNames) {
            OdooModule module = getModule(moduleName, project);
            if (module != null)
                modules.add(module);
        }
        return modules;
    }
}
