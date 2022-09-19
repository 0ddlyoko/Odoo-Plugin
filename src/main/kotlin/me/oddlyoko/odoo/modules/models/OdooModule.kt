package me.oddlyoko.odoo.modules.models

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.jetbrains.python.PythonFileType
import me.oddlyoko.odoo.OdooUtil
import me.oddlyoko.odoo.models.indexes.OdooModelIndex
import me.oddlyoko.odoo.modules.OdooModuleUtil
import me.oddlyoko.odoo.modules.indexes.OdooModuleDependencyIndex
import me.oddlyoko.odoo.modules.trackers.OdooManifestModificationTracker
import me.oddlyoko.odoo.modules.trackers.OdooModuleModificationTracker

data class OdooModule(val directory: PsiDirectory, val manifest: PsiFile) {

    val virtualFile: VirtualFile
        get() = manifest.virtualFile
    val project: Project
        get() = directory.project
    val name: String
        get() = directory.name
    val moduleDescriptor: ModuleDescriptor?
        get() = OdooUtil.getData(directory, KEY_MODULE_DESCRIPTOR, manifest) {
            ModuleDescriptor.fromFile(manifest)
        }

    /**
     * Retrieves modules that this module depends on
     */
    fun getModuleDepends(): List<String> {
        val result = arrayListOf<String>()
        getDirectModuleDepends().mapNotNull { OdooModuleUtil.getModule(it, project) }.forEach {
            result.add(it.name)
            result.addAll(it.getModuleDepends())
        }
        result.add(OdooModuleUtil.BASE_MODULE)
        return result.distinct()
    }

    /**
     * Retrieves Odoo modules that this module depends on
     */
    fun getOdooModuleDepends(): List<OdooModule> = getModuleDepends().mapNotNull { OdooModuleUtil.getModule(it, project) }

    /**
     * Retrieves Odoo modules that this module depends on
     */
    fun getOdooModuleDepends(includeThisOne: Boolean): List<OdooModule> {
        val result = arrayListOf<OdooModule>()
        result.addAll(getOdooModuleDepends())
        if (includeThisOne)
            result.add(this)
        return result.distinct()
    }

    /**
     * Retrieves modules that depend on this module
     */
    fun getModuleDependings(): List<String> = getOdooModuleDependings().map { it.name }

    /**
     * Retrieves Odoo modules that dpeend on this module
     */
    fun getOdooModuleDependings(): List<OdooModule> {
        val result = arrayListOf<OdooModule>()
        getDirectOdooModuleDepending().forEach {
            result.add(it)
            result.addAll(it.getOdooModuleDependings())
        }
        return result.distinct()
    }

    /**
     * Retrieves Odoo modules that dpeend on this module
     */
    fun getOdooModuleDependings(includeThisOne: Boolean): List<OdooModule> {
        val result = arrayListOf<OdooModule>()
        result.addAll(getOdooModuleDependings())
        if (includeThisOne)
            result.add(this)
        return result.distinct()
    }

    /**
     * Retrieves direct modules that this OdooModule depends on
     */
    fun getDirectModuleDepends(): List<String> = OdooModuleDependencyIndex.getDepends(virtualFile, project)


    /**
     * Retrieves direct Odoo module that this OdooModule depends on
     */
    fun getDirectOdooModuleDepends(): List<OdooModule> = getDirectModuleDepends().mapNotNull { OdooModuleUtil.getModule(it, project) }

    /**
     * Retrieves direct modules that depends on this OdooModule
     */
    fun getDirectModuleDepending(): List<String> = getDirectOdooModuleDepending().map { it.name }

    /**
     * Retrieves direct Odoo modules that depends on this OdooModule
     */
    fun getDirectOdooModuleDepending(): List<OdooModule> = OdooModuleDependencyIndex.getOdooDepending(name, project)

    /**
     * Check if this OdooModule can see given module<br />
     * This module can see given module only if this module depends on given module name
     */
    fun canSeeModule(target: String): Boolean = getModuleDepends().contains(target)

    fun getVirtualFiles(includeDepends: Boolean): List<VirtualFile> =
        if (includeDepends)
            getOdooModuleDepends(true).flatMap { it.getVirtualFiles(false) }
        else
            OdooUtil.getData(directory, KEY_FILES, getOdooModuleModificationTracker()) {
                val files = arrayListOf<VirtualFile>()
                VfsUtil.processFilesRecursively(directory.virtualFile) {
                    files.add(it)
                }
                return@getData files
            } ?: listOf()

    fun getPythonFiles(includeDepends: Boolean): List<VirtualFile> =
        getVirtualFiles(includeDepends).filter { it.fileType == PythonFileType.INSTANCE }

    fun getDependingVirtualFiles(): List<VirtualFile> =
        getOdooModuleDependings().flatMap { it.getVirtualFiles(false) }

    fun getDependingPythonFiles(): List<VirtualFile> =
        getDependingVirtualFiles().filter { it.fileType == PythonFileType.INSTANCE }

    fun getModels(includeDepends: Boolean): List<String> =
        OdooModelIndex.getAllModels(project, getOdooPythonModuleScope(includeDepends))

    fun getOdooModuleScope(includeDepends: Boolean): GlobalSearchScope =
        GlobalSearchScope.filesWithLibrariesScope(project, getVirtualFiles(includeDepends))

    fun getOdooPythonModuleScope(includeDepends: Boolean): GlobalSearchScope =
        GlobalSearchScope.filesWithLibrariesScope(project, getPythonFiles(includeDepends))

    fun getOdooDependingModuleScope(): GlobalSearchScope =
        GlobalSearchScope.filesWithLibrariesScope(project, getDependingVirtualFiles())

    fun getOdooDependingPythonModuleScope(): GlobalSearchScope =
        GlobalSearchScope.filesWithLibrariesScope(project, getDependingPythonFiles())

    fun getOdooManifestModificationTracker(): ModificationTracker = OdooManifestModificationTracker[name]

    fun getOdooModuleModificationTracker(): ModificationTracker = OdooModuleModificationTracker[name]

    companion object {
        val KEY_MODULE_DESCRIPTOR = "module.descriptor"
        val KEY_FILES = "module.files"
    }
}
