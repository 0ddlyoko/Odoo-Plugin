package me.oddlyoko.odoo.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyExpressionStatement;
import me.oddlyoko.odoo.models.ModuleDescriptor;
import org.jetbrains.annotations.NotNull;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public final class OdooService {
    public static final String[] MANIFEST_FILES = new String[] {
            "__manifest__.py",
            "__openerp__.py",
    };
    private final Project project;
    private final PyObject manifestToJsonMethod;

    public OdooService(Project project) {
        System.out.println("OdooService.OdooService");
        this.project = project;
        PythonInterpreter pythonInterpreter = new PythonInterpreter();
        pythonInterpreter.exec("import json; import ast");
        manifestToJsonMethod = pythonInterpreter.eval("lambda d: json.dumps(ast.literal_eval(d))");
    }

    public void loadProject() {
        System.out.println("OdooService.loadProject");
        String projectName = project.getName();
        System.out.println("projectName = " + projectName);
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        VirtualFile[] vFiles = projectRootManager.getContentRoots();
        System.out.println("Size = " + vFiles.length);
        System.out.println(Arrays.stream(vFiles).map(VirtualFile::getUrl).collect(Collectors.joining("\n")));
        new Task.Backgroundable(project, "Load project in background") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                loadProjectBackground(indicator);
            }
        }.queue();
    }

    public void loadProjectBackground(ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        indicator.setText("Reading Odoo modules, please wait");
        System.out.println("OdooService.loadProjectBackground");
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiFile[] manifestFiles;
            for (String manifestName : MANIFEST_FILES) {
                PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, manifestName, GlobalSearchScope.projectScope(project));
                for (PsiFile psiFile : psiFiles)
                    loadModuleFromFile(psiFile, indicator);
            }
        });
    }

    private void loadModuleFromFile(PsiFile psiFile, ProgressIndicator indicator) {
        if (psiFile.isDirectory())
            return;
        PsiDirectory parentDirectory = psiFile.getParent();
        if (parentDirectory == null) {
            System.out.println("Parent directory of " + psiFile.getName() + " is empty !");
            return;
        }
        indicator.setText2("Loading module " + parentDirectory);
        System.out.println("Loading module " + parentDirectory);
        String odooModuleName = parentDirectory.getName();
        PyExpressionStatement exp = PsiTreeUtil.findChildOfType(psiFile, PyExpressionStatement.class);
        if (exp == null) {
            System.out.println("Cannot find PyExpressionStatement in " + odooModuleName);
            return;
        }
        String txt = exp.getText();
        //System.out.println(txt);
        if (odooModuleName.equals("l10n_do")) {
            System.out.println(txt);
            String manifestJson = manifestToJson(txt);
            ModuleDescriptor descriptor = ModuleDescriptor.fromJson(manifestJson);
            System.out.println(descriptor);
        }
    }

    private String manifestToJson(String manifest) {
        PyObject result = manifestToJsonMethod.__call__(new PyString(manifest));
        return (String) result.__tojava__(String.class);
    }

    public void unloadProject() {
        System.out.println("OdooService.unloadProject");
    }

    public Project getProject() {
        return project;
    }
}
