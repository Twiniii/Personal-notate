package comparators;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import models.BaseEntity;

public final class EntityComparators
{
    private EntityComparators()
    {
    }

    public static Comparator<String> ukrainianNames()
    {
        Collator collator = Collator.getInstance(Locale.forLanguageTag("uk-UA"));
        collator.setStrength(Collator.TERTIARY);
        collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        return collator::compare;
    }

    public static Comparator<BaseEntity> byName()
    {
        return Comparator.comparing(BaseEntity::getName, ukrainianNames());
    }
}
