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
import java.util.Set;

public final class OdooModuleUtil {
    public static final List<String> MANIFEST_FILES = List.of(
            "__manifest__.py",
            "__openerp__.py"
    );
    public static final String INIT_FILE = "__init__.py";
    public static final String BASE_MODULE = "base";

    private OdooModuleUtil() {}

    public static boolean isValidManifest(@NotNull VirtualFile file) {
        for (String manifestFile : MANIFEST_FILES)
            if (manifestFile.equals(file.getName()))
                return true;
        return false;
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
     * @return The odoo module directory if found, <i>null</i> otherwise
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
    public static PsiFile getManifestFromDirectory(@NotNull PsiDirectory directory) {
        PsiFile file = null;
        for (String manifest : MANIFEST_FILES) {
            file = directory.findFile(manifest);
            if (file != null)
                break;
        }
        return file == null ? null : file.getOriginalFile();
    }

    public static Set<OdooModule> getAllModules(@NotNull Project project) {
        return OdooModuleIndex.getAllModules(project);
    }

    /**
     * Get available {@link OdooModule} from given {@link OdooModule}
     *
     * @param odooModule The {@link OdooModule}
     * @return A {@link List} containing the given module, his depends, depends of depends, ...
     */
    public static List<OdooModule> getAvailableModules(@NotNull OdooModule odooModule) {
        return odooModule.getModuleDepends()
                .stream()
                .map(s -> getModule(s, odooModule.getProject()))
                .toList();
    }

    public static OdooModule getModuleFromDirectory(@NotNull PsiDirectory psiDirectory) {
        return getModule(psiDirectory.getVirtualFile(), psiDirectory.getProject());
    }

    public static OdooModule getModuleFromFile(@NotNull PsiFile file) {
        VirtualFile vFile = file.getVirtualFile();
        if (vFile == null)
            return null;
        return getModule(vFile, file.getProject());
    }

    public static OdooModule getModule(@NotNull VirtualFile vFile, @NotNull Project project) {
        VirtualFile odooDirectory = getOdooModuleDirectory(vFile);
        if (odooDirectory == null)
            return null;
        PsiDirectory odooDir = PsiManager.getInstance(project).findDirectory(odooDirectory);
        if (odooDir == null)
            return null;
        odooDir = (PsiDirectory) odooDir.getOriginalElement();
        PsiFile manifest = getManifestFromDirectory(odooDir);
        if (manifest == null)
            return null;
        return new OdooModule(odooDir, manifest.getOriginalFile());
    }

    public static OdooModule getModule(@NotNull String moduleName, @NotNull Project project) {
        return OdooModuleIndex.getModule(moduleName, project);
    }

    public static OdooModule getModule(@NotNull PsiElement element) {
        if (element instanceof PsiFile psiFile)
            return getModuleFromFile(psiFile);
        if (element instanceof PsiDirectory psiDirectory)
            return getModuleFromDirectory(psiDirectory);
        if (element instanceof PomTargetPsiElement targetElement) {
            PomTarget target = targetElement.getTarget();
            if (target instanceof PsiTarget psiTarget)
                return getModule(psiTarget.getNavigationElement());
        }
        return getModuleFromFile(element.getContainingFile().getOriginalFile());
    }

    public static OdooModule getBaseModule(@NotNull Project project) {
        return getModule(BASE_MODULE, project);
    }
}
