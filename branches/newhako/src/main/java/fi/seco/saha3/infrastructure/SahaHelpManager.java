package fi.seco.saha3.infrastructure;

/**
 * Controls what is shown in the mouseover help in the right upper corner
 * of every SAHA UI page. The page-specific help texts are not implemented.
 *
 */
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
