package me.oddlyoko.odoo.modules.reference;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Reference Contributor for __manifest__.py file
 */
public class OdooManifestReferenceContributor extends PsiReferenceContributor {
    public static final PatternCondition<PyStringLiteralExpression> DEPENDS_CONDITION = new OdooManifestReferenceListCondition(
            List.of("depends")
    );
    public static final PsiElementPattern.Capture<? extends PsiElement> DEPENDS_PATTERN =
            PlatformPatterns.psiElement(PyStringLiteralExpression.class).with(DEPENDS_CONDITION);

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(DEPENDS_PATTERN,
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        return new PsiReference[] { new OdooModuleReference(element) };
                    }
                });
    }
}
