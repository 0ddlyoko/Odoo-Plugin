package me.oddlyoko.odoo.modules.models;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.PythonFileType;
import me.oddlyoko.odoo.OdooUtil;
import me.oddlyoko.odoo.models.OdooModelUtil;
import me.oddlyoko.odoo.models.models.OdooModel;
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

    private static final String KEY_FILES = "module.files";
    private static final String KEY_PYTHON_FILES = "module.files.python";
    private static final String KEY_PYTHON_FILES_DEPENDS = "module.files.python.depends";
    private static final String KEY_ODOO_MODELS = "module.models";

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
                ModuleDescriptor.parseFile(manifestFile), OdooManifestModificationTracker.get(directory.getName()));
    }

    /**
     * Retrieves modules that are a depend of this {@link OdooModule}
     *
     * @return A {@link List}
     */
    public List<String> getModuleDepends() {
        return OdooUtil.getData(getDirectory(), KEY_MODULE_DEPENDS, () -> {
            ModuleDescriptor descriptor = getModuleDescriptor();
            List<String> result = new ArrayList<>();
            if (descriptor != null) {
                for (String depend : descriptor.getDepends()) {
                    OdooModule module = OdooModuleUtil.getModule(depend, getProject());
                    if (module != null) {
                        result.add(depend);
                        result.addAll(module.getModuleDepends());
                    }
                }
            }
            return Collections.unmodifiableList(result);
        }, OdooManifestModificationTracker.get(directory.getName()));
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
        modules.addAll(OdooUtil.getData(getDirectory(), KEY_MODULE_DEPENDS_ODOO_MODULE, () ->
                getModuleDepends().stream()
                        .map(s -> OdooModuleUtil.getModule(s, getProject()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()), OdooManifestModificationTracker.get(directory.getName())
        ));
        return modules;
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
            }, OdooModuleModificationTracker.get(directory.getName()));

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
                        .collect(Collectors.toList()), OdooModuleModificationTracker.get(directory.getName()));
    }

    /**
     * Retrieves models that are defined in this {@link OdooModule}<br />
     * If <i>includeDepends</i> is <i>true</i> then models defined in depends modules are also retrieved
     *
     * @param includeDepends <i>true</i> to include models defined in depends modules
     * @return A {@link List} of {@link OdooModel}
     */
    public List<OdooModel> getModels(boolean includeDepends) {
        if (!includeDepends)
            return OdooUtil.getData(getDirectory(), KEY_ODOO_MODELS, () ->
                    getPythonFiles(false).stream()
                            .flatMap(file -> OdooModelUtil.getModels(file, getProject()).stream())
                            .collect(Collectors.toList()), OdooModuleModificationTracker.get(directory.getName()));

        return getModules(true).stream()
                .flatMap(odooModule -> odooModule.getModels(false).stream())
                .collect(Collectors.toList());
    }

    public GlobalSearchScope getOdooModuleScope(boolean includeDepends) {
        return GlobalSearchScope.filesWithLibrariesScope(getProject(), getVirtualFiles(includeDepends));
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
