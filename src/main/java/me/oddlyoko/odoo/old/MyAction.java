package me.oddlyoko.odoo.old;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import me.oddlyoko.odoo.modules.OdooModuleUtil;
import me.oddlyoko.odoo.modules.models.OdooModule;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

public class MyAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        System.out.println("LOL");
        Project project = Objects.requireNonNull(e.getProject());
        //System.out.println(OdooModuleUtil.getAllModules(project));
        OdooModule odooModule = OdooModuleUtil.getModule("estilos_expense", project);
        Instant before = Instant.now();
        System.out.println(odooModule.getModels(false).size());
        Instant after = Instant.now();
        System.out.println("Time: " + (after.toEpochMilli() - before.toEpochMilli()));
        before = Instant.now();
        //List<OdooModel> models = odooModule.getModels(true);
        Collection<String> models = odooModule.getModuleDepends();
        System.out.println(models);
        System.out.println(models.size());
        after = Instant.now();
        System.out.println("Time: " + (after.toEpochMilli() - before.toEpochMilli()));
        Collection<OdooModule> models2 = odooModule.getOdooModuleDepending();
        System.out.println(models2);
        System.out.println(models2.size());
        after = Instant.now();
        System.out.println("Time: " + (after.toEpochMilli() - before.toEpochMilli()));
    }
}
