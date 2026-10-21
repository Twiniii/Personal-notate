package diagnostics;

public final class ConstructorTrace
{
    private static boolean enabled;

    private ConstructorTrace()
    {
    }

    public static boolean isEnabled()
    {
        return enabled;
    }

    public static void setEnabled(boolean value)
    {
        enabled = value;
    }

    public static void log(String message)
    {
        if (enabled)
        {
            System.out.println("[Конструктор] " + message);
        }
    }
}
