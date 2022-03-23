package es.upm.babel.sequenceTester;

import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


class Texts {
  private static Locale locale;
  private static ResourceBundle bundle;

  static void setLocale(Locale locale) {
    if ((Texts.locale == null) || !Texts.locale.equals(locale)) {
      Texts.locale = locale;
      if (!Arrays.asList(Locale.getAvailableLocales()).contains(locale)) {
        // Which locale to use for this message? :-)
        throw new RuntimeException("Locale "+locale+" is not available");
      }
      try {
        bundle = ResourceBundle.getBundle("texts", locale);
      } catch (MissingResourceException exc) {
        throw new RuntimeException("Texts for locale "+locale+" are not available");
      }
    }
  }

  static String getText(String key) {
    if (locale == null)
      throw new RuntimeException("No locale defined");
    try { return bundle.getString(key); }
    catch (MissingResourceException exc) {
      System.out.println("*** WARNING. Inform teachers: Key "+key+" does not exist in bundle for locale "+locale);
      return key;
    }
  }

  static String getText(String key, String format) {
    String text = getText(key);
    if (format.contains("C")) text = capitalizeFirstLetter(text);
    if (format.contains("P")) text = " "+text;
    if (format.contains("S")) text += " ";
    return text;
  }

  static String capitalizeFirstLetter(String str) {
    return str.length() == 0 ? str
            : str.length() == 1 ? str.toUpperCase()
            : str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
  }

}
