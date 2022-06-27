package me.oddlyoko.odoo;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class OdooUtil {
    private OdooUtil() {}

    /**
     * Retrieve saved data from the cache
     *
     * @param psiElement the element to retrieve the data from
     * @param supplier   the supplier that will generate the data if it doesn't exist in the cache
     * @param depends    the elements that will update the cache if they change
     * @return the data
     * @param <E> the type of the data
     */
    public static <E> E getData(PsiElement psiElement, Supplier<E> supplier, Object... depends) {
        return getData(psiElement, null, supplier, depends);
    }

    /**
     * Retrieve saved data from the cache
     *
     * @param psiElement the element to retrieve the data from
     * @param param      the key that identifies the data in the map
     * @param supplier   the supplier that will generate the data if it doesn't exist in the cache
     * @param depends    the elements that will update the cache if they change
     * @return the data
     * @param <E> the type of the data
     * @param <P> the type of the parameter
     */
    public static <E, P> E getData(PsiElement psiElement, P param, Supplier<E> supplier, Object... depends) {
        CachedValuesManager manager = CachedValuesManager.getManager(psiElement.getProject());
        return getData(psiElement, manager.getKeyForClass(depends.getClass()), param, supplier, depends);
    }

    /**
     * Retrieve saved data from the cache
     *
     * @param psiElement the element to retrieve the data from
     * @param key        the key that identifies the map that contains the data
     * @param param      the key that identifies the data in the map
     * @param supplier   the supplier that will generate the data if it doesn't exist in the cache
     * @param depends    the elements that will update the cache if they change
     * @return the data
     * @param <E> the type of the data
     * @param <P> the type of the parameter
     */
    public static <E, P> E getData(PsiElement psiElement, Key<CachedValue<Map<P, E>>> key, P param, Supplier<E> supplier, Object... depends) {
        Map<P, E> map = CachedValuesManager.getCachedValue(psiElement, key, () -> {
            Object[] objs = depends != null && depends.length > 0 ? depends : new Object[] { PsiModificationTracker.MODIFICATION_COUNT };
            return CachedValueProvider.Result.create(new HashMap<>(), objs);
        });
        E value = map.get(param);
        if (value == null) {
            value = supplier.get();
            map.put(param, value);
        }
        return value;
    }
}
