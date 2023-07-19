/* Code for COMP-102-112 - 2021T1, Assignment 5
 * Name: Annie Cho
 * Username: choanni
 * ID: 300575457
 */

import ecs100.*;
import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JSlider;

/**
 * WeatherReporter
 * Analyses weather data from files of weather-station measurements.
 *
 * The weather data files consist of a set of measurements from weather stations around
 * New Zealand at a series of date/time stamps.
 * For each date/time, the file has:
 *  A line with the date and time (four integers for day, month, year, and time)
 *   eg "24 01 2021 1900"  for 24 Jan 2021 at 19:00 
 *  A line with the number of weather-stations for that date/time 
 *  Followed by a line of data for each weather station:
 *   - name: one token, eg "Cape-Reinga"
 *   - (x, y) coordinates on the map: two numbers, eg   186 38
 *   - four numbers for temperature, dew-point, suface-pressure, and sea-level-pressure
 * Some of the data files (eg hot-weather.txt, and cold-weather.txt) have data for just one date/time.
 * The weather-all.txt has data for lots of times. The date/times are all in order.
 * You should look at the files before trying to complete the methods below.
 *
 * Note, the data files were extracted from MetOffice weather data from 24-26 January 2021
 */

public class WeatherReporter{
    public static final double DIAM = 10;       // The diameter of the temperature circles.    
    public static final double LEFT_TEXT = 10;  // The left of the date text
    public static final double TOP_TEXT = 50;   // The top of the date text
    int lineCount = 0;
    int numStations = 0;
    String plot = "";
    ArrayList <Double> plotValues = new ArrayList<Double>();
    ArrayList <Double> tempValues = new ArrayList<Double>();
    ArrayList <Double> xValues = new ArrayList<Double>();
    ArrayList <Double> yValues = new ArrayList<Double>();
    ArrayList <Double> dewValues = new ArrayList<Double>();
    ArrayList <Double> surfaceValues = new ArrayList<Double>();
    ArrayList <Double> seaValues = new ArrayList<Double>();// for identifying max and minimum

    /**   CORE
     * Plots the temperatures for one date/time from a file on a map of NZ
     * Asks for the name of the file and opens a Scanner
     * It is good design to call plotSnapshot, passing the Scanner as an argument.
     */
    public void plotTemperatures(){
        String filename = UI.askString("What is the name of the file?");
        plot = UI.askString("What would you like to plot?");
        try {
            List <String> allLines = Files.readAllLines(Path.of(filename));
            for (String line : allLines){
                Scanner scanner = new Scanner(line);
                plotSnapshot(scanner);
                lineCount++;
            }
            lineCount = 0;
            tempValues.clear();
            xValues.clear();
            yValues.clear();
            dewValues.clear();
            surfaceValues.clear();
            seaValues.clear();
            plotValues.clear();
        } catch(IOException e){UI.println("File reading failed");}    
    }

    /**
     * CORE:
     *  Plot the temperatures for the next snapshot in the file by drawing
     *   a filled coloured circle (size DIAM) at each weather-station location.
     *  The colour of the circle should indicate the temperature.
     *
     *  The method should
     *   - read the date/time and draw the date/time at the top-left of the map.
     *   - read the number of stations, then
     *   - for each station,
     *     - read the name, coordinates, and data, and
     *     - plot the temperature for that station. 
     *   (Hint: You will find the getTemperatureColor(...) method useful.)
     *
     *  COMPLETION:
     *  Also finds the highest and lowest temperatures at that time, and
     *  plots them with a larger circle.
     *  (Hint: If more than one station has the highest (or coolest) temperature,
     *         you only need to draw a larger circle for one of them.
     */   
    
    public void drawCircle(String valueName, double value, double x, double y, double size){
        UI.setColor(getTemperatureColor(value));
        UI.fillOval(x,y,size,size);
        if (valueName.equals("sea level") || valueName.equals("surface")){
            UI.setColor(Color.black);
            UI.drawOval(x,y,size,size);
        }
    }
    
    public void plotSnapshot(Scanner sc){
        if (lineCount == 0){
            UI.drawImage("map-new-zealand.gif", 0, 0);
            String day = sc.next();
            String month = sc.next();
            String year = sc.next();
            String time = sc.next();
            UI.drawString(day + "/" + month + "/" + year + " " + time, LEFT_TEXT, TOP_TEXT);
            UI.println("Snapshot: " + day + "/" + month + "/" + year + " " + time);
        }
        else if(lineCount == 1){
            numStations = sc.nextInt();
            UI.println("Number of stations: " + numStations);
        }
        else{
            String stationName = sc.next();
            xValues.add(sc.nextDouble());
            yValues.add(sc.nextDouble());
            tempValues.add(sc.nextDouble());
            dewValues.add(sc.nextDouble());
            surfaceValues.add(sc.nextDouble());
            seaValues.add(sc.nextDouble());
            
            if (plot.equals("temperature")){
                plotValues = tempValues;
                plot = "temperature";
            }
            else if (plot.equals("dew")){
                plotValues = dewValues;
                plot = "dew";
            }
            else if (plot.equals("surface")){
                plotValues = surfaceValues;
                plot = "surface";
            }
            else if (plot.equals("sea level")){
                plotValues = seaValues;
                plot = "sea level";
            }
        }
        if (lineCount == numStations+1){  // if we're on the last station
            int maxIndex = plotValues.indexOf(Collections.max(plotValues)); // finds the index of the max value
            int minIndex = plotValues.indexOf(Collections.min(plotValues)); // finds the index of the min value
            for (int i = 0; i<numStations; i++){
                if (i==maxIndex || i==minIndex){
                    drawCircle(plot, plotValues.get(i), xValues.get(i)-DIAM, yValues.get(i)-DIAM, DIAM*2);
                }
                else{
                    drawCircle(plot, plotValues.get(i), xValues.get(i)-DIAM/2, yValues.get(i)-DIAM/2, DIAM);
                }
            }
        }
    }

    /**   COMPLETION
     * Displays an animated view of the temperatures over all
     * the times in a weather data files, plotting the temperatures
     * for the first date/time, as in the core, pausing for half a second,
     * then plotting the temperatures for the second date/time, and
     * repeating until all the data in the file has been shown.
     * 
     * (Hint, use the plotSnapshot(...) method that you used in the core)
     */
    public void animateTemperatures(){
        try {
            List <String> allLines = Files.readAllLines(Path.of("weather-all.txt"));
            plot = UI.askString("What value would you like to plot?");
            for (String line : allLines){
                Scanner scanner = new Scanner(line);
                plotSnapshot(scanner);
                lineCount++;
                if (lineCount == numStations+2){   // if it is on the last station 
                    lineCount = 0; // then reset the linecount
                    UI.sleep(500); // and wait
                    UI.clearGraphics();
                    tempValues.clear();
                    xValues.clear();
                    yValues.clear();
                    dewValues.clear();
                    surfaceValues.clear();
                    seaValues.clear();
                    plotValues.clear();
                }
            }
            lineCount = 0;
        } catch(IOException e){UI.println("File reading failed");}    
    }

    /**   COMPLETION
     * Prints a table of all the weather data from a single station, one line for each day/time.
     * Asks for the name of the station.
     * Prints a header line
     * Then for each line of data for that station in the weather-all.txt file, it prints 
     * a line with the date/time, temperature, dew-point, surface-pressure, and  sealevel-pressure
     * If there are no entries for that station, it will print a message saying "Station not found".
     * Hint, the \t in a String is the tab character, which helps to make the table line up.
     */
    public void reportStation(){
        int reportLineCount = 0;
        int reportStations = 0;
        String day = "";
        String month = "";
        String year = "";
        String time = "";
        boolean stationFound = false;
        String stationName = UI.askString("Name of a station: ");
        UI.printf("Report for %s: \n", stationName);
        UI.println("Date       \tTime \tTemp \tDew \tkPa \t\tSea kPa");  // makes a header 
        try {
            List <String> allLines = Files.readAllLines(Path.of("weather-all.txt"));
            for (String line : allLines){
                Scanner scanner = new Scanner(line);
                if (reportLineCount == 0){
                    day = scanner.next();
                    month = scanner.next();
                    year = scanner.next();
                    time = scanner.next();
                } 
                else if (reportLineCount == 1){
                    reportStations = scanner.nextInt();
                }
                else if ((scanner.next()).equalsIgnoreCase(stationName)){  // if first value in scanner is equal to the stationName then                
                    String x = scanner.next();
                    String y = scanner.next();
                    String temp = scanner.next();
                    String dew = scanner.next();
                    String surface = scanner.next();
                    String seaLevel = scanner.next();
                    stationFound = true;
                    if (temp.equals("-999")){
                        temp = "-";
                    }
                    if (dew.equals("-999")){
                        dew = "-";
                    }
                    if (surface.equals("-999")){
                        surface = "-";
                    }
                    if (seaLevel.equals("-999")){
                        seaLevel = "-";
                    }
                    UI.println(day + "/" + month + "/" + year +"\t"+time+" \t"+temp+" \t"+dew+" \t"+surface+" \t\t"+seaLevel);
                }
                reportLineCount++;
                if (reportLineCount == reportStations+2){
                    reportLineCount = 0;
                }
            }
            if (stationFound == false){
                    UI.println("Station not found.");
                }
        } catch(IOException e){UI.println("File reading failed");}  

    }

    /** Returns a color representing that temperature
     *  The colors are increasingly blue below 15 degrees, and
     *  increasingly red above 15 degrees.
     */
    public Color getTemperatureColor(double temp){
        double max = 37, min = -5, mid = (max+min)/2;
        if (temp < min || temp > max){
            return Color.white;
        }
        else if (temp <= mid){ //blue range: hues from .7 to .5
            double tempFracOfRange = (temp-min)/(mid-min);
            double hue = 0.7 -  tempFracOfRange*(0.7-0.5); 
            return Color.getHSBColor((float)hue, 1.0F, 1.0F);
        }
        else { //red range: .15 to 0.0
            double tempFracOfRange = (temp-mid)/(max-mid);
            double hue = 0.15 -  tempFracOfRange*(0.15-0.0); 
            return Color.getHSBColor((float)hue, 1.0F, 1.0F);
        }
    }
    
    public void setupGUI(){
        UI.initialise();
        UI.addButton("Clear", UI::clearGraphics);
        UI.addButton("Plot temperature", this::plotTemperatures);
        UI.addButton("Animate temperature", this::animateTemperatures);
        UI.addButton("Report",  this::reportStation);
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(800,750);
        UI.setFontSize(18);
    }
    public static void main(String[] arguments){
        WeatherReporter obj = new WeatherReporter();
        obj.setupGUI();
    }    
}
