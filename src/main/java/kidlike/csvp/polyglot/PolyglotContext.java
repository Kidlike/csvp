package kidlike.csvp.polyglot;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

/**
 * <a href="https://www.graalvm.org/reference-manual/embed-languages/#computed-arrays-using-polyglot-proxies">
 *     https://www.graalvm.org/reference-manual/embed-languages/#computed-arrays-using-polyglot-proxies</a>
 */
@Singleton
public class PolyglotContext {
    private static final String LANG = "js";

    private final Context context;

    public PolyglotContext() {
        context = Context.newBuilder(LANG)
            .allowAllAccess(true)
            .build();
        context.enter();
    }

    @PreDestroy
    public void teardown() {
        context.leave();
        context.close();
    }

    public Value eval(CharSequence source) {
        return context.eval(LANG, source);
    }
}
