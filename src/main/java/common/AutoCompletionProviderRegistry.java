package common;

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AutoCompletionProviderRegistry {

    private static final Map<Automaton.MachineType, Supplier<CompletionProvider>> providerSuppliers = new HashMap<>();

    /**
     * Register a completion provider supplier for a specific automaton type.
     *
     * @param type The automaton type
     * @param providerSupplier A supplier that creates the completion provider
     */
    public static void registerProvider(Automaton.MachineType type, Supplier<CompletionProvider> providerSupplier) {
        providerSuppliers.put(type, providerSupplier);
    }

    /**
     * Get the completion provider for a specific automaton type.
     * Returns a default provider if none is registered.
     *
     * @param type The automaton type
     * @return The completion provider for that type
     */
    public static CompletionProvider getProvider(Automaton.MachineType type) {
        Supplier<CompletionProvider> supplier = providerSuppliers.get(type);
        if (supplier != null) {
            return supplier.get();
        }
        return new DefaultCompletionProvider();
    }
}