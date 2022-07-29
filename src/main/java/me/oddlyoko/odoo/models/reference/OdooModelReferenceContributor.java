package me.oddlyoko.odoo.models.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.PyDictLiteralExpression;
import com.jetbrains.python.psi.PyListLiteralExpression;
import com.jetbrains.python.psi.PySetLiteralExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.PyTargetExpression;
import org.jetbrains.annotations.NotNull;

public class OdooModelReferenceContributor extends PsiReferenceContributor {
    public static final PsiElementPattern.Capture<? extends PsiElement> NAME_PATTERN =
            PlatformPatterns.psiElement(PyStringLiteralExpression.class).afterSiblingSkipping(
                    PlatformPatterns.psiElement().withElementType(PyTokenTypes.EQ),
                    PlatformPatterns.psiElement(PyTargetExpression.class).withName("_name")
            );
    public static final PsiElementPattern.Capture<? extends PsiElement> INHERIT_PATTERN =
            PlatformPatterns.psiElement(PyStringLiteralExpression.class).afterSiblingSkipping(
                    PlatformPatterns.psiElement().withElementType(PyTokenTypes.EQ),
                    PlatformPatterns.psiElement(PyTargetExpression.class).withName("_inherit")
            );
    public static final PsiElementPattern.Capture<? extends PsiElement> INHERIT_DICT_PATTERN =
            PlatformPatterns.psiElement(PyStringLiteralExpression.class).withParent(
                    PlatformPatterns.psiElement(PyListLiteralExpression.class).afterSiblingSkipping(
                            PlatformPatterns.psiElement().withElementType(PyTokenTypes.EQ),
                            PlatformPatterns.psiElement(PyTargetExpression.class).withName("_inherit")
                    )
            );
    public static final PsiElementPattern.Capture<? extends PsiElement> INHERITS_PATTERN =
            PlatformPatterns.psiElement(PyStringLiteralExpression.class).withAncestor(2,
                    PlatformPatterns.or(
                            PlatformPatterns.psiElement(PyDictLiteralExpression.class).afterSiblingSkipping(
                                    PlatformPatterns.psiElement().withElementType(PyTokenTypes.EQ),
                                    PlatformPatterns.psiElement(PyTargetExpression.class).withName("_inherits")
                            ),
                            PlatformPatterns.psiElement(PySetLiteralExpression.class).afterSiblingSkipping(
                                    PlatformPatterns.psiElement().withElementType(PyTokenTypes.EQ),
                                    PlatformPatterns.psiElement(PyTargetExpression.class).withName("_inherits")
                            )
                    )
            );

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        PsiReferenceProvider provider = new PsiReferenceProvider() {
            @NotNull
            @Override
            public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                return new PsiReference[] { new OdooModelReference(element) };
            }
        };
        registrar.registerReferenceProvider(NAME_PATTERN, provider);
        registrar.registerReferenceProvider(INHERIT_PATTERN, provider);
        registrar.registerReferenceProvider(INHERIT_DICT_PATTERN, provider);
        registrar.registerReferenceProvider(INHERITS_PATTERN, provider);
    }
}
