package legacy;

public final class LegacyNoteRenderer
{
    public String renderRecord(String heading, String body, String section, int rank)
    {
        return String.format(
            "[LEGACY]%n%s%n%s%nРозділ: %s%nВажливість: %d",
            heading, body, section, rank
        );
    }
}
