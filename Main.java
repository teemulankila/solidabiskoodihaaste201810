/**
 * (C) 2018 Teemu Lankila
 * Competition entry for Solidabis koodihaaste from August 2018
 * This code can be freely used for any purpose.
 * Parts of code (C) 2018 Solidabis
 */

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

class Parkkihalli {
    //Static values defined by competition rules
    private static int MAX_CUSTOMERS = 3 * 6 * 9;
    private static String REPORT_TIME = "2018-09-15 09:00:00";
    private static int PARKING_FEE_PER_MINUTE = 5; //Cents

    private static List<TuloJaLahtoAika> tuloJaLahtoAjat = new ArrayList<>();

    private int currentCustomers = 0;
    private double utilizationRate = 0;

    public static void main(String[] args) {
        //Generate the parking garage and customers
        Parkkihalli paha = new Parkkihalli();

        //Sort the customers by arriving order
        Collections.sort(tuloJaLahtoAjat);

        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        Date reportTime;
        try {
            reportTime = ft.parse(REPORT_TIME);
            paha.generateReport(reportTime);
        } catch (ParseException e) {
            System.out.println("Unparseable time for format " + ft);
        }
    }

    public Parkkihalli() {
        for (int i = 0; i < 50000; i++) {
            tuloJaLahtoAjat.add(new TuloJaLahtoAika());
        }
    }

    /**
     * Report following data:
     * - Amount of current filled parking slots
     * - Revenue
     * - Loss of revenue due full capacity
     * - Utilization rate
     * @param reportDate Date and time for the report
     */
    public void generateReport(Date reportDate) {
        List<Date> leavers = new ArrayList<>();
        Date currentTime = null;
        Date firstCustomer = null;
        long reportPeriodLength = 0;
        int totalRevenue = 0; //Cents
        int totalLoss = 0; //Cents
        int parkingFee = 0; //Cents

        //Go through all the arriving customers. List needs to be sorted first by arrival time.
        for (int i = 0; i < tuloJaLahtoAjat.size(); i++) {
            //Check data validity
            if (tuloJaLahtoAjat.get(i).invalidData()) {
                //Considering this data to be error which can be ignored
                continue;
            }

            ///Round up minutes since every starting minute generates a fee per minute
            int parkingTime = (int)Math.ceil((tuloJaLahtoAjat.get(i).departure.getTime() - tuloJaLahtoAjat.get(i).arrival.getTime()) / 1000.0 / 60.0);
            parkingFee = PARKING_FEE_PER_MINUTE * parkingTime;

            if (reportPeriodLength == 0) {
                //Save the time period length what is included in the report
                reportPeriodLength = reportDate.getTime() - tuloJaLahtoAjat.get(i).arrival.getTime();
                //Save the start time of the report
                firstCustomer = tuloJaLahtoAjat.get(i).arrival;
            }

            //What is current time
            currentTime = tuloJaLahtoAjat.get(i).arrival;

            //Check if all arriving cars have been included
            if(currentTime.getTime() > reportDate.getTime()) {
                //Next car arrives after the needed report date. No need to continue calculating rest of the customers.

                //Still need to check how many more has left the building
                for (int x = leavers.size()-1; x >= 0; x--) {
                    if (leavers.get(x).compareTo(reportDate) < 0) {
                        //This already left
                        currentCustomers--;
                        leavers.remove(x);
                    }
                }
                //Exit the loop
                break;
            }

            //Check how many left since last arrival
            for (int x = leavers.size()-1; x >= 0; x--) {
                if (leavers.get(x).compareTo(currentTime) < 0) {
                    //This already left
                    currentCustomers--;
                    leavers.remove(x);
                }
            }

            //Check if the parking garage has some room left for new customers
            if (currentCustomers < MAX_CUSTOMERS) {
                //This will fit to parking garage

                //Each customer increases the utilization rate. Calculate the weighted arithmetic mean.
                //Weight is the time how long customer spent parked in the garage.
                long deltaTime = 0;
                if (reportDate.getTime() < tuloJaLahtoAjat.get(i).departure.getTime()) {
                    //Stop counting to report time if car is still parked at that time
                    deltaTime = reportDate.getTime() - tuloJaLahtoAjat.get(i).arrival.getTime();
                } else {
                    //Customer arrived and left within the start and report time
                    deltaTime = tuloJaLahtoAjat.get(i).departure.getTime() - tuloJaLahtoAjat.get(i).arrival.getTime();
                }
                utilizationRate += deltaTime/(double)reportPeriodLength;

                currentCustomers++;
                //Add leaving time to list
                leavers.add(tuloJaLahtoAjat.get(i).departure);
                //Calculate cumulative revenue
                totalRevenue += parkingFee;

                //DEBUG info print
                /*
                System.out.println("Arrival : " + tuloJaLahtoAjat.get(i).arrival.toString() + " Departure: " + tuloJaLahtoAjat.get(i).departure.toString());
                System.out.println("Parking minutes: " + parkingTime);
                System.out.println("Total revenue: " + totalRevenue/100.0 + " euros");
                */
            } else {
                //Parking garage full. Nothing else to do than add the fee to the cumulative loss.
                totalLoss += parkingFee;
            }
        }
        System.out.println("Report at " + reportDate.toString());
        System.out.println("Customers: \t" + currentCustomers + "/" + MAX_CUSTOMERS);
        DecimalFormat df2 = new DecimalFormat( "#,###,###,##0.00" );
        String revenueString = df2.format(totalRevenue/100.0);
        System.out.println("Report period starts at " + firstCustomer.toString());
        System.out.println("Total revenue: \t" + revenueString + " euros");
        String lossString = df2.format(totalLoss/100.0);
        System.out.println("Total loss: \t" + lossString + " euros");
        //Convert the utilization rate from parking slots to percentage
        String utilizationRateString = df2.format(utilizationRate / MAX_CUSTOMERS * 100);
        System.out.println("Average usage over time: " + utilizationRateString + " %");
    }

    public List<TuloJaLahtoAika> getTimeStamps() {
        return tuloJaLahtoAjat;
    }

    public String toString() {
        return tuloJaLahtoAjat.toString();
    }

    public class TuloJaLahtoAika implements Comparable<TuloJaLahtoAika> {
        private Date arrival;
        private Date departure;

        public TuloJaLahtoAika() {
            Long minArrivalTime = Timestamp.valueOf("2018-08-01 00:00:00").getTime();
            Long maxDepartureTime = Timestamp.valueOf("2018-09-30 00:00:00").getTime();
            //System.out.println(maxDepartureTime);
            Long randomTimeBetweenMaxAndMin = minArrivalTime + (long)(Math.random() * (maxDepartureTime - minArrivalTime + 1));

            arrival = new Date(randomTimeBetweenMaxAndMin);
            departure = new Date(maxDepartureTime);

            while ((departure).after(new Date(maxDepartureTime - 1))) {
                departure = new Date((long)(randomTimeBetweenMaxAndMin + new Random().nextGaussian() * (120 * 60000) + 360 * 60000));
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            dateFormat.format(arrival);
            dateFormat.format(departure);
        }

        private Date getArrivalTime() {
            return arrival;
        }

        private Date getDepartureTime() {
            return departure;
        }

        public String toString() {
            return "saapumisaika: " + arrival + " lähtöaika: " + departure + System.getProperty("line.separator");
        }

        /**
         * Check if the arrival and departure times are in correct order
         * @return true when data is invalid
         */
        public boolean invalidData() {
            return (getArrivalTime().compareTo(getDepartureTime()) == 1);
        }

        /**
         * Make the class comparable by arrival time. Enables sorting.
         * @param o the object to be compared.
         * @return Integer as defined by the interface
         */
        @Override
        public int compareTo(TuloJaLahtoAika o) {
            return getArrivalTime().compareTo(o.getArrivalTime());
        }
    }
}
