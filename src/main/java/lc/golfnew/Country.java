/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lc.golfnew;

/**
 *
 * @author collet
 */
class Country
{
  private final String iso;
  private final String code;
  public String  name;
  public String  language;

Country(String iso, String code, String name, String language)
  {
    this.iso = iso;
    this.code = code;
    this.name = name;
    this.language = language;
  }

  @Override
  public String toString()
  {
    return iso + " - " + code + " - " + name.toUpperCase() + " - " + language;
    //return code + " - " + name.toUpperCase();
  }

} //end class country
