package me.oddlyoko.odoo.modules.models;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import me.oddlyoko.odoo.modules.OdooModuleUtil;

import java.util.Objects;

/**
 * Represent an odoo module
 */
public final class OdooModule {
    public static final Key<CachedValue<ModuleDescriptor>> MODULE_DESCRIPTOR_KEY = new Key<>("moduleDescriptor");

    private final PsiDirectory directory;
    private final PsiFile manifestFile;

    public OdooModule(PsiDirectory directory) {
        this(directory, OdooModuleUtil.getManifest(directory));
    }

    public OdooModule(PsiDirectory directory, PsiFile manifestFile) {
        this.directory = directory;
        this.manifestFile = manifestFile;
    }

    public PsiDirectory getDirectory() {
        return directory;
    }

    public PsiFile getManifestFile() {
        return manifestFile;
    }

    public String getName() {
        return directory.getName();
    }

    public ModuleDescriptor getModuleDescriptor() {
        return CachedValuesManager.getCachedValue(manifestFile, MODULE_DESCRIPTOR_KEY, () -> {
            ModuleDescriptor descriptor = ModuleDescriptor.parseFile(manifestFile);
            return CachedValueProvider.Result.create(descriptor, ModificationTracker.NEVER_CHANGED);
        });
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
