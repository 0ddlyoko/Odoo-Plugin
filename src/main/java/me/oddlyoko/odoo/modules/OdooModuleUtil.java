package me.oddlyoko.odoo.modules;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.PomTarget;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTarget;
import me.oddlyoko.odoo.modules.indexes.OdooModuleIndex;
import me.oddlyoko.odoo.modules.models.OdooModule;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class OdooModuleUtil {
    public static final String[] MANIFEST_FILES = new String[]{
            "__manifest__.py",
            //"__openerp__.py",
    };
    public static final String INIT_FILE = "__init__.py";

    private OdooModuleUtil() {}

    public static boolean isValidManifest(@NotNull VirtualFile file) {
        for (String manifestFile : MANIFEST_FILES)
            if (!manifestFile.equals(file.getName()))
                return false;
        return true;
    }

    public static boolean isOdooModuleDirectory(@NotNull VirtualFile dir) {
        if (!dir.isDirectory())
            return false;
        if (dir.findChild(INIT_FILE) == null)
            return false;
        for (String manifestFile : MANIFEST_FILES)
            if (dir.findChild(manifestFile) != null)
                return true;
        return false;
    }

    /**
     * Check if given file is inside an odoo module
     *
     * @param file The file to check
     * @return <i>true</i> if given file is inside an odoo module
     */
    public static boolean isInOdooModule(@NotNull VirtualFile file) {
        return getOdooModuleDirectory(file) != null;
    }

    /**
     * Retrieves the odoo module directory from a given file or directory
     *
     * @param file The file or directory
     * @return The odoo module diectory if found, <i>null</i> otherwise
     */
    public static VirtualFile getOdooModuleDirectory(@NotNull VirtualFile file) {
        if (isOdooModuleDirectory(file))
            return file;
        VirtualFile parent = file;
        while ((parent = parent.getParent()) != null)
            if (isOdooModuleDirectory(parent))
                return parent;
        return null;
    }

    /**
     * Retrieves the manifest file from an odoo directory
     *
     * @param directory The odoo directory
     * @return The manifest file if found, <i>null</i> otherwise
     */
    public static PsiFile getManifest(@NotNull PsiDirectory directory) {
        PsiFile file = null;
        for (String manifest : MANIFEST_FILES) {
            file = directory.findFile(manifest);
            if (file != null)
                break;
        }
        return file == null ? null : file.getOriginalFile();
    }

    public static List<OdooModule> getModules(@NotNull Project project) {
        return OdooModuleIndex.getAllModules(project);
    }

    public static OdooModule getModule(@NotNull VirtualFile file, @NotNull Project project) {
        VirtualFile odooDirectory = getOdooModuleDirectory(file);
        if (odooDirectory == null)
            return null;
        PsiDirectory odooDir = PsiManager.getInstance(project).findDirectory(odooDirectory);
        if (odooDir == null)
            return null;
        return getModule(odooDir);
    }

    public static OdooModule getModule(@NotNull PsiDirectory odooDirectory) {
        // Call getOriginalElement() to avoid using the copied PsiDirectory and to use data stored inside original directory
        odooDirectory = (PsiDirectory) odooDirectory.getOriginalElement();
        PsiFile manifest = getManifest(odooDirectory);
        if (manifest == null)
            return null;
        return new OdooModule(odooDirectory, manifest);
    }

    public static OdooModule getModule(@NotNull PsiFile manifest) {
        // Call getOriginalFile() to avoid using the copied PsiFile and to use data stored inside original file
        manifest = manifest.getOriginalFile();
        PsiDirectory odooDirectory = manifest.getParent();
        if (odooDirectory == null)
            return null;
        return new OdooModule(odooDirectory, manifest);
    }

    public static OdooModule getModule(@NotNull String moduleName, @NotNull Project project) {
        return OdooModuleIndex.getModule(moduleName, project);
    }

    public static OdooModule getModule(@NotNull PsiElement element) {
        if (element instanceof PsiFile)
            return getModule((PsiFile) element);
        if (element instanceof PsiDirectory)
            return getModule((PsiDirectory) element);
        if (element instanceof PomTargetPsiElement) {
            PomTarget target = ((PomTargetPsiElement) element).getTarget();
            if (target instanceof PsiTarget)
                return getModule(((PsiTarget) target).getNavigationElement());
        }
        return getModule(element.getContainingFile());
    }
}
