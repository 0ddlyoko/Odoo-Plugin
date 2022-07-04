package me.oddlyoko.odoo.models;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import me.oddlyoko.odoo.models.models.ModelDescriptor;
import me.oddlyoko.odoo.models.models.OdooModel;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class OdooModelUtil {
    public static final String NAME_KEY = "_name";
    public static final String DESCRIPTION_KEY = "_description";
    public static final String INHERIT_KEY = "_inherit";
    public static final String INHERITS_KEY = "_inherits";

    private OdooModelUtil() {}

    /**
     * Check if given {@link PyClass} is a valid odoo model
     *
     * @param pyClass {@link PyClass} to check
     * @return <i>true</i> if given {@link PyClass} is a valid model
     */
    public static boolean isInvalidOdooPyClass(@NotNull PyClass pyClass) {
        OdooModel odooPyClass = getOdooPyClass(pyClass);
        if (odooPyClass == null)
            return false;
        return odooPyClass.isInvalidOdooClass();
    }

    /**
     * Retrieves {@link OdooModel} that is the child of {@link PyClass}
     *
     * @param pyClass The {@link PyClass} which has the saved {@link OdooModel}
     * @return An {@link OdooModel} instance that is the child of {@link PyClass}, or null if not found
     */
    public static OdooModel getOdooPyClass(@NotNull PyClass pyClass) {
        if (pyClass instanceof OdooModel)
            return (OdooModel) pyClass;
        OdooModel odooPyClass = new OdooModel(pyClass.getNode());
        if (odooPyClass.isInvalidOdooClass())
            return null;
        return odooPyClass;
    }

    /**
     * Retrieves {@link ModelDescriptor} that is saved in given {@link PyClass}
     *
     * @param pyClass The {@link PyClass}
     * @return A {@link ModelDescriptor} instance that has been saved in given {@link PyClass}, or null if not found
     */
    public static ModelDescriptor getDescriptor(@NotNull PyClass pyClass) {
        return ModelDescriptor.fromPyClass(pyClass);
    }

    /**
     * Retrieves the name of the model from a given {@link PyClass}
     *
     * @param pyClass The {@link PyClass}
     * @return Name of the odoo model from given {@link PyClass}, or null
     */
    public static String getModelName(@NotNull PyClass pyClass) {
        OdooModel odooPyClass = getOdooPyClass(pyClass);
        if (odooPyClass == null)
            return null;
        ModelDescriptor modelDescriptor = odooPyClass.getModelDescriptor();
        if (modelDescriptor == null)
            return null;
        return modelDescriptor.getOdooModel();
    }

    /**
     * Retrieves classes inside given {@link VirtualFile} inside given {@link Project}
     *
     * @param file    The file
     * @param project The project
     * @return A {@link List} containing {@link PyClass} that are inside given {@link VirtualFile} of given {@link Project}
     */
    public static List<PyClass> getClasses(@NotNull VirtualFile file, @NotNull Project project) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (!(psiFile instanceof PyFile))
            return List.of();
        return ((PyFile) psiFile).getTopLevelClasses();
    }

    /**
     * Retrieves {@link OdooModel} from given list of {@link PyClass}
     *
     * @param pyClasses The list of classes
     * @return A {@link List} containing {@link OdooModel}
     */
    public static Set<OdooModel> getModels(List<PyClass> pyClasses) {
        Set<OdooModel> models = new HashSet<>();
        pyClasses.forEach(pyClass -> {
            OdooModel model = getOdooPyClass(pyClass);
            if (model != null)
                models.add(model);
        });
        return models;
    }

    /**
     * Retrieves {@link OdooModel} from given {@link VirtualFile} inside given {@link Project}
     *
     * @param file    The file
     * @param project The project
     * @return A {@link List} containing {@link OdooModel} that are inside given {@link VirtualFile} of given {@link Project}
     */
    public static Set<OdooModel> getModels(@NotNull VirtualFile file, @NotNull Project project) {
        return getModels(getClasses(file, project));
    }
}
