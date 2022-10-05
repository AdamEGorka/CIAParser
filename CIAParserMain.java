import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CIAParserMain {

    public static void main(String[] args) {

        CIAParser parser = new CIAParser();
        Scanner scanner = new Scanner(System.in);
        //1.
        System.out.println("1. List all countries with flags containing both _ and _ colors.");
        String color1 = scanner.nextLine();
        String color2 = scanner.nextLine();
        System.out.println(parser.getFlagColors(color1, color2));

        // 2.
        System.out.println("2. What is the lowest point in the _?");
        String ocean = scanner.nextLine();
        System.out.println(parser.getLowestOceanPoint(ocean));

        // 3.
        System.out.println("3. Find the largest country in _ in terms of Electricity Production.");
        String region = scanner.nextLine();
        System.out.println(parser.findLargestEnergyProducer(region));

        //4.
        System.out.println("4. Norway is notoriously known for having the largest coastline in Europe despite its small land\n" +
                "area. Which country in _ has the largest coastline to land area ratio?");
        region = scanner.nextLine();
        System.out.println(parser.getLargestCoastline(region));

        //5.
        System.out.println("5. What is the population of the country in _ with the highest mean elevation?");
        region = scanner.nextLine();
        System.out.println(parser.getPopulationHighestElevationCountry(region));

        //6.
        System.out.println("6. Many islands rely on imports from larger countries. Which countries are the Imports Partners\n" +
                "for the third largest island (by total area) in the _?");
        region = scanner.nextLine();
        String importPartners = parser.getImportPartners(region);
        System.out.println(importPartners);

        //7.
        System.out.println("7. Provide a list of all countries starting with the letter _, sorted by total area, smallest to largest.");
        region = scanner.nextLine();
        String[] countriesByArea = parser.getCountriesByArea(region);
        for(int i = 0; i < countriesByArea.length; i++) {
            System.out.print(countriesByArea[i]+", ");
        }

        //8.
        //Provide a list of all countries containing "land" in their name e.g. "Greenland", sorted by inflation rate
        //For "land" - other examples : North, South, West, East, New, Island, The, ...
        System.out.println("Provide a list of all countries containing \"_\" in their name e.g. \"Greenland\", sorted by inflation rate");
        region = scanner.nextLine();
        String[] countriesByInflation = parser.getCountriesInflationRate(region);
        for(int i = 0; i < countriesByInflation.length; i++) {
            System.out.print(countriesByInflation[i]+", ");
        }


    }
}
