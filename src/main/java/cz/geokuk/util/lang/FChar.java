package cz.geokuk.util.lang;

/**
 * Metody nad znaky UNICODE
 * @author Martin Veverka
 * @version 1.0
 */

public final class FChar {

  private FChar() {
  }

  /**
   * Vrátí jméno Unicode znaku tak jak je definováno na stránce <a href="http://www.unicode.org/charts">http://www.unicode.org/charts</a>.
   * @param c Znak. Tato verze bere pouze znaky ASCCI, tedy spodních 128 znaků.
   * @return Vrácené jméno znaku. Jméno znaku je velkými písmeny, obsahuje mezery a někdy i pomlčky.
   */
  public static String getCharName(char c) {
    if (c < 0 || c >= 128)
      throw new UnsupportedOperationException("Operation getCharName is supported only for characters in interval <0,127>");
    return sJmenaZnaku[c];
  }

  /**
   * Vrátí jméno znaku jako identifikátor. Jméno se zjistí pomocí {@link #getCharName},
   * pak se převedou první písmena všech slov na velká, ostatní na malá a oddělovací pomlčky, mezery a jiné paznaky se vyhodí.
   * @param cc Znak, jehož jméno se má určit.
   * @return Jméno znaku vrácené metodou {@link #getCharName} upravené jako identifikátor.
   */
  public static String getCharNameAsIdentifier(char cc) {
    String jmeno = getCharName(cc);
    StringBuffer sb = new StringBuffer(jmeno.length());
    boolean pristivelky = true;
    for (int i = 0; i < jmeno.length(); i++) {
      char c = jmeno.charAt(i);
      if (c == ' ' || c == '-') { // prázdný znak, říkající, že příští písmeno bude velké
        pristivelky = true;
      } else { // písmeno
        if (pristivelky) sb.append(c); else sb.append(Character.toLowerCase(c));
        pristivelky = false;
      }
    }
    return sb.toString();
  }


  private static final String[] sJmenaZnaku = new String[] {
        "NULL", // 0x0000
      "START OF HEADING", // 0x0001
      "START OF TEXT", // 0x0002
      "END OF TEXT", // 0x0003
      "END OF TRANSMISSION", // 0x0004
      "ENQUIRY", // 0x0005
      "ACKNOWLEDGE", // 0x0006
      "BELL", // 0x0007
      "BACKSPACE", // 0x0008
      "CHARACTER TABULATION", // 0x0009
      "LINE FEED", // 0x000A
      "LINE TABULATION", // 0x000B
      "FORM FEED", // 0x000C
      "CARRIAGE RETURN", // 0x000D
      "SHIFT OUT", // 0x000E
      "SHIFT IN", // 0x000F
      "DATA LINK ESCAPE", // 0x0010
      "DEVICE CONTROL ONE", // 0x0011
      "DEVICE CONTROL TWO", // 0x0012
      "DEVICE CONTROL THREE", // 0x0013
      "DEVICE CONTROL FOUR", // 0x0014
      "NEGATIVE ACKNOWLEDGE", // 0x0015
      "SYNCHRONOUS IDLE", // 0x0016
      "END OF TRANSMISSION BLOCK", // 0x0017
      "CANCEL", // 0x0018
      "END OF MEDIUM", // 0x0019
      "SUBSTITUTE", // 0x001A
      "ESCAPE", // 0x001B
      "INFORMATION SEPARATOR FOUR", // 0x001C
      "INFORMATION SEPARATOR THREE", // 0x001D
      "INFORMATION SEPARATOR TWO", // 0x001E
      "INFORMATION SEPARATOR ONE", // 0x001F
      "SPACE", // 0x0020
      "EXCLAMATION MARK", // 0x0021
      "QUOTATION MARK", // 0x0022
      "NUMBER SIGN", // 0x0023
      "DOLLAR SIGN", // 0x0024
      "PERCENT SIGN", // 0x0025
      "AMPERSAND", // 0x0026
      "APOSTROPHE", // 0x0027
      "LEFT PARENTHESIS", // 0x0028
      "RIGHT PARENTHESIS", // 0x0029
      "ASTERISK", // 0x002A
      "PLUS SIGN", // 0x002B
      "COMMA", // 0x002C
      "HYPHEN-MINUS", // 0x002D
      "FULL STOP", // 0x002E
      "SOLIDUS", // 0x002F
      "DIGIT ZERO", // 0x0030
      "DIGIT ONE", // 0x0031
      "DIGIT TWO", // 0x0032
      "DIGIT THREE", // 0x0033
      "DIGIT FOUR", // 0x0034
      "DIGIT FIVE", // 0x0035
      "DIGIT SIX", // 0x0036
      "DIGIT SEVEN", // 0x0037
      "DIGIT EIGHT", // 0x0038
      "DIGIT NINE", // 0x0039
      "COLON", // 0x003A
      "SEMICOLON", // 0x003B
      "LESS-THAN SIGN", // 0x003C
      "EQUALS SIGN", // 0x003D
      "GREATER-THAN SIGN", // 0x003E
      "QUESTION MARK", // 0x003F
      "COMMERCIAL AT", // 0x0040
      "LATIN CAPITAL LETTER A", // 0x0041
      "LATIN CAPITAL LETTER B", // 0x0042
      "LATIN CAPITAL LETTER C", // 0x0043
      "LATIN CAPITAL LETTER D", // 0x0044
      "LATIN CAPITAL LETTER E", // 0x0045
      "LATIN CAPITAL LETTER F", // 0x0046
      "LATIN CAPITAL LETTER G", // 0x0047
      "LATIN CAPITAL LETTER H", // 0x0048
      "LATIN CAPITAL LETTER I", // 0x0049
      "LATIN CAPITAL LETTER J", // 0x004A
      "LATIN CAPITAL LETTER K", // 0x004B
      "LATIN CAPITAL LETTER L", // 0x004C
      "LATIN CAPITAL LETTER M", // 0x004D
      "LATIN CAPITAL LETTER N", // 0x004E
      "LATIN CAPITAL LETTER O", // 0x004F
      "LATIN CAPITAL LETTER P", // 0x0050
      "LATIN CAPITAL LETTER Q", // 0x0051
      "LATIN CAPITAL LETTER R", // 0x0052
      "LATIN CAPITAL LETTER S", // 0x0053
      "LATIN CAPITAL LETTER T", // 0x0054
      "LATIN CAPITAL LETTER U", // 0x0055
      "LATIN CAPITAL LETTER V", // 0x0056
      "LATIN CAPITAL LETTER W", // 0x0057
      "LATIN CAPITAL LETTER X", // 0x0058
      "LATIN CAPITAL LETTER Y", // 0x0059
      "LATIN CAPITAL LETTER Z", // 0x005A
      "LEFT SQUARE BRACKET", // 0x005B
      "REVERSE SOLIDUS", // 0x005C
      "RIGHT SQUARE BRACKET", // 0x005D
      "CIRCUMFLEX ACCENT", // 0x005E
      "LOW LINE", // 0x005F
      "GRAVE ACCENT", // 0x0060
      "LATIN SMALL LETTER A", // 0x0061
      "LATIN SMALL LETTER B", // 0x0062
      "LATIN SMALL LETTER C", // 0x0063
      "LATIN SMALL LETTER D", // 0x0064
      "LATIN SMALL LETTER E", // 0x0065
      "LATIN SMALL LETTER F", // 0x0066
      "LATIN SMALL LETTER G", // 0x0067
      "LATIN SMALL LETTER H", // 0x0068
      "LATIN SMALL LETTER I", // 0x0069
      "LATIN SMALL LETTER J", // 0x006A
      "LATIN SMALL LETTER K", // 0x006B
      "LATIN SMALL LETTER L", // 0x006C
      "LATIN SMALL LETTER M", // 0x006D
      "LATIN SMALL LETTER N", // 0x006E
      "LATIN SMALL LETTER O", // 0x006F
      "LATIN SMALL LETTER P", // 0x0070
      "LATIN SMALL LETTER Q", // 0x0071
      "LATIN SMALL LETTER R", // 0x0072
      "LATIN SMALL LETTER S", // 0x0073
      "LATIN SMALL LETTER T", // 0x0074
      "LATIN SMALL LETTER U", // 0x0075
      "LATIN SMALL LETTER V", // 0x0076
      "LATIN SMALL LETTER W", // 0x0077
      "LATIN SMALL LETTER X", // 0x0078
      "LATIN SMALL LETTER Y", // 0x0079
      "LATIN SMALL LETTER Z", // 0x007A
      "LEFT CURLY BRACKET", // 0x007B
      "VERTICAL LINE", // 0x007C
      "RIGHT CURLY BRACKET", // 0x007D
      "TILDE", // 0x007E
      "DELETE" }; // 0x007F

}