package me.oddlyoko.odoo.modules.models;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.PythonFileType;
import me.oddlyoko.odoo.OdooUtil;
import me.oddlyoko.odoo.models.indexes.OdooModelIndex;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import me.oddlyoko.odoo.modules.tracker.OdooManifestModificationTracker;
import me.oddlyoko.odoo.modules.tracker.OdooModuleModificationTracker;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represent an odoo module
 */
public final class OdooModule {
    private static final String KEY_MODULE_DESCRIPTOR = "module.descriptor";
    private static final String KEY_MODULE_DEPENDS = "module.depends.all";
    private static final String KEY_MODULE_DEPENDS_ODOO_MODULE = "module.depends.all.odoo.module";
    private static final String KEY_MODULE_DEPENDS_ODOO_MODULE_SELF = "module.depends.all.odoo.module.self";

    private static final String KEY_FILES = "module.files";
    private static final String KEY_PYTHON_FILES = "module.files.python";
    private static final String KEY_PYTHON_FILES_DEPENDS = "module.files.python.depends";

    private final PsiDirectory directory;
    private final PsiFile manifestFile;

    public OdooModule(@NotNull PsiDirectory directory, @NotNull PsiFile manifestFile) {
        this.directory = directory;
        this.manifestFile = manifestFile;
    }

    @NotNull
    public PsiDirectory getDirectory() {
        return directory;
    }

    @NotNull
    public PsiFile getManifestFile() {
        return manifestFile;
    }

    @NotNull
    public Project getProject() {
        return directory.getProject();
    }

    public String getName() {
        return directory.getName();
    }

    public ModuleDescriptor getModuleDescriptor() {
        return OdooUtil.getData(getDirectory(), KEY_MODULE_DESCRIPTOR, () ->
                ModuleDescriptor.parseFile(manifestFile), getOdooManifestModificationTracker());
    }

    /**
     * Retrieves modules that are a depend of this {@link OdooModule}
     *
     * @return An unmodifable {@link List}
     */
    public List<String> getModuleDepends() {
        return OdooUtil.getData(getDirectory(), KEY_MODULE_DEPENDS, () -> {
            List<String> result = new ArrayList<>();
            for (String depend : getDirectModuleDepends()) {
                OdooModule module = OdooModuleUtil.getModule(depend, getProject());
                if (module != null) {
                    result.add(depend);
                    result.addAll(module.getModuleDepends());
                }
            }
            return Collections.unmodifiableList(result);
        }, getOdooManifestModificationTracker());
    }

    /**
     * Retrieves direct module dependencies of this {@link OdooModule}
     * 
     * @return A modifiable {@link List}
     */
    public List<String> getDirectModuleDepends() {
        ModuleDescriptor descriptor = getModuleDescriptor();
        if (descriptor == null)
            return List.of();
        return new ArrayList<>(descriptor.getDepends());
    }

    /**
     * Retrieves modules that are a depend of this {@link OdooModule}
     *
     * @return A {@link List}
     */
    public List<OdooModule> getModules(boolean includeThisOne) {
        List<OdooModule> modules = new ArrayList<>();
        // Add this module at first in the list
        if (includeThisOne)
            modules.add(this);
        String key = includeThisOne ? KEY_MODULE_DEPENDS_ODOO_MODULE : KEY_MODULE_DEPENDS_ODOO_MODULE_SELF;
        modules.addAll(OdooUtil.getData(getDirectory(), key, () ->
                getModuleDepends().stream()
                        .map(s -> OdooModuleUtil.getModule(s, getProject()))
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()),
                getOdooDependsManifestModificationTracker(includeThisOne).toArray()
        ));
        OdooModule baseModule = OdooModuleUtil.getModule(OdooModuleUtil.BASE_MODULE, getProject());
        modules.add(baseModule);
        // Remove duplicated module
        return modules.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Check if this {@link OdooModule} can see given module<br />
     * This module can see given module only if this module depends on given module name
     *
     * @param target The target module
     * @return <i>true</i> If this module depends on given module name
     */
    public boolean canSeeModule(String target) {
        return getModuleDepends().contains(target);
    }

    /**
     * Retrieves files that are in this {@link OdooModule}
     *
     * @return A {@link List} of {@link VirtualFile}
     */
    public List<VirtualFile> getVirtualFiles(boolean includeDepends) {
        if (!includeDepends)
            return OdooUtil.getData(getDirectory(), KEY_FILES, () -> {
                List<VirtualFile> files = new ArrayList<>();
                VfsUtil.processFilesRecursively(getDirectory().getVirtualFile(), files::add);
                return files;
            }, getOdooModuleModificationTracker());

        return getModules(true).stream()
                .flatMap(odooModule -> odooModule.getVirtualFiles(false).stream())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves files that are in this {@link OdooModule} and are python files
     *
     * @return A {@link List} of {@link VirtualFile}
     */
    public List<VirtualFile> getPythonFiles(boolean includeDepends) {
        return OdooUtil.getData(getDirectory(), includeDepends ? KEY_PYTHON_FILES_DEPENDS : KEY_PYTHON_FILES, () ->
                getVirtualFiles(includeDepends).stream()
                        .filter(file -> file.getFileType() == PythonFileType.INSTANCE)
                        .collect(Collectors.toList()),
                getOdooDependsModuleModificationTracker(includeDepends).toArray());
    }

    public List<String> getModels(boolean includeDepends) {
        return new ArrayList<>(OdooModelIndex.getAllModels(getProject(), getOdooPythonModuleScope(includeDepends)));
    }

    public GlobalSearchScope getOdooModuleScope(boolean includeDepends) {
        return GlobalSearchScope.filesWithLibrariesScope(getProject(), getVirtualFiles(includeDepends));
    }

    public GlobalSearchScope getOdooPythonModuleScope(boolean includeDepends) {
        return GlobalSearchScope.filesWithLibrariesScope(getProject(), getPythonFiles(includeDepends));
    }

    public OdooManifestModificationTracker getOdooManifestModificationTracker() {
        return OdooManifestModificationTracker.get(getName());
    }

    public OdooModuleModificationTracker getOdooModuleModificationTracker() {
        return OdooModuleModificationTracker.get(getName());
    }

    /**
     * Retrieves modules' {@link OdooManifestModificationTracker} that are a direct depend of this module
     *
     * @return A {@link List}
     */
    public List<OdooManifestModificationTracker> getOdooDependsManifestModificationTracker(boolean includeThisOne) {
        List<String> depends = getDirectModuleDepends();
        if (includeThisOne)
            depends.add(getName());
        return depends.stream().map(OdooManifestModificationTracker::get).collect(Collectors.toList());
    }

    /**
     * Retrieves modules' {@link OdooModuleModificationTracker} that are a direct depend of this module
     *
     * @return A {@link List}
     */
    public List<OdooModuleModificationTracker> getOdooDependsModuleModificationTracker(boolean includeThisOne) {
        List<String> depends = getDirectModuleDepends();
        if (includeThisOne)
            depends.add(getName());
        return depends.stream().map(OdooModuleModificationTracker::get).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OdooModule that = (OdooModule) o;
        return Objects.equals(directory, that.directory) && Objects.equals(manifestFile, that.manifestFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directory, manifestFile);
    }

    @Override
    public String toString() {
        return getName();
    }
}
