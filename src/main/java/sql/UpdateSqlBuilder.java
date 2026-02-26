package sql;

import static interfaces.Log.LOG;
import java.util.List;
import java.util.Locale;

public final class UpdateSqlBuilder {

    private UpdateSqlBuilder() {}

    // 🔒 modèle immuable unique
    public record ColumnMeta(String name, boolean updatable) {}

    // 🔁 résultat riche
    public record SetClause(String sql, int columnCount) {}

    public static SetClause build(List<ColumnMeta> columns) {
        LOG.debug("entering build ");
        var parts = columns.stream()
                .filter(ColumnMeta::updatable)
                .map(c -> c.name().toLowerCase(Locale.ROOT) + " = ?")
                .toList();

        return new SetClause(
                String.join(", ", parts),
                parts.size()
        );
    }
}
