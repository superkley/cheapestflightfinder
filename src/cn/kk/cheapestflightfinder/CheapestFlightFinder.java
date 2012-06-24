package cn.kk.cheapestflightfinder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CheapestFlightFinder {
    private static final int RETRIES = 10;
    private String resultPage;

    public static void main(String[] args) throws InterruptedException, IOException {
        GregorianCalendar startDate = new GregorianCalendar(2012, 6, 20);
        GregorianCalendar endDate = new GregorianCalendar(2012, 9, 3);
        int minDays = 20;
        int maxDays = 40;

        List<Flight> possibleFlights = new LinkedList<Flight>();
        GregorianCalendar limit = (GregorianCalendar) startDate.clone();
        limit.add(Calendar.DAY_OF_YEAR, minDays);
        System.out.println(limit.getTime());
        while (endDate.after(limit)) {
            for (int days = minDays; days <= maxDays; days++) {
                GregorianCalendar start = (GregorianCalendar) startDate.clone();
                GregorianCalendar stop = (GregorianCalendar) startDate.clone();
                stop.add(Calendar.DAY_OF_YEAR, days);
                if (stop.after(endDate)) {
                    break;
                }
                final Flight flight = new Flight(start, stop);
                // flight.setNonStop(false);
                possibleFlights.add(flight);
            }
            startDate.add(Calendar.DAY_OF_YEAR, 1);
            limit = (GregorianCalendar) startDate.clone();
            limit.add(Calendar.DAY_OF_YEAR, minDays);
        }

        final int size = possibleFlights.size();
        System.out.println("共查询总数：" + size);
        final Semaphore lock = new Semaphore(size);
        lock.acquire(size);

        ExecutorService es = Executors.newFixedThreadPool(10);
        for (final Flight flight : possibleFlights) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        new CheapestFlightFinder().start(flight);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    } finally {
                        lock.release();
                    }
                }
            });
        }

        lock.acquire(size);
        es.awaitTermination(1, TimeUnit.SECONDS);

        Collections.sort(possibleFlights, new Comparator<Flight>() {
            @Override
            public int compare(Flight o1, Flight o2) {
                double thisVal = o1.getPriceValue();
                double anotherVal = o2.getPriceValue();
                return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
            }
        });

        for (int i = 0; i < Math.min(10, size); i++) {
            System.out.println(i + ": " + possibleFlights.get(i));
        }
        
        BufferedWriter writer = new BufferedWriter(new FileWriter("F:\\cheapestflights.txt"));
        for (Flight f: possibleFlights) {
            writer.write(f.toString());
            writer.write("\r\n");
        }
        writer.close();        
    }

    private void start(final Flight flight) {
        final Helper helper = new Helper();

        tryAndRun("open start page", helper, new Runnable() {
            @Override
            public void run() {
                openStartPage(helper);
            }
        });

        tryAndRun("search price", helper, new Runnable() {
            @Override
            public void run() {
                searchPrice(helper, flight.getAccDep(), flight.getAccArr(), flight.getMode(), flight.getFlightType(),
                        flight.getDepartureDateAsString(), flight.getReturnDateAsString(), flight.isCabinClass(),
                        flight.isNonStop());
            }
        });

        tryAndRun("read results", helper, new Runnable() {
            @Override
            public void run() {
                readResults(helper, flight);
            }
        });

        System.err.println(flight.toString());
    }

    private void readResults(final Helper helper, final Flight flight) {
        if (resultPage != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                        helper.openUrlInputStream(resultPage))));
                String line;
                boolean found = false;
                int step = 0;
                while (null != (line = reader.readLine())) {
                    if (line.contains("offerWithoutInsur") || line.contains("flightPriceButton")) {
                        break;
                    }
                    if (line.contains("endpreis")) {
                        found = true;
                        step = 0;
                        continue;
                    }
                    if (found) {
                        switch (step) {
                        case 0:
                            flight.setPrice(line.trim().replace(".", "").replace(',', '.'));
                            step = 1;
                            break;
                        case 1:
                            if (line.contains("Reisedauer:&nbsp;")) {
                                if (Helper.isEmptyOrNull(flight.getDepDuration())) {
                                    flight.setDepDuration(line.substring(line.indexOf("Reisedauer:&nbsp;")
                                            + "Reisedauer:&nbsp;".length()));
                                } else {
                                    flight.setDepDuration(flight.getDepDuration()
                                            + ", "
                                            + line.substring(line.indexOf("Reisedauer:&nbsp;")
                                                    + "Reisedauer:&nbsp;".length()));
                                }
                            }
                            break;
                        case 2:
                            if (line.contains("Reisedauer:&nbsp;")) {
                                if (Helper.isEmptyOrNull(flight.getRetDuration())) {
                                    flight.setRetDuration(line.substring(line.indexOf("Reisedauer:&nbsp;")
                                            + "Reisedauer:&nbsp;".length()));
                                } else {
                                    flight.setRetDuration(flight.getRetDuration()
                                            + ", "
                                            + line.substring(line.indexOf("Reisedauer:&nbsp;")
                                                    + "Reisedauer:&nbsp;".length()));
                                }
                            }
                            break;
                        }
                        if (line.contains("Rückflug")) {
                            step = 2;
                        }
                        if (line.contains("airlinesRow")) {
                            if (step == 1) {
                                flight.setDepAirline(Helper.substringBetween(line, "title=\"", "\">"));
                            } else {
                                flight.setRetAirline(Helper.substringBetween(line, "title=\"", "\">"));
                            }
                        }
                    }
                }
                reader.close();
                if (flight.getPrice() == null) {
                    throw new RuntimeException("No price found for current parameters!");
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private void searchPrice(Helper helper, String accDep, String accArr, String mode, String flightType,
            String departureDate, String returnDate, boolean cabinClass, boolean nonStop) {
        String depCity = accDep;// URLEncoder.encode("Frankfurt am Main - Alle Flughäfen (FRA) - Deutschland", "UTF-8");
        String arrCity = accArr; // URLEncoder.encode("Shanghai - Alle Flughäfen (SHA) - China", "UTF-8");
        String paxAdt = "1";
        String paxChd = "0";
        String paxInf = "0";
        String departureTimeRange = ""; // ,1,2
        String returnTimeRange = ""; // ,1,2
        String depAirline = ""; // e.g. CI

        String url = "http://www.fluege.de/flight/wait/?sFlightInput%5BaccDepRegion%5D=&sFlightInput%5BaccArrRegion%5D=&sFlightInput%5BaccDep%5D="
                + accDep
                + "&sFlightInput%5BaccArr%5D="
                + accArr
                + "&sFlightInput%5BsortBy%5D=&sFlightInput%5Bmode%5D="
                + mode
                + "&sFlightInput%5Bpage%5D=&sFlightInput%5BflightType%5D="
                + flightType
                + "&sFlightInput%5BdepCity%5D="
                + depCity
                + "&sFlightInput%5BarrCity%5D="
                + arrCity
                + "&sFlightInput%5BdepartureDate%5D="
                + departureDate
                + "&sFlightInput%5BreturnDate%5D="
                + returnDate
                + "&sFlightInput%5BpaxAdt%5D="
                + paxAdt
                + "&sFlightInput%5BpaxChd%5D="
                + paxChd
                + "&sFlightInput%5BpaxInf%5D="
                + paxInf
                + "&sFlightInput%5BdepartureTimeRange%5D="
                + departureTimeRange
                + "&sFlightInput%5BreturnTimeRange%5D="
                + returnTimeRange
                + "&sFlightInput%5BdepAirline%5D="
                + depAirline
                + "&sFlightInput%5BcabinClass%5D="
                + (cabinClass ? "Y" : "N")
                + "&sFlightInput%5BnonStop%5D=" + (nonStop ? "TRUE" : "FALSE");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    helper.openUrlInputStream(url))));
            resultPage = null;
            String line;
            while (null != (line = reader.readLine())) {
                if (line.contains("location.href = '")) {
                    resultPage = Helper.substringBetween(line, "location.href = '", "'");
                }
            }
            reader.close();
            System.out.println("Result page: " + resultPage);
            if (resultPage == null) {
                throw new RuntimeException("No result page found!");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void tryAndRun(String task, Helper helper, Runnable runnable) {
        int failed = 0;
        boolean success = false;
        while (!success && failed++ < RETRIES) {
            try {
                runnable.run();
                success = true;
            } catch (Exception e) {
                System.err.println("Task '" + task + "' failed: " + e.toString());
                try {
                    Thread.sleep(1000 + failed * 500);
                } catch (InterruptedException e1) {
                }
            }
        }
        if (!success) {
            throw new RuntimeException("Task '" + task + "' failed after " + RETRIES + " retries!");
        }
    }

    private void openStartPage(Helper helper) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    helper.openUrlInputStream("http://www.fluege.de"))));
            reader.close();
            String cookie = helper.getHeader("Cookie");
            // System.out.println("Cookie: " + cookie);
            if (!cookie.contains("fluegedeSt")) {
                throw new RuntimeException("fluegedeSt not found in cookie!");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
