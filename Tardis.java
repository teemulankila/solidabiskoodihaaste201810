import java.util.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

class Parkkihalli {
    public static void main(String[] args) {
        //Generate the parkkihalli and customers
        Parkkihalli paha = new Parkkihalli();

        paha.calculateStuff();
    }

    private static List<TuloJaLahtoAika> tuloJaLahtoAjat = new ArrayList<>();

    public Parkkihalli() {
        for (int i = 0; i < 50000; i++) {
            tuloJaLahtoAjat.add(new TuloJaLahtoAika());
        }
    }

    public void calculateStuff() {
        for (int i = 0; i < tuloJaLahtoAjat.size(); i++) {
            if (tuloJaLahtoAjat.get(i).timeTraveller() == true) {
                System.out.println(tuloJaLahtoAjat.get(i).toString());
            }
        }
    }

    public List<TuloJaLahtoAika> getTimeStamps() {
        return tuloJaLahtoAjat;
    }

    public String toString() {
        return tuloJaLahtoAjat.toString();
    }

    public class TuloJaLahtoAika {
        private Date arrival;
        private Date departure;

        public TuloJaLahtoAika() {
            Long minArrivalTime = Timestamp.valueOf("2018-08-01 00:00:00").getTime();
            Long maxDepartureTime = Timestamp.valueOf("2018-09-30 00:00:00").getTime();
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

        public boolean timeTraveller() {
            return (getArrivalTime().compareTo(getDepartureTime()) == 1);
        }
    }
}
