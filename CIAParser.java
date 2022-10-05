import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CIAParser {
    private String baseURL;
    private Document currentDoc;
    private String ciaBaseURL;

    public CIAParser() {
        this.baseURL = "https://www.cia.gov/the-world-factbook/field/map-references/";

        this.ciaBaseURL = "https://www.cia.gov";
        try {
            this.currentDoc = Jsoup.connect(this.baseURL).get();
        } catch (IOException e) {
        }
    }

    /**
     *
     *
     * @param color1 String of the first desired color
     * @param color2 String of the second desired color
     * @return A Hashmap <String of the country's name, Whether it contains the colors>
     */
    public ArrayList<String> getFlagColors(String color1, String color2) {
        Map<String, Boolean> flagMap = new HashMap<String, Boolean>();
        // key = name of country
        // value = whether the country contains specified colors
        String flagURLs = "https://www.cia.gov/the-world-factbook/field/map-references/";

        try {
            this.currentDoc = Jsoup.connect(flagURLs).get();

            for (Element e : this.currentDoc.select("h2.h3 > a")) {
                String hrefLink = e.attr("href"); //This is the href that contains the page for a country
                // Ex: /the-world-factbook/countries/afghanistan
                String fullLink = "https://www.cia.gov" + hrefLink;
                String countryName = hrefLink.substring(hrefLink.lastIndexOf("/") + 1);

                //Now to connect to the new country page!
                try {
                    this.currentDoc = Jsoup.connect(fullLink).get();
                    for (Element countryElement : this.currentDoc.select("h3.mt30 > a[href*=flag]")) {

                        Element flagElement = countryElement.parent().nextElementSibling(); //This is the <p> containing
                        // the actual flag description
                        String flagDescription = flagElement.text();

                        // Now to search through the description and look for color keywords!
                        boolean foundColor1 = false;
                        boolean foundColor2 = false;
                        String[] UKList = {"red", "blue", "white"}; // UKList - a list of the colors associated with
                        // the UK flag since some descriptions just say
                        // "contains flag of UK" , such as Akritiri
                        if (flagDescription.contains("UK")) {
                            for (int i = 0; i < 3; i++) {
                                if (color1.equals(UKList[i]))
                                    foundColor1 = true;
                            }
                            for (int i = 0; i < 3; i++) {
                                if (color2.equals(UKList[i]))
                                    foundColor2 = true;
                            }
                        }
                        if (flagDescription.contains(color1)) {
                            foundColor1 = true;
                        }
                        if (flagDescription.contains(color2)) {
                            foundColor2 = true;
                        }
                        flagMap.put(countryName, foundColor1 && foundColor2);
                    }


                } catch (IOException exception) {
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        ArrayList<String> finalCountries = new ArrayList<>();

        for (Map.Entry<String, Boolean> e:
             flagMap.entrySet()) {
            if(e.getValue()) {
                finalCountries.add(e.getKey());
            }
        }

        return finalCountries;
    }

    /**
     *
     *
     * @param OceanName String of a the name of an ocean, given as NAME_OCEAN ex: "Atlantic Ocean"
     * @return String the lowest point of an ocean
     */
    public String getLowestOceanPoint(String OceanName) {
        String lowestPoint = null;
        String oceanURL = "https://www.cia.gov/the-world-factbook/oceans/";
        try {
            this.currentDoc = Jsoup.connect(oceanURL).get();
            //Gets links for all oceans
            for (Element e : this.currentDoc.select("h5 > a")) {
                String hrefLink = e.attr("href");
                String fullLink = "https://www.cia.gov" + hrefLink;
                try {
                    this.currentDoc = Jsoup.connect(fullLink).get();
                    for (Element oceanElement : this.currentDoc.select("p:contains(lowest point)")) {
                        Pattern pattern = Pattern.compile("lowest point: </strong>(.*?)<br>");
                        Matcher matcher = pattern.matcher(oceanElement.html());
                        if (matcher.find()) {
                           lowestPoint = matcher.group(1);
                        }
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return lowestPoint;
    }


    /**
     *
     * @param region The region of interest (eg: Africa)
     * @return String of the name of the largest energy producer in the @param region
     */
    public String findLargestEnergyProducer(String region) {
        String largestEnergyProducer = "";
        String regionURL = "https://www.cia.gov/the-world-factbook/";
        try {
            this.currentDoc = Jsoup.connect(regionURL).get();

            //Gets links for all regions ~ This will be hard!
                                                            //Looking for Title of "The World & Regions"
            for (Element e : this.currentDoc.select("h2:contains(The World)")) {
                for( Element eref : e.parent().child(1).select("p")) {

                    //Making sure that we only look at the user-desired region (eg:"Africa")
                    if (eref.text().equals(region)) {
                        String hrefLink = eref.attr("href");
                        Pattern pattern = Pattern.compile("<a href=\"(.*?)\">");
                        // An example eref :  <p><strong><a href="/countries/world/">World</a></strong></p>
                        Matcher matcher = pattern.matcher(eref.html());
                        while(matcher.find()) {

                            //Now that we have the link to a region, we need to connect to it and grab energy production data
                            String regionLink = "https://www.cia.gov" + matcher.group(1);

                            try {
                                this.currentDoc = Jsoup.connect(regionLink).get();
                                Document regionDoc = Jsoup.connect(regionLink).get();

                                //"World" is a unique case
                                if(region.equalsIgnoreCase("World")) {

                                    Elements el = this.currentDoc.select("h3:contains(Electricity - production)");
                                    return el.first().nextElementSibling().text();
                                }
                                else {
                                    //We are now on a page containing multiple countries
                                    //Need to create an ArrayList<String> and ArrayList<Double> to store vals

                                    ArrayList<String> countryNames = new ArrayList<>();
                                    ArrayList<Double> countryEnergyVals = new ArrayList<>();

                                    for ( Element countryElement : regionDoc.select("h5 > a"))
                                    {
                                        String countryName = countryElement.text();

                                        //Now connects to each country
                                        // ciaBaseURL = "https://www.cia.gov"
                                        this.currentDoc = Jsoup.connect(ciaBaseURL + countryElement.attr("href")).get();


                                        Element el = this.currentDoc.selectFirst("h3:contains(Electricity - production)");
                                        if(el != null) {
                                            String txt = el.nextElementSibling().text();
                                            Double electricityVal = Double.parseDouble(txt.substring(0, txt.indexOf(" ")));

                                            if(txt.contains("million")) {
                                                electricityVal = electricityVal/1000000;
                                            }
                                            else if (txt.contains("billion")) {
                                                electricityVal = electricityVal/1000;
                                            }

                                            countryNames.add(countryName);
                                            countryEnergyVals.add(electricityVal);
                                        }

                                    }

                                    Double baseEnergy = 0.0;
                                    int savedIndex = 0;
                                    for(int i = 0; i < countryEnergyVals.size(); i++) {
                                        if(countryEnergyVals.get(i) > baseEnergy) {
                                            baseEnergy = countryEnergyVals.get(i);
                                            savedIndex = i;
                                        }
                                    }
                                    return countryNames.get(savedIndex)+", "+ countryEnergyVals.get(savedIndex)+" trillion kWh";
                                }
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }

                        }
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return largestEnergyProducer;
    }

    //4.
    /**
     *
     * @param region A string for the desired region, e.g. "Africa"
     * @return The country with the largest coastline
     */
    public String getLargestCoastline(String region) {
        String largestEnergyProducer = "";
        String regionURL = "https://www.cia.gov/the-world-factbook/";
        try {
            this.currentDoc = Jsoup.connect(regionURL).get();
            //Gets links for all regions
            //Looking for Title of "The World & Regions"
            for (Element e : this.currentDoc.select("h2:contains(The World)")) {
                for( Element eref : e.parent().child(1).select("p")) {

                    //Making sure that we only look at the user-desired region (eg:"Africa")
                    if (eref.text().equals(region)) {
                        String hrefLink = eref.attr("href");
                        Pattern pattern = Pattern.compile("<a href=\"(.*?)\">");
                        // An example eref :  <p><strong><a href="/countries/world/">World</a></strong></p>
                        Matcher matcher = pattern.matcher(eref.html());
                        while(matcher.find()) {

                            String regionLink = "https://www.cia.gov" + matcher.group(1);

                            try {
                                this.currentDoc = Jsoup.connect(regionLink).get();
                                Document regionDoc = Jsoup.connect(regionLink).get();
                                //We are now on a page containing multiple countries

                                ArrayList<String> countryNames = new ArrayList<>();
                                ArrayList<Double> countryVals = new ArrayList<>();
                                for ( Element countryElement : regionDoc.select("h5 > a"))
                                {
                                    String countryName = countryElement.text();
                                    // An example href :  <p><strong><a href="/countries/world/">World</a></strong></p>

                                    //Now connects to each country
                                    // ciaBaseURL = "https://www.cia.gov"
                                    this.currentDoc = Jsoup.connect(ciaBaseURL + countryElement.attr("href")).get();

                                    Element el = this.currentDoc.selectFirst("h3.mt30 > a:contains(Coastline)");
                                    Element coastLineEl = el.parent().nextElementSibling();


                                    Double coastLine = 0.0;
                                    Double area = 0.0;

                                    //Finding land area
                                    //<p><strong>total: </strong>2,381,740 sq km<br><br><strong>land: </strong>2,381,740 sq km<br><br><strong>water: </strong>0 sq km</p>
                                    Element el2 = this.currentDoc.selectFirst("p:contains(land:)");
                                    Pattern findAreaPattern = Pattern.compile("(^|\\s)([0-9]+)($|\\s)");

                                    if(el2 != null) {

                                        Pattern patternForCountry = Pattern.compile("land: </strong>(.*?) sq km<br>");
                                        Matcher matcherLand = patternForCountry.matcher(el2.html());
                                        if(matcherLand.find()) {
                                            area = Double.parseDouble(matcherLand.group(0).replaceAll("\\D", ""));
                                        }
                                    }

                                    if(coastLineEl != null ) {

                                        String cLT = coastLineEl.text();
                                                                                //Takes in first digit, then next space
                                        Matcher m = findAreaPattern.matcher(cLT);
                                        if(m.find()) {
                                            coastLine = Double.parseDouble(m.group(0));
                                        }

                                    }
                                    if(coastLine != 0 && area != 0) {
                                        countryNames.add(countryName);
                                        countryVals.add(coastLine / area);
                                    }
                                }

                                Double baseVal = 0.0;
                                int savedIndex = 0;
                                for(int i = 0; i < countryVals.size(); i++) {
                                    if(countryVals.get(i) > baseVal) {
                                        baseVal = countryVals.get(i);
                                        savedIndex = i;
                                    }
                                }
                                return countryNames.get(savedIndex)+", "+ countryVals.get(savedIndex)+" sq km";

                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }

                        }
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return largestEnergyProducer;
    }


    //5.

    /**
     *
     * @param region A string for the desired region, e.g. "Africa"
     * @return A String of the population of the country with the highest mean elevation in the given region
     */
    public String getPopulationHighestElevationCountry(String region) {
        String highestCountryElevation = "";
        String regionURL = "https://www.cia.gov/the-world-factbook/";
        try {
            this.currentDoc = Jsoup.connect(regionURL).get();
            //Gets links for all regions
            //Looking for Title of "The World & Regions"
            for (Element e : this.currentDoc.select("h2:contains(The World)")) {
                for( Element eref : e.parent().child(1).select("p")) {
                    //Making sure that we only look at the user-desired region (eg:"Africa")
                    if (eref.text().equals(region)) {

                        Pattern pattern = Pattern.compile("<a href=\"(.*?)\">");
                        // An example eref :  <p><strong><a href="/countries/world/">World</a></strong></p>
                        Matcher matcher = pattern.matcher(eref.html());
                        while(matcher.find()) {

                            String regionLink = "https://www.cia.gov" + matcher.group(1);
                            try {
                                this.currentDoc = Jsoup.connect(regionLink).get();
                                Document regionDoc = Jsoup.connect(regionLink).get();
                                //We are now on a page containing multiple countries

                                ArrayList<String> countryNames = new ArrayList<>();
                                ArrayList<Double> countryVals = new ArrayList<>();
                                //^This is the arrayList of elements we are comparing
                                ArrayList<Integer> countryPopulations = new ArrayList<>();

                                for ( Element countryElement : regionDoc.select("h5 > a"))
                                {
                                    //Now connects to each country
                                    // ciaBaseURL = "https://www.cia.gov"
                                    this.currentDoc = Jsoup.connect(ciaBaseURL + countryElement.attr("href")).get();
                                    //Val is elevation
                                    Double val = 0.0;
                                    Integer population = 0;
                                    String countryName = countryElement.text();

                                    //Element that we will be comparing
                                    Element el = this.currentDoc.selectFirst("h3.mt30 > a:contains(Elevation)");
                                    Element valEl = el.parent().nextElementSibling();

                                    Pattern findAreaPattern = Pattern.compile("mean elevation: </strong>(.*?) m");
                                    //Finding elevations
                                    Matcher m = findAreaPattern.matcher(valEl.html());
                                    if(m.find()) {
                                        val = Double.parseDouble(m.group(0).replaceAll("\\D",""));
                                    }

                                    //Finding population
                                    //population shows up a lot! So... the easiest way to approach this is to
                                    // Find the region of interest, "People and Society" , then look for population in
                                    // there

                                    Element el3 = this.currentDoc.selectFirst("div[id=people-and-society]:contains(Population)");
                                    Element el2 = el3.selectFirst("p");

                                    String populationString = el2.text().substring(0,el2.text().indexOf(" ")).replaceAll("\\D", "");
                                    if(!populationString.equals(""))
                                        population = Integer.parseInt(populationString);

                                    if(valEl != null ) {

                                        String valString = valEl.text();
                                        //Takes in first digit, then next space
                                         m = findAreaPattern.matcher(valString);
                                        if(m.find()) {
                                            val = Double.parseDouble(m.group(0));
                                        }

                                    }
                                    if( val != 0.0 && population != 0) {
                                        countryNames.add(countryName);
                                        countryPopulations.add(population);
                                        countryVals.add(val);
                                    }
                                }

                                Double baseVal = 0.0;
                                int savedIndex = 0;
                                for(int i = 0; i < countryVals.size(); i++) {
                                    if(countryVals.get(i) > baseVal) {
                                        baseVal = countryVals.get(i);
                                        savedIndex = i;
                                    }
                                }
                                if(countryNames.size() > 0 && countryVals.size() > 0)
                                    return countryNames.get(savedIndex)+", "+ countryPopulations.get(savedIndex)+"";


                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }

                        }
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return "Not found";
    }
    //6.

    /**
     *
     * @param region A string for the desired region, e.g. "Africa"
     * @return A String of import partners
     */
    public String getImportPartners(String region) {
        ArrayList<Country> countryArrayList = new ArrayList<Country>();
        String countryListURL = "https://www.cia.gov/the-world-factbook/field/map-references/";
        try {
            this.currentDoc = Jsoup.connect(countryListURL).get();

            for (Element e : this.currentDoc.select("li > p")) {
                String countryName = e.parent().child(0).text();
                String regionName = e.text();
                String hrefLink = e.parent().child(0).selectFirst("a").attr("href");
                String fullLink = "https://www.cia.gov" + hrefLink;
                if( regionName.contains(region) ) { // Case sensitive!

                    //Now to connect to the new country page!
                    try {
                        this.currentDoc = Jsoup.connect(fullLink).get();

                        Double area = 0.0;
                        //Look for Land area
                        Element el2 = this.currentDoc.selectFirst("p:contains(land:)");
                        if (el2 != null) {
                            Pattern patternForCountry = Pattern.compile("land: </strong>(.*?) sq km<br>");
                            Matcher matcherLand = patternForCountry.matcher(el2.html());
                            if (matcherLand.find()) {
                                area = Double.parseDouble(matcherLand.group(0).replaceAll("\\D", ""));
                            }
                        }
                        countryArrayList.add(new Country(countryName, area));
                        //Finding import partners
                        Element partnersEl = this.currentDoc.selectFirst("h3.mt30 > a:contains(Partners)");
                        if(partnersEl != null) {
                            partnersEl = partnersEl.parent().parent().selectFirst("p");
                            countryArrayList.get(countryArrayList.size()-1).addPartner(partnersEl.text());
                        }

                    } catch (IOException exception) {
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        // Now we need to sort the arrayLists and return them as a List[String]

        Collections.sort(countryArrayList);

        if(countryArrayList.size() >= 3)
        {
            return countryArrayList.get(2).getPartner();
        }
        else
        {
            return countryArrayList.get(countryArrayList.size()-1).getPartner();
        }


    }

    //7.
    /**
     *
     * @param startingLetter A letter to start from, e.g. "D"
     * @return String[] containing ccountry names
     */
    public String[] getCountriesByArea(String startingLetter) {

        ArrayList<Country> countryArrayList = new ArrayList<Country>();

        String countryListURL = "https://www.cia.gov/the-world-factbook/field/map-references/";
        try {
            this.currentDoc = Jsoup.connect(countryListURL).get();

            for (Element e : this.currentDoc.select("h2.h3 > a")) {
                String hrefLink = e.attr("href"); //This is the href that contains the page for a country
                // Ex: /the-world-factbook/countries/afghanistan
                String fullLink = "https://www.cia.gov" + hrefLink;
                String countryName = hrefLink.substring(hrefLink.lastIndexOf("/") + 1);
                if(countryName.substring(0,1).equalsIgnoreCase(startingLetter.substring(0,1))) {
                    //Now to connect to the new country page!
                    try {
                        this.currentDoc = Jsoup.connect(fullLink).get();

                        Double area = 0.0;
                        //Look for Land area
                        Element el2 = this.currentDoc.selectFirst("p:contains(land:)");
                        Pattern findAreaPattern = Pattern.compile("(^|\\s)([0-9]+)($|\\s)");

                        if (el2 != null) {

                            Pattern patternForCountry = Pattern.compile("land: </strong>(.*?) sq km<br>");
                            Matcher matcherLand = patternForCountry.matcher(el2.html());
                            if (matcherLand.find()) {
                                area = Double.parseDouble(matcherLand.group(0).replaceAll("\\D", ""));
                            }
                        }
                        countryArrayList.add(new Country(countryName, area));
                    } catch (IOException exception) {
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        // Now we need to sort the arrayLists and return them as a List[String]

        Collections.sort(countryArrayList);
        String[] countryArr = new String[countryArrayList.size()];
        for(int i = 0; i < countryArrayList.size(); i++) {
            countryArr[i] = countryArrayList.get(i).getName();
        }

        return countryArr;
    }
    //8.
    public String[] getCountriesInflationRate(String keyword) {

        ArrayList<Country> countryArrayList = new ArrayList<Country>();
        String countryListURL = "https://www.cia.gov/the-world-factbook/field/map-references/";
        try {
            this.currentDoc = Jsoup.connect(countryListURL).get();

            for (Element e : this.currentDoc.select("h2.h3 > a")) {
                String hrefLink = e.attr("href"); //This is the href that contains the page for a country
                // Ex: /the-world-factbook/countries/afghanistan
                String fullLink = "https://www.cia.gov" + hrefLink;
                String countryName = hrefLink.substring(hrefLink.lastIndexOf("/") + 1);
                if(countryName.contains(keyword)) {
                    //Now to connect to the new country page!
                    try {
                        this.currentDoc = Jsoup.connect(fullLink).get();

                        Double inflation = 0.0;
                        //Look for inflation
                        Element el2 = this.currentDoc.selectFirst("div > h3:contains(Inflation Rate)");
                        if(el2 != null && el2.parent().nextElementSibling() != null) {
                            el2 = el2.parent().child(1);
                        }
                        if (el2 != null) {
                            Pattern patternForCountry = Pattern.compile("(.*?)%");
                            Matcher matcherLand = patternForCountry.matcher(el2.text());
                            if (matcherLand.find()) {
                                inflation = Double.parseDouble(matcherLand.group(0).replaceAll("%", ""));
                            }
                        }
                        countryArrayList.add(new Country(countryName, inflation));
                    } catch (IOException exception) {

                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        // Now we need to sort the arrayLists and return them as a List[String]

        Collections.sort(countryArrayList);
        String[] countryArr = new String[countryArrayList.size()];
        for(int i = 0; i < countryArrayList.size(); i++) {
            countryArr[i] = countryArrayList.get(i).getName();
        }

        return countryArr;
    }


    /**
     * Provides a temporary fix for Incorrect CIA World Factbook hyperlinks.
     * This diagnoses an issue in which "the-world-factbook" is not included in the URL.
     * Requires Pattern and Matcher imported.
     * @author Daniel (TA), Code Provided on Piazza
     * @param url A valid URL that includes cia.gov, which should be all links currently on the
     *            factbook.
     * @exception IllegalArgumentException malformed URL that is not for cia.gov.
     * @return A URL which appends the necessary string to ensure it properly links within the
     *            Factbook.
     */

    static String appendCIAbug(String url) {
        Matcher m = Pattern.compile("www\\.cia\\.gov\\/(.*)").matcher(url);
        if (!m.find()) {throw new IllegalArgumentException("Invalid CIA URL");}
        return url.contains("/the-world-factbook/") ? url : "https://www.cia" +
                ".gov/the-world-factbook/" + m.group(1);
    }


    private class Country implements Comparable<Country>{
        private String countryName;
        private Double val;
        private String partners;

        public Country ( String countryName, Double val) {
            this.countryName = countryName;
            this.val = val;
            partners = "";
        }
        public Double getVal() {
            return val;
        }
        public String getName(){
            return countryName;
        }
        public void addPartner(String partnerName) {
            partners = (partnerName);
        }
        public String getPartner() {
            return partners;
        }
        @Override
        public int compareTo(Country c) {
            if(this.val == c.getVal()) {
                return 0;
            }
            else if(this.val > c.getVal()) {
                return 1;
            }
            return -1;
        }

    }
}


