package me.oddlyoko.odoo;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import me.oddlyoko.odoo.services.OdooService;
import org.jetbrains.annotations.NotNull;

public class ProjectStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        System.out.println("ProjectStartupActivity.runActivity");
        if (ApplicationManager.getApplication().isUnitTestMode())
            return;
        OdooService odoo = project.getService(OdooService.class);
        odoo.loadProject();
    }
}
