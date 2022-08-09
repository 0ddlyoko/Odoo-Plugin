package me.oddlyoko.odoo.modules

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Consumer
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.python.PythonFileType

object OdooManifestFilter: FileBasedIndex.FileTypeSpecificInputFilter {

    override fun registerFileTypesUsedForIndexing(fileTypeSink: Consumer<in FileType>) {
        fileTypeSink.consume(PythonFileType.INSTANCE)
    }

    override fun acceptInput(file: VirtualFile): Boolean = OdooModuleUtil.isValidManifest(file)
}
