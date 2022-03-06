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

import java.util.ArrayList;
import java.util.List;

public final class OdooModelUtil {

    private OdooModelUtil() {}

    /**
     * Check if given {@link PyClass} is a valid odoo model
     *
     * @param pyClass {@link PyClass} to check
     * @return <i>true</i> if given {@link PyClass} is a valid model
     */
    public static boolean isValidModel(@NotNull PyClass pyClass) {
        OdooModel model = getModel(pyClass);
        if (model == null)
            return false;
        return model.isValid();
    }

    /**
     * Retrieves {@link OdooModel} that is saved in given {@link PyClass}
     *
     * @param pyClass The {@link PyClass} which has the saved {@link OdooModel}
     * @return An {@link OdooModel} instance that has been saved in given {@link PyClass}, or null if not found
     */
    public static OdooModel getModel(@NotNull PyClass pyClass) {
        return OdooModel.fromPyClass(((PyClass) pyClass.getOriginalElement()));
    }

    /**
     * Retrieves {@link ModelDescriptor} that is saved in given {@link PyClass}
     *
     * @param pyClass The {@link PyClass} which has the saved {@link OdooModel}
     * @return A {@link ModelDescriptor} instance that has been saved in given {@link PyClass}, or null if not found
     */
    public static ModelDescriptor getDescriptor(@NotNull PyClass pyClass) {
        return ModelDescriptor.fromPyClass(((PyClass) pyClass.getOriginalElement()));
    }

    /**
     * Retrieves the name of the model from a given {@link PyClass}
     *
     * @param pyClass The {@link PyClass}
     * @return Name of the odoo model from given {@link PyClass}, or null
     */
    public static String getModelName(@NotNull PyClass pyClass) {
        OdooModel model = getModel(pyClass);
        if (model == null)
            return null;
        ModelDescriptor modelDescriptor = model.getModelDescriptor();
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
     * Retrieves {@link OdooModel OdooModels} from given list of {@link PyClass}
     *
     * @param pyClasses The list of classes
     * @return A {@link List} containing {@link OdooModel}
     */
    public static List<OdooModel> getModels(List<PyClass> pyClasses) {
        List<OdooModel> models = new ArrayList<>();
        pyClasses.forEach(pyClass -> {
            OdooModel model = getModel(pyClass);
            if (model != null)
                models.add(model);
        });
        return models;
    }

    /**
     * Retrieves {@link OdooModel OdooModels} from given {@link VirtualFile} inside given {@link Project}
     *
     * @param file    The file
     * @param project The project
     * @return A {@link List} containing {@link OdooModel} that are inside given {@link VirtualFile} of given {@link Project}
     */
    public static List<OdooModel> getModels(@NotNull VirtualFile file, @NotNull Project project) {
        return getModels(getClasses(file, project));
    }
}
