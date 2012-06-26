/*  Copyright (c) 2012 Xiaoyun Zhu
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy  
 *  of this software and associated documentation files (the "Software"), to deal  
 *  in the Software without restriction, including without limitation the rights  
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
 *  copies of the Software, and to permit persons to whom the Software is  
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in  
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN  
 *  THE SOFTWARE.  
 */
package cn.kk.cheapestflightfinder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class CheapestFlightFinder {
    private static final int MAX_CONNECTIONS = 10;
    private static Main MAIN;
    private static ExecutorService EXECUTOR = Executors.newFixedThreadPool(CheapestFlightFinder.MAX_CONNECTIONS);
    private static Semaphore LOCK;
    private static final int RETRIES = 2;
    private static final boolean DEBUG = true;

    public static void main(String[] args) throws InterruptedException, IOException {
        GregorianCalendar startDate = new GregorianCalendar(2012, 6, 20);
        GregorianCalendar endDate = new GregorianCalendar(2012, 9, 3);
        int minDays = 20;
        int maxDays = 40;

        CheapestFlightFinder.search("FRA", "PVG", startDate, endDate, minDays, maxDays, true, true, true);
    }

    public static void search(String from, String to, Calendar startDate, Calendar endDate, int minDays, int maxDays,
            boolean nonStop, boolean economy, boolean back) throws InterruptedException, IOException {
        List<Flight> possibleFlights = new LinkedList<Flight>();
        GregorianCalendar limit = (GregorianCalendar) startDate.clone();
        if (back) {
            limit.add(Calendar.DAY_OF_YEAR, minDays);
        }
        // System.out.println(limit.getTime());
        while (endDate.after(limit)) {
            if (back) {
                for (int days = minDays; days <= maxDays; days++) {
                    GregorianCalendar start = (GregorianCalendar) startDate.clone();
                    GregorianCalendar stop = (GregorianCalendar) startDate.clone();
                    stop.add(Calendar.DAY_OF_YEAR, days);
                    if (stop.after(endDate)) {
                        break;
                    }
                    final Flight flight = new Flight(start, stop);
                    flight.setAccArr(from);
                    flight.setAccDep(to);
                    flight.setNonStop(nonStop);
                    flight.setCabinClass(economy);
                    flight.setFlightType(back ? "RT" : "OW");
                    possibleFlights.add(flight);
                }
            } else {
                GregorianCalendar start = (GregorianCalendar) startDate.clone();
                GregorianCalendar stop = (GregorianCalendar) startDate.clone();
                stop.add(Calendar.DAY_OF_YEAR, 7);
                final Flight flight = new Flight(start, stop);
                flight.setAccArr(from);
                flight.setAccDep(to);
                flight.setNonStop(nonStop);
                flight.setCabinClass(economy);
                flight.setFlightType(back ? "RT" : "OW");
                possibleFlights.add(flight);
            }
            startDate.add(Calendar.DAY_OF_YEAR, 1);
            limit = (GregorianCalendar) startDate.clone();
            if (back) {
                limit.add(Calendar.DAY_OF_YEAR, minDays);
            }
        }

        final int size = possibleFlights.size();
        System.out.println(from + "->" + to + " - 共查询总数：" + size);
        CheapestFlightFinder.LOCK = new Semaphore(size);
        CheapestFlightFinder.LOCK.acquire(size);

        for (final Flight flight : possibleFlights) {
            CheapestFlightFinder.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        new CheapestFlightFinder().start(flight);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    } finally {
                        if (Helper.isNotEmptyOrNull(flight.getPrice())) {
                            CheapestFlightFinder.MAIN.onFlightFound(flight);
                        }
                        CheapestFlightFinder.LOCK.release();
                    }
                }
            });
        }

        CheapestFlightFinder.LOCK.acquire(size);
        // es.awaitTermination(1, TimeUnit.SECONDS);
        //
        // Collections.sort(possibleFlights, new Comparator<Flight>() {
        // @Override
        // public int compare(Flight o1, Flight o2) {
        // double thisVal = o1.getPriceValue();
        // double anotherVal = o2.getPriceValue();
        // return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
        // }
        // });

        // for (int i = 0; i < Math.min(10, size); i++) {
        // System.out.println(i + ": " + possibleFlights.get(i));
        // }
        //
        // BufferedWriter writer = new BufferedWriter(new FileWriter("F:\\cheapestflights.txt"));
        // for (Flight f : possibleFlights) {
        // writer.write(f.toString());
        // writer.write("\r\n");
        // }
        // writer.close();
    }

    public void start(final Flight flight) {
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
                flight.setResultPage(searchPrice(flight, helper, flight.getAccDep(), flight.getAccArr(),
                        flight.getMode(), flight.getFlightType(), flight.getDepartureDateAsString(),
                        flight.getReturnDateAsString(), flight.isCabinClass(), flight.isNonStop()));
            }
        });

        tryAndRun("read results", helper, new Runnable() {
            @Override
            public void run() {
                readResults(helper, flight);
            }
        });

        // System.err.println(flight.toString());
    }

    private void readResults(final Helper helper, final Flight flight) {
        if (flight.getResultPage() != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                        helper.openUrlInputStream(flight.getResultPage()))));
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
                        if (line.contains("ckflug")) {
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
                    // throw new RuntimeException("No price found for current parameters!");
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private String searchPrice(Flight flight, Helper helper, String accDep, String accArr, String mode,
            String flightType, String departureDate, String returnDate, boolean cabinClass, boolean nonStop) {
        String depCity = accDep;// URLEncoder.encode("Frankfurt am Main - Alle Flughäfen (FRA) - Deutschland", "UTF-8");
        String arrCity = accArr; // URLEncoder.encode("Shanghai - Alle Flughäfen (SHA) - China", "UTF-8");
        String paxAdt = "1";
        String paxChd = "0";
        String paxInf = "0";
        String departureTimeRange = ""; // ,1,2
        String returnTimeRange = ""; // ,1,2
        String depAirline = ""; // e.g. CI

        final String url = "http://www.fluege.de/flight/wait/?sFlightInput%5BaccDepRegion%5D=&sFlightInput%5BaccArrRegion%5D=&sFlightInput%5BaccDep%5D="
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
                + (cabinClass ? "Y" : "C")
                + "&sFlightInput%5BnonStop%5D=" + (nonStop ? "TRUE" : "FALSE");

        flight.setSearchPage(url);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    helper.openUrlInputStream(url))));
            String resultPage = null;
            String line;
            while (null != (line = reader.readLine())) {
                if (line.contains("location.href = '")) {
                    resultPage = Helper.substringBetween(line, "location.href = '", "'");
                }
            }
            reader.close();
            // System.out.println("Result page: " + this.resultPage);
            if (resultPage == null) {
                throw new RuntimeException("No result page found!");
            }
            return resultPage;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void tryAndRun(String task, Helper helper, Runnable runnable) {
        int failed = 0;
        boolean success = false;
        while (!success && (failed++ < CheapestFlightFinder.RETRIES)) {
            if (Thread.interrupted()) {
                throw new RuntimeException("Task interrupted!");
            }
            try {
                runnable.run();
                success = true;
            } catch (Exception e) {
                System.err.println("Task '" + task + "' failed: " + e.toString());
                if (CheapestFlightFinder.DEBUG) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000 + (failed * 500));
                } catch (InterruptedException e1) {
                }
            }
        }
        if (!success) {
            throw new RuntimeException("Task '" + task + "' failed after " + CheapestFlightFinder.RETRIES + " retries!");
        }
    }

    private void openStartPage(Helper helper) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                    helper.openUrlInputStream("http://www.fluege.de"))));
            reader.close();
            String cookie = helper.getHeader("Cookie");

            if (cookie == null) {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void setMain(Main main) {
        CheapestFlightFinder.MAIN = main;
    }

    public static boolean cancelSearch() {
        List<Runnable> queued = CheapestFlightFinder.EXECUTOR.shutdownNow();
        while (!CheapestFlightFinder.EXECUTOR.isTerminated()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
            }
        }
        CheapestFlightFinder.LOCK.release(queued.size());
        CheapestFlightFinder.EXECUTOR = Executors.newFixedThreadPool(CheapestFlightFinder.MAX_CONNECTIONS);
        return true;
    }
}
