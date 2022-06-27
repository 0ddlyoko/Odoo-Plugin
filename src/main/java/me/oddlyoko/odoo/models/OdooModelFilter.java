package me.oddlyoko.odoo.models;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.python.PythonFileType;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Filter on model files
 */
public final class OdooModelFilter implements FileBasedIndex.FileTypeSpecificInputFilter {
    public static final OdooModelFilter INSTANCE = new OdooModelFilter();

    private OdooModelFilter() {}

    @Override
    public void registerFileTypesUsedForIndexing(@NotNull Consumer<? super FileType> fileTypeSink) {
        fileTypeSink.consume(PythonFileType.INSTANCE);
    }

    @Override
    public boolean acceptInput(@NotNull VirtualFile file) {
        return OdooModuleUtil.isInOdooModule(file);
    }
}
