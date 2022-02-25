package me.oddlyoko.odoo.listeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

public class ProjectListener implements ProjectManagerListener {
    /*
    @Override
    public void projectOpened(@NotNull Project project) {
        System.out.println("ProjectListener.projectOpened");
        if (ApplicationManager.getApplication().isUnitTestMode())
            return;
        OdooService odoo = project.getService(OdooService.class);
        odoo.loadProject();
    }

    @Override
    public void projectClosed(@NotNull Project project) {
        System.out.println("ProjectListener.projectClosed");
        if (ApplicationManager.getApplication().isUnitTestMode())
            return;
        OdooService odoo = project.getService(OdooService.class);
        odoo.unloadProject();
    }
 */
}
