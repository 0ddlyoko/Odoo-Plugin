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
import me.oddlyoko.odoo.modules.indexes.OdooModuleDependencyIndex;
import me.oddlyoko.odoo.modules.trackers.OdooManifestModificationTracker;
import me.oddlyoko.odoo.modules.trackers.OdooModuleModificationTracker;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represent an odoo module
 */
public final class OdooModule {
    private static final String KEY_MODULE_DESCRIPTOR = "module.descriptor";

    private static final String KEY_FILES = "module.files";

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
                ModuleDescriptor.parseFile(manifestFile), manifestFile);
    }

    /**
     * Retrieves modules that are a depend of this {@link OdooModule}
     *
     * @return An unmodifiable {@link List}
     */
    public List<String> getModuleDepends() {
        List<String> result = new ArrayList<>();
        for (String depend : getDirectModuleDepends()) {
            OdooModule module = OdooModuleUtil.getModule(depend, getProject());
            if (module != null) {
                result.add(depend);
                result.addAll(module.getModuleDepends());
            }
        }
        return result.stream().distinct().collect(Collectors.toUnmodifiableList());
    }

    public List<OdooModule> getOdooModuleDepends() {
        return getModuleDepends().stream().map(s -> OdooModuleUtil.getModule(s, getProject())).collect(Collectors.toList());
    }

    /**
     * Retrieves modules that are a depend of this {@link OdooModule}
     *
     * @return A {@link List}
     */
    public List<OdooModule> getOdooModuleDepends(boolean includeThisOne) {
        Stream<OdooModule> stream = Stream.concat(
                getModuleDepends().stream().map(s -> OdooModuleUtil.getModule(s, getProject())),
                Stream.of(OdooModuleUtil.getBaseModule(getProject()))
        );
        if (includeThisOne)
            stream = Stream.concat(Stream.of(this), stream);

        return stream.filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    /**
     * Retrieves direct module dependencies of this {@link OdooModule}
     * 
     * @return A modifiable {@link List}
     */
    public List<String> getDirectModuleDepends() {
        return OdooModuleDependencyIndex.getDepends(getManifestFile().getVirtualFile(), getProject());
    }

    public List<OdooModule> getDirectOdooModuleDepends() {
        return getDirectModuleDepends().stream().map(s -> OdooModuleUtil.getModule(s, getProject())).collect(Collectors.toList());
    }

    public List<OdooModule> getOdooModuleDepending() {
        return OdooModuleDependencyIndex.getOdooDepending(getName(), getProject());
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

        return getOdooModuleDepends(true).stream()
                .flatMap(odooModule -> odooModule.getVirtualFiles(false).stream())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves files that are in this {@link OdooModule} and are python files
     *
     * @return A {@link List} of {@link VirtualFile}
     */
    public List<VirtualFile> getPythonFiles(boolean includeDepends) {
        return getVirtualFiles(includeDepends).stream()
                .filter(file -> file.getFileType() == PythonFileType.INSTANCE)
                .collect(Collectors.toList());
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
