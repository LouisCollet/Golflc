
package lc.golfnew;

import lists.*;
import java.io.Serializable;
import java.math.BigDecimal;
//import java.sql.*;
import java.util.*;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

/**
 *
 * @author collet DCI needs beans.xml under Other Sources/META-INF or /src/main/java/resources/META-INF
 */
@Named("listC")
@SessionScoped

public class ListController implements Serializable, interfaces.GolfInterface, interfaces.Log
{
private static final List<SelectItem> COUNTRIES = new ArrayList<>();

private static BigDecimal HandicapPlayer;
private static int zwanzeur;
private static int greenshirt;

public ListController()    // constructor
{
    //
}

    public BigDecimal getHandicapPlayer() {
       return HandicapPlayer;
   }

public int getGreenshirt()
{
       greenshirt = ScoreCard3List.getListe().get(0).getPlayerhasroundZwanzeursGreenshirt();
  //  LOG.debug("greenshirt listsc3 = " + greenshirt);
        return greenshirt;
}

    public static void setGreenshirt(int greenshirt) {
        ListController.greenshirt = greenshirt;
    }

public int getZwanzeur()
{
    zwanzeur = ScoreCard3List.getListe().get(0).getPlayerhasroundZwanzeursResult();
    LOG.debug("zwanzeur listsc3 = " + zwanzeur);
        return zwanzeur;
  //  }
}

    public static void setZwanzeur(int zwanzeur)
    {
        ListController.zwanzeur = zwanzeur;
    }

    
/*
     *
     * @param player.getIdplayer()
     * @param in_course
     * @return
     * @throws SQLException
     */
/*
public List<SelectItem> getCountries()
{
if (COUNTRIES.isEmpty())
        {
            //COUNTRIES = CountriesController.getCountries() ;
  //first field = itemValue, stockée dans DB
 COUNTRIES.add(new SelectItem("AF", "Afghanistan"));
 COUNTRIES.add(new SelectItem("AL", "Albania"));
 COUNTRIES.add(new SelectItem("DZ", "Algeria"));
 COUNTRIES.add(new SelectItem("AS", "American Samoa"));
 COUNTRIES.add(new SelectItem("AD", "Andorra"));
 COUNTRIES.add(new SelectItem("AO", "Angola"));
 COUNTRIES.add(new SelectItem("AI", "Anguilla "));
 COUNTRIES.add(new SelectItem("AQ", "Antarctica"));
 COUNTRIES.add(new SelectItem("AG", "Antigua and Barbuda"));
 COUNTRIES.add(new SelectItem("AR", "Argentina"));
 COUNTRIES.add(new SelectItem("AM", "Armenia"));
 COUNTRIES.add(new SelectItem("AW", "Aruba"));
 COUNTRIES.add(new SelectItem("AU", "Australia"));
 COUNTRIES.add(new SelectItem("AT", "Austria"));
 COUNTRIES.add(new SelectItem("AZ", "Azerbaijan"));
 COUNTRIES.add(new SelectItem("BS", "Bahamas"));
 COUNTRIES.add(new SelectItem("BH", "Bahrain"));
 COUNTRIES.add(new SelectItem("BD", "Bangladesh"));
 COUNTRIES.add(new SelectItem("BB", "Barbados"));
 COUNTRIES.add(new SelectItem("BY", "Belarus"));
 COUNTRIES.add(new SelectItem("BE", "Belgium"));
 COUNTRIES.add(new SelectItem("BZ", "Belize"));
 COUNTRIES.add(new SelectItem("BJ", "Benin"));
 COUNTRIES.add(new SelectItem("BM", "Bermuda"));
 COUNTRIES.add(new SelectItem("BT", "Bhutan"));
 COUNTRIES.add(new SelectItem("BO", "Bolivia, Plurinational State Of'  "));
 COUNTRIES.add(new SelectItem("BQ", "Bonaire, Saint Eustatius and Saba'"));
 COUNTRIES.add(new SelectItem("BA", "Bosnia and Herzegovina"));
 COUNTRIES.add(new SelectItem("BW", "Botswana"));
 COUNTRIES.add(new SelectItem("BV", "Bouvet Island"));
 COUNTRIES.add(new SelectItem("BR", "Brazil"));
 COUNTRIES.add(new SelectItem("IO", "British Indian Ocean Territory"));
 COUNTRIES.add(new SelectItem("BN", "Brunei Darussalam"));
 COUNTRIES.add(new SelectItem("BG", "Bulgaria"));
 COUNTRIES.add(new SelectItem("BF", "Burkina Faso"));
 COUNTRIES.add(new SelectItem("BI", "Burundi"));
 COUNTRIES.add(new SelectItem("KH", "Cambodia"));
 COUNTRIES.add(new SelectItem("CM", "Cameroon"));
 COUNTRIES.add(new SelectItem("CA", "Canada"));
 COUNTRIES.add(new SelectItem("CV", "Cape Verde  "));
 COUNTRIES.add(new SelectItem("KY", "Cayman Islands"));
 COUNTRIES.add(new SelectItem("CF", "Central African Republic"));
 COUNTRIES.add(new SelectItem("TD", "Chad"));
 COUNTRIES.add(new SelectItem("CL", "Chile"));
 COUNTRIES.add(new SelectItem("CN", "China"));
 COUNTRIES.add(new SelectItem("CX", "Christmas Island"));
 COUNTRIES.add(new SelectItem("CC", "Cocos (Keeling)) Islands"));
 COUNTRIES.add(new SelectItem("CO", "Colombia"));
 COUNTRIES.add(new SelectItem("KM", "Comoros"));
 COUNTRIES.add(new SelectItem("CG", "Congo"));
 COUNTRIES.add(new SelectItem("CD", "Congo, The Democratic Republic Of The"));
 COUNTRIES.add(new SelectItem("CK", "Cook Islands"));
 COUNTRIES.add(new SelectItem("CR", "Costa Rica"));
 COUNTRIES.add(new SelectItem("HR", "Croatia"));
 COUNTRIES.add(new SelectItem("CU", "Cuba"));
 COUNTRIES.add(new SelectItem("CW", "Curaçao"));
 COUNTRIES.add(new SelectItem("CY", "Cyprus"));
 COUNTRIES.add(new SelectItem("CZ", "Czech Republic"));
 COUNTRIES.add(new SelectItem("CI", "Côte d'Ivoire"));
 COUNTRIES.add(new SelectItem("DK", "Denmark"));
 COUNTRIES.add(new SelectItem("DJ", "Djibouti"));
 COUNTRIES.add(new SelectItem("DM", "Dominica"));
 COUNTRIES.add(new SelectItem("DO", "Dominican Republic"));
 COUNTRIES.add(new SelectItem("EC", "Ecuador"));
 COUNTRIES.add(new SelectItem("EG", "Egypt"));
 COUNTRIES.add(new SelectItem("SV", "El Salvador"));
 COUNTRIES.add(new SelectItem("GQ", "Equatorial Guinea"));
 COUNTRIES.add(new SelectItem("ER", "Eritrea"));
 COUNTRIES.add(new SelectItem("EE", "Estonia"));
 COUNTRIES.add(new SelectItem("ET", "Ethiopia"));
 COUNTRIES.add(new SelectItem("FK", "Falkland Islands  (Malvinas))"));
 COUNTRIES.add(new SelectItem("FO", "Faroe Islands"));
 COUNTRIES.add(new SelectItem("FJ", "Fiji"));
 COUNTRIES.add(new SelectItem("FI", "Finland"));
 COUNTRIES.add(new SelectItem("FR", "France"));
 COUNTRIES.add(new SelectItem("GF", "French Guiana"));
 COUNTRIES.add(new SelectItem("PF", "French Polynesia"));
 COUNTRIES.add(new SelectItem("TF", "French Southern Territories"));
 COUNTRIES.add(new SelectItem("GA", "Gabon"));
 COUNTRIES.add(new SelectItem("GM", "Gambia"));
 COUNTRIES.add(new SelectItem("GE", "Georgia"));
 COUNTRIES.add(new SelectItem("DE", "Germany"));
 COUNTRIES.add(new SelectItem("GH", "Ghana "));
 COUNTRIES.add(new SelectItem("GI", "Gibraltar"));
 COUNTRIES.add(new SelectItem("GR", "Greece"));
 COUNTRIES.add(new SelectItem("GL", "Greenland"));
 COUNTRIES.add(new SelectItem("GD", "Grenada"));
 COUNTRIES.add(new SelectItem("GP", "Guadeloupe"));
 COUNTRIES.add(new SelectItem("GU", "Guam"));
 COUNTRIES.add(new SelectItem("GT", "Guatemala"));
 COUNTRIES.add(new SelectItem("GG", "Guernsey"));
 COUNTRIES.add(new SelectItem("GN", "Guinea"));
 COUNTRIES.add(new SelectItem("GW", "Guinea-Bissau"));
 COUNTRIES.add(new SelectItem("GY", "Guyana"));
 COUNTRIES.add(new SelectItem("HT", "Haiti"));
 COUNTRIES.add(new SelectItem("HM", "Heard and McDonald Islands"));
 COUNTRIES.add(new SelectItem("VA", "Holy See (Vatican City State))"));
 COUNTRIES.add(new SelectItem("HN", "Honduras"));
 COUNTRIES.add(new SelectItem("HK", "Hong Kong"));
 COUNTRIES.add(new SelectItem("HU", "Hungary"));
 COUNTRIES.add(new SelectItem("IS", "Iceland"));
 COUNTRIES.add(new SelectItem("IN", "India"));
 COUNTRIES.add(new SelectItem("ID", "Indonesia"));
 COUNTRIES.add(new SelectItem("IR", "'Iran, Islamic Republic Of'"));
 COUNTRIES.add(new SelectItem("IQ", "Iraq"));
 COUNTRIES.add(new SelectItem("IE", "Ireland"));
 COUNTRIES.add(new SelectItem("IM", "Isle of Man"));
 COUNTRIES.add(new SelectItem("IL", "Israel"));
 COUNTRIES.add(new SelectItem("IT", "Italy"));
 COUNTRIES.add(new SelectItem("JM", "Jamaica"));
 COUNTRIES.add(new SelectItem("JP", "Japan"));
 COUNTRIES.add(new SelectItem("JE", "Jersey"));
 COUNTRIES.add(new SelectItem("JO", "Jordan"));
 COUNTRIES.add(new SelectItem("KZ", "Kazakhstan"));
 COUNTRIES.add(new SelectItem("KE", "Kenya"));
 COUNTRIES.add(new SelectItem("KI", "Kiribati"));
 COUNTRIES.add(new SelectItem("KP", "Korea, Democratic People\'s Republic Of"));
 COUNTRIES.add(new SelectItem("KR", "Korea, Republic of"));
 COUNTRIES.add(new SelectItem("KW", "Kuwait"));
 COUNTRIES.add(new SelectItem("KG", "Kyrgyzstan"));
 COUNTRIES.add(new SelectItem("LA", "Lao People's Democratic Republic"));
 COUNTRIES.add(new SelectItem("LV", "Latvia"));
 COUNTRIES.add(new SelectItem("LB", "Lebanon"));
 COUNTRIES.add(new SelectItem("LS", "Lesotho"));
 COUNTRIES.add(new SelectItem("LR", "Liberia"));
 COUNTRIES.add(new SelectItem("LY", "Libyan Arab Jamahiriya"));
 COUNTRIES.add(new SelectItem("LI", "Liechtenstein"));
 COUNTRIES.add(new SelectItem("LT", "Lithuania"));
 COUNTRIES.add(new SelectItem("LU", "Luxembourg"));
 COUNTRIES.add(new SelectItem("MO", "Macao"));
 COUNTRIES.add(new SelectItem("MK", "Macedonia, the Former Yugoslav Republic Of"));
 COUNTRIES.add(new SelectItem("MG", "Madagascar"));
 COUNTRIES.add(new SelectItem("MW", "Malawi"));
 COUNTRIES.add(new SelectItem("MY", "Malaysia"));
 COUNTRIES.add(new SelectItem("MV", "Maldives"));
 COUNTRIES.add(new SelectItem("ML", "Mali"));
 COUNTRIES.add(new SelectItem("MT", "Malta"));
 COUNTRIES.add(new SelectItem("MH", "Marshall Islands"));
 COUNTRIES.add(new SelectItem("MQ", "Martinique"));
 COUNTRIES.add(new SelectItem("MR", "Mauritania"));
 COUNTRIES.add(new SelectItem("MU", "Mauritius"));
 COUNTRIES.add(new SelectItem("YT", "Mayotte"));
 COUNTRIES.add(new SelectItem("MX", "Mexico"));
 COUNTRIES.add(new SelectItem("FM", "Micronesia, Federated States Of"));
 COUNTRIES.add(new SelectItem("MD", "Moldova, Republic of"));
 COUNTRIES.add(new SelectItem("MC", "Monaco"));
 COUNTRIES.add(new SelectItem("MN", "Mongolia"));
 COUNTRIES.add(new SelectItem("ME", "Montenegro"));
 COUNTRIES.add(new SelectItem("MS", "Montserrat"));
 COUNTRIES.add(new SelectItem("MA", "Morocco"));
 COUNTRIES.add(new SelectItem("MZ", "Mozambique"));
 COUNTRIES.add(new SelectItem("MM", "Myanmar"));
 COUNTRIES.add(new SelectItem("NA", "Namibia"));
 COUNTRIES.add(new SelectItem("NR", "Nauru"));
 COUNTRIES.add(new SelectItem("NP", "Nepal"));
 COUNTRIES.add(new SelectItem("NL", "Netherlands"));
 COUNTRIES.add(new SelectItem("AN", "Netherlands Antilles"));
 COUNTRIES.add(new SelectItem("NC", "New Caledonia"));
 COUNTRIES.add(new SelectItem("NZ", "New Zealand"));
 COUNTRIES.add(new SelectItem("NI", "Nicaragua"));
 COUNTRIES.add(new SelectItem("NE", "Niger"));
 COUNTRIES.add(new SelectItem("NG", "Nigeria"));
 COUNTRIES.add(new SelectItem("NU", "Niue"));
 COUNTRIES.add(new SelectItem("NF", "Norfolk Island"));
 COUNTRIES.add(new SelectItem("MP", "Northern Mariana Islands"));
 COUNTRIES.add(new SelectItem("NO", "Norway"));
 COUNTRIES.add(new SelectItem("OM", "Oman"));
 COUNTRIES.add(new SelectItem("PK", "Pakistan"));
 COUNTRIES.add(new SelectItem("PW", "Palau"));
 COUNTRIES.add(new SelectItem("PS", "Palestinian Territory, Occupied"));
 COUNTRIES.add(new SelectItem("PA", "Panama"));
 COUNTRIES.add(new SelectItem("PG", "Papua New Guinea"));
 COUNTRIES.add(new SelectItem("PY", "Paraguay"));
 COUNTRIES.add(new SelectItem("PE", "Peru"));
 COUNTRIES.add(new SelectItem("PH", "Philippines"));
 COUNTRIES.add(new SelectItem("PN", "Pitcairn"));
 COUNTRIES.add(new SelectItem("PL", "Poland"));
 COUNTRIES.add(new SelectItem("PT", "Portugal"));
 COUNTRIES.add(new SelectItem("PR", "Puerto Rico"));
 COUNTRIES.add(new SelectItem("QA", "Qatar"));
 COUNTRIES.add(new SelectItem("RO", "Romania"));
 COUNTRIES.add(new SelectItem("RU", "Russian Federation"));
 COUNTRIES.add(new SelectItem("RW", "Rwanda"));
 COUNTRIES.add(new SelectItem("RE", "Réunion"));
 COUNTRIES.add(new SelectItem("BL", "Saint Barthélemy"));
 COUNTRIES.add(new SelectItem("SH", "Saint Helena, Ascension and Tristan Da Cunha"));
 COUNTRIES.add(new SelectItem("KN", "Saint Kitts And Nevis"));
 COUNTRIES.add(new SelectItem("LC", "Saint Lucia"));
 COUNTRIES.add(new SelectItem("MF", "Saint Martin"));
 COUNTRIES.add(new SelectItem("PM", "Saint Pierre Et Miquelon"));
 COUNTRIES.add(new SelectItem("VC", "Saint Vincent And The Grenedines"));
 COUNTRIES.add(new SelectItem("WS", "Samoa"));
 COUNTRIES.add(new SelectItem("SM", "San Marino"));
 COUNTRIES.add(new SelectItem("ST", "Sao Tome and Principe"));
 COUNTRIES.add(new SelectItem("SA", "Saudi Arabia"));
 COUNTRIES.add(new SelectItem("SN", "Senegal"));
 COUNTRIES.add(new SelectItem("RS", "Serbia"));
 COUNTRIES.add(new SelectItem("SC", "Seychelles"));
 COUNTRIES.add(new SelectItem("SL", "Sierra Leone"));
 COUNTRIES.add(new SelectItem("SG", "Singapore"));
 COUNTRIES.add(new SelectItem("SX", "Sint Maarten (Dutch part))"));
 COUNTRIES.add(new SelectItem("SK", "Slovakia"));
 COUNTRIES.add(new SelectItem("SI", "Slovenia"));
 COUNTRIES.add(new SelectItem("SB", "Solomon Islands"));
 COUNTRIES.add(new SelectItem("SO", "Somalia"));
 COUNTRIES.add(new SelectItem("ZA", "South Africa"));
 COUNTRIES.add(new SelectItem("GS", "South Georgia and the South Sandwich Islands"));
 COUNTRIES.add(new SelectItem("ES", "Spain"));
 COUNTRIES.add(new SelectItem("LK", "Sri Lanka"));
 COUNTRIES.add(new SelectItem("SD", "Sudan"));
 COUNTRIES.add(new SelectItem("SR", "Suriname"));
 COUNTRIES.add(new SelectItem("SJ", "Svalbard And Jan Mayen"));
 COUNTRIES.add(new SelectItem("SZ", "Swaziland"));
 COUNTRIES.add(new SelectItem("SE", "Sweden"));
 COUNTRIES.add(new SelectItem("CH", "Switzerland"));
 COUNTRIES.add(new SelectItem("SY", "Syrian Arab Republic  "));
 COUNTRIES.add(new SelectItem("TW", "Taiwan, Province Of China"));
 COUNTRIES.add(new SelectItem("TJ", "Tajikistan"));
 COUNTRIES.add(new SelectItem("TZ", "'Tanzania, United Republic of'"));
 COUNTRIES.add(new SelectItem("TH", "Thailand"));
 COUNTRIES.add(new SelectItem("TL", "Timor-Leste"));
 COUNTRIES.add(new SelectItem("TG", "Togo"));
 COUNTRIES.add(new SelectItem("TK", "Tokelau"));
 COUNTRIES.add(new SelectItem("TO", "Tonga"));
 COUNTRIES.add(new SelectItem("TT", "Trinidad and Tobago"));
 COUNTRIES.add(new SelectItem("TN", "Tunisia"));
 COUNTRIES.add(new SelectItem("TR", "Turkey"));
 COUNTRIES.add(new SelectItem("TM", "Turkmenistan"));
 COUNTRIES.add(new SelectItem("TC", "Turks and Caicos Islands"));
 COUNTRIES.add(new SelectItem("TV", "Tuvalu"));
 COUNTRIES.add(new SelectItem("UG", "Uganda"));
 COUNTRIES.add(new SelectItem("UA", "Ukraine"));
 COUNTRIES.add(new SelectItem("AE", "United Arab Emirates"));
 COUNTRIES.add(new SelectItem("GB", "United Kingdom"));
 COUNTRIES.add(new SelectItem("US", "United States"));
 COUNTRIES.add(new SelectItem("UM", "United States Minor Outlying Islands"));
 COUNTRIES.add(new SelectItem("UY", "Uruguay"));
 COUNTRIES.add(new SelectItem("UZ", "Uzbekistan"));
 COUNTRIES.add(new SelectItem("VU", "Vanuatu"));
 COUNTRIES.add(new SelectItem("VE", "'Venezuela, Bolivarian Republic of' "));
 COUNTRIES.add(new SelectItem("VN", "Viet Nam"));
 COUNTRIES.add(new SelectItem("VG", "Virgin Islands, British"));
 COUNTRIES.add(new SelectItem("VI", "Virgin Islands, U.S."));
 COUNTRIES.add(new SelectItem("WF", "Wallis and Futuna"));
 COUNTRIES.add(new SelectItem("EH", "Western Sahara"));
 COUNTRIES.add(new SelectItem("YE", "Yemen"));
 COUNTRIES.add(new SelectItem("ZM", "Zambia"));
 COUNTRIES.add(new SelectItem("ZW", "Zimbabwe"));
 COUNTRIES.add(new SelectItem("AX", "Åland Islands"));
 }

       // LOG.info("List GAMES  = " );
       // for(SelectItem d:GAMES)
       // {LOG.info("game = " + d);
       // }
return COUNTRIES;
}
 */
 
public void main(String args[]) // throws InstantiationException, SQLException, ClassNotFoundException, IllegalAccessException
{

LOG.info(" -- main terminated" );

    } // end main
} //end class
