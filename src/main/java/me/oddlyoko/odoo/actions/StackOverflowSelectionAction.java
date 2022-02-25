package me.oddlyoko.odoo.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

public class StackOverflowSelectionAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        String selection = editor.getCaretModel().getCurrentCaret().getSelectedText();
        BrowserUtil.browse("https://stackoverflow.com/search?q=" + selection);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        System.out.println(editor.getCaretModel().getCurrentCaret().hasSelection());
        e.getPresentation().setEnabledAndVisible(editor.getCaretModel().getCurrentCaret().hasSelection());
    }
}
