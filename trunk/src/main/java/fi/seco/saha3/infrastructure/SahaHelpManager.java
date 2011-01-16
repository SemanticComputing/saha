package fi.seco.saha3.infrastructure;

public class SahaHelpManager
{
    public static String getHelpString(String view)
    {
        return "<h2>" + view + " help</h2><p>" + getFullHelpText(view) + "</p>";
    }

    private static String getFullHelpText(String view)
    {
        return "No help available for this page.";
    }
}
