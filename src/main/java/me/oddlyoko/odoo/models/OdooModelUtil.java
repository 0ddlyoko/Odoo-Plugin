package me.oddlyoko.odoo.models;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import me.oddlyoko.odoo.models.indexes.OdooModelIndex;
import me.oddlyoko.odoo.models.models.ModelDescriptor;
import me.oddlyoko.odoo.models.models.OdooModel;
import me.oddlyoko.odoo.modules.models.OdooModule;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
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
        return modelDescriptor.odooModel();
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
        if (!(psiFile instanceof PyFile pyFile))
            return List.of();
        return pyFile.getTopLevelClasses();
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

    /**
     * Retrieves all models for given project
     *
     * @param project The project
     * @return A {@link Collection} of string containing all models defined in this project
     */
    public static Collection<String> getAllModels(@NotNull Project project) {
        return OdooModelIndex.getAllModels(project);
    }

    /**
     * Retrieves all available models for given project with given scope
     *
     * @param project The project
     * @param scope   The scope
     * @return A {@link Collection} of String containing all models defined in this project with given scope
     */
    public static Collection<String> getAllModels(@NotNull Project project, @NotNull GlobalSearchScope scope) {
        return OdooModelIndex.getAllModels(project, scope);
    }

    /**
     * Retrieves all available models for given project with given {@link OdooModule}
     *
     * @param project    The project
     * @param odooModule The {@link OdooModule}
     * @return A {@link Collection} of String containing all models defined in this project with given {@link OdooModule}
     */
    public static Collection<String> getAllModels(@NotNull Project project, @NotNull OdooModule odooModule) {
        return OdooModelIndex.getAllModels(project, odooModule.getOdooPythonModuleScope(true));
    }

    /**
     * Retrieves all {@link PyClass} instance from given id for given {@link Project}
     *
     * @param odooModel The id of the model
     * @param project   The project
     * @return A {@link List} containing all {@link PyClass} instance of given id for given {@link Project}
     */
    public static List<PyClass> getModelsByName(@NotNull String odooModel, @NotNull Project project) {
        return OdooModelIndex.getModelsByName(odooModel, project);
    }

    /**
     * Retrieves all {@link PyClass} instance from given id for given {@link Project} and scope
     *
     * @param odooModel The id of the project
     * @param project   The project
     * @param scope     The scope
     * @return A {@link List} containing all {@link PyClass} instance of given id for given {@link Project}
     */
    public static List<PyClass> getModelsByName(@NotNull String odooModel, @NotNull Project project, GlobalSearchScope scope) {
        return OdooModelIndex.getModelsByName(odooModel, project, scope);
    }

    public static List<PyClass> getModelsByName(@NotNull String odooModel, @NotNull Project project, OdooModule odooModule) {
        return OdooModelIndex.getModelsByName(odooModel, project, odooModule.getOdooPythonModuleScope(true));
    }
}
