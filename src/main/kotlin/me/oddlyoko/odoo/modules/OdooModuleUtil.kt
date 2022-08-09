package me.oddlyoko.odoo.modules

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.PomTargetPsiElement
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTarget
import me.oddlyoko.odoo.modules.indexes.OdooModuleIndex

import me.oddlyoko.odoo.modules.models.OdooModule


object OdooModuleUtil {
    val MANIFEST_FILES = listOf(
        "__manifest__.py",
        "__openerp__.py",
    )
    val INIT_FILE = "__init__.py"
    val BASE_MODULE = "base"

    fun isValidManifest(file: VirtualFile): Boolean = MANIFEST_FILES.any { it == file.name }

    fun isOdooModuleDirectory(dir: VirtualFile): Boolean {
        if (!dir.isDirectory)
            return false
        if (dir.findChild(INIT_FILE) == null)
            return false
        return MANIFEST_FILES.any { dir.findChild(it) != null }
    }

    /**
     * Check if given file is inside an odoo module
     */
    fun isInOdooModule(file: VirtualFile) = getOdooModuleDirectory(file) != null

    /**
     * Retrieves the odoo module directory from a given file or directory
     */
    fun getOdooModuleDirectory(file: VirtualFile): VirtualFile? {
        if (isOdooModuleDirectory(file))
            return file
        var parent: VirtualFile = file
        while (true) {
            parent = parent.parent ?: return null
            if (isOdooModuleDirectory(parent))
                return parent
        }
    }

    /**
     * Retrieves the manifest file from an odoo directory
     */
    fun getManifestFromDirectory(directory: PsiDirectory): PsiFile? =
        MANIFEST_FILES.firstNotNullOfOrNull { directory.findFile(it) }

    fun getAllModules(project: Project): List<OdooModule> = OdooModuleIndex.getAllOdooModules(project)

    /**
     * Get available OdooModule from given OdooModule
     */
    fun getAvailableModules(odooModule: OdooModule): List<OdooModule> = odooModule.getOdooModuleDepends()

    fun getModuleFromDirectory(directory: PsiDirectory): OdooModule? =
        getModule(directory.virtualFile, directory.project)

    fun getModuleFromFile(file: PsiFile): OdooModule? =
        file.virtualFile?.let { getModule(it, file.project) }

    fun getModule(vFile: VirtualFile, project: Project): OdooModule? {
        val odooDir = getOdooModuleDirectory(vFile) ?: return null
        val psiDir = PsiManager.getInstance(project).findDirectory(odooDir)?.originalElement ?: return null
        val psiFile = getManifestFromDirectory(psiDir as PsiDirectory) ?: return null
        return OdooModule(psiDir, psiFile.originalFile)
    }

    fun getModule(moduleName: String, project: Project): OdooModule? = OdooModuleIndex.getModule(moduleName, project)

    fun getModule(element: PsiElement): OdooModule? = when (element) {
        is PsiFile -> getModuleFromFile(element)
        is PsiDirectory -> getModuleFromDirectory(element)
        is PomTargetPsiElement -> if (element.target is PsiTarget) getModule((element.target as PsiTarget).navigationElement) else getModuleFromFile(element.containingFile.originalFile)
        else -> getModuleFromFile(element.containingFile.originalFile)
    }

    fun getBaseModule(project: Project): OdooModule? = getModule(BASE_MODULE, project)
}
