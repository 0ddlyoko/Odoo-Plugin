package me.oddlyoko.odoo.modules.models;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represent an odoo module
 */
public final class OdooModule {
    public static final Key<CachedValue<ModuleDescriptor>> MODULE_DESCRIPTOR_KEY = new Key<>("module.descriptor");
    public static final Key<CachedValue<Set<String>>> MODULE_DEPENDS_KEY = new Key<>("module.depends.all");

    private final PsiDirectory directory;
    private final PsiFile manifestFile;

    public OdooModule(@NotNull PsiDirectory directory) {
        this(directory, OdooModuleUtil.getManifest(directory));
    }

    public OdooModule(@NotNull PsiDirectory directory, @Nullable PsiFile manifestFile) {
        this.directory = directory;
        this.manifestFile = manifestFile;
    }

    @NotNull
    public PsiDirectory getDirectory() {
        return directory;
    }

    @Nullable
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
        return CachedValuesManager.getCachedValue(manifestFile, MODULE_DESCRIPTOR_KEY, () -> {
            ModuleDescriptor descriptor = ModuleDescriptor.parseFile(manifestFile);
            // TODO Add correct ModificationTracker
            return CachedValueProvider.Result.create(descriptor, ModificationTracker.EVER_CHANGED);
        });
    }

    /**
     * Retrieves modules that are a depends of this {@link OdooModule}
     *
     * @return A {@link Set}
     */
    public Set<String> getModuleDepends() {
        return CachedValuesManager.getCachedValue(manifestFile, MODULE_DEPENDS_KEY, () -> {
            ModuleDescriptor descriptor = getModuleDescriptor();
            Set<String> result = new HashSet<>();
            if (descriptor != null) {
                for (String depend : descriptor.getDepends()) {
                    OdooModule module = OdooModuleUtil.getModule(depend, getProject());
                    if (module != null) {
                        result.add(depend);
                        result.addAll(module.getModuleDepends());
                    }
                }
            }
            // TODO Add correct ModificationTracker
            return CachedValueProvider.Result.create(Collections.unmodifiableSet(result), ModificationTracker.EVER_CHANGED);
        });
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
