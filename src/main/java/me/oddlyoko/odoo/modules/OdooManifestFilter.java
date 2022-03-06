package me.oddlyoko.odoo.modules;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.python.PythonFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Filter on manifest file
 */
public final class OdooManifestFilter implements FileBasedIndex.FileTypeSpecificInputFilter {
    public static final OdooManifestFilter INSTANCE = new OdooManifestFilter();

    private OdooManifestFilter() {}

    @Override
    public void registerFileTypesUsedForIndexing(@NotNull Consumer<? super FileType> fileTypeSink) {
        fileTypeSink.consume(PythonFileType.INSTANCE);
    }

    @Override
    public boolean acceptInput(@NotNull VirtualFile file) {
        return OdooModuleUtil.isValidManifest(file);
    }
}
