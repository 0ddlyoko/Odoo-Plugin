package me.oddlyoko.odoo.models.models;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.jetbrains.python.psi.PyClass;
import me.oddlyoko.odoo.models.OdooModelUtil;
import me.oddlyoko.odoo.modules.OdooModuleUtil;

import java.util.Objects;

/**
 * Represent an odoo model
 */
public final class OdooModel {
    public static final String NAME_KEY = "_name";
    public static final String DESCRIPTION_KEY = "_description";
    public static final String INHERIT_KEY = "_inherit";
    public static final String INHERITS_KEY = "_inherits";
    public static final Key<CachedValue<OdooModel>> MODEL_KEY = new Key<>("model");

    private final PsiDirectory directory;
    private final PsiFile file;
    private final PyClass pyClass;
    private final String odooModel;
    private final boolean valid;

    private OdooModel(PsiDirectory directory, PsiFile file, PyClass pyClass) {
        this.directory = directory;
        this.file = file;
        this.pyClass = pyClass;
        ModelDescriptor descriptor = getModelDescriptor();
        this.odooModel = descriptor != null ? descriptor.getOdooModel() : null;
        this.valid = odooModel == null;
    }

    public PsiDirectory getDirectory() {
        return directory;
    }

    public PsiFile getFile() {
        return file;
    }

    public PyClass getPyClass() {
        return pyClass;
    }

    public String getOdooModel() {
        return odooModel;
    }

    public ModelDescriptor getModelDescriptor() {
        return OdooModelUtil.getDescriptor(pyClass);
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OdooModel odooModel = (OdooModel) o;
        return Objects.equals(directory, odooModel.directory) &&
                Objects.equals(file, odooModel.file) &&
                Objects.equals(pyClass, odooModel.pyClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(directory, file, pyClass);
    }

    @Override
    public String toString() {
        return pyClass.getName();
    }

    /**
     * Retrieves existing {@link OdooModel} linked to given {@link PyClass} or create it
     *
     * @param pyClass The {@link PyClass}
     * @return Existing {@link OdooModel} linked to given {@link PyClass} or new one
     */
    public static OdooModel fromPyClass(PyClass pyClass) {
        return CachedValuesManager.getCachedValue(pyClass, MODEL_KEY,
                () -> CachedValueProvider.Result.create(parsePyClass(pyClass), ModificationTracker.NEVER_CHANGED));
    }

    /**
     * Create a new {@link OdooModel} based on given {@link PyClass}
     *
     * @param pyClass The class that will be used to create the {@link OdooModel}
     * @return Created {@link OdooModel}, or null if given class is not a valid {@link OdooModel}
     */
    private static OdooModel parsePyClass(PyClass pyClass) {
        PsiFile file = pyClass.getContainingFile();
        if (file == null)
            return null;
        VirtualFile vFile = file.getVirtualFile();
        if (vFile == null)
            return null;
        VirtualFile odooDir = OdooModuleUtil.getOdooModuleDirectory(vFile);
        if (odooDir == null)
            return null;
        PsiDirectory directory = PsiManager.getInstance(pyClass.getProject()).findDirectory(odooDir);
        if (directory == null)
            return null;
        return new OdooModel(directory, file, pyClass);
    }
}
