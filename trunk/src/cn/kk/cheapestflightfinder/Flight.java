package cn.kk.cheapestflightfinder;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Flight {
    private GregorianCalendar departureDate;
    private GregorianCalendar returnDate;

    private String accDep = "FRA";

    private String accArr = "SHA";

    private String mode = "BP";

    private String flightType = "RT";

    private boolean cabinClass = true;

    private boolean nonStop = true;

    private String price;

    private String depDuration;

    private String retDuration;

    private String depAirline;

    private String retAirline;

    private double priceValue = Integer.MAX_VALUE;

    public Flight(GregorianCalendar start, GregorianCalendar stop) {
        this.departureDate = start;
        this.returnDate = stop;
    }

    public String getAccArr() {
        return accArr;
    }

    public String getAccDep() {
        return accDep;
    }

    public String getDepAirline() {
        return depAirline;
    }

    public GregorianCalendar getDepartureDate() {
        return departureDate;
    }

    public String getDepartureDateAsString() {
        return getCalendarString(departureDate);
    }

    public String getDepDuration() {
        return depDuration;
    }

    public String getFlightType() {
        return flightType;
    }

    public String getMode() {
        return mode;
    }

    public String getPrice() {
        return price;
    }

    public String getRetAirline() {
        return retAirline;
    }

    public String getRetDuration() {
        return retDuration;
    }

    public GregorianCalendar getReturnDate() {
        return returnDate;
    }

    public String getReturnDateAsString() {
        return getCalendarString(returnDate);
    }

    private String getCalendarString(GregorianCalendar calendar) {
        return fill(calendar.get(Calendar.DAY_OF_MONTH)) + "." + fill(calendar.get(Calendar.MONTH) + 1) + "."
                + calendar.get(Calendar.YEAR);
    }

    private String fill(int i) {
        if (i < 10) {
            return "0" + i;
        } else {
            return String.valueOf(i);
        }
    }

    public boolean isCabinClass() {
        return cabinClass;
    }

    public boolean isNonStop() {
        return nonStop;
    }

    public void setAccArr(String accArr) {
        this.accArr = accArr;
    }

    public void setAccDep(String accDep) {
        this.accDep = accDep;
    }

    public void setCabinClass(boolean cabinClass) {
        this.cabinClass = cabinClass;
    }

    public void setDepAirline(String depAirline) {
        this.depAirline = depAirline;
    }

    public void setDepartureDate(GregorianCalendar departureDate) {
        this.departureDate = departureDate;
    }

    public void setDepDuration(String depDuration) {
        this.depDuration = depDuration;
    }

    public void setFlightType(String flightType) {
        this.flightType = flightType;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setNonStop(boolean nonStop) {
        this.nonStop = nonStop;
    }

    public void setPrice(String price) {
        this.price = price;
        try {
            priceValue = Double.parseDouble(price);
        } catch (NumberFormatException e) {
            // ignore
        }
    }

    public void setRetAirline(String retAirline) {
        this.retAirline = retAirline;
    }

    public void setRetDuration(String retDuration) {
        this.retDuration = retDuration;
    }

    public void setReturnDate(GregorianCalendar returnDate) {
        this.returnDate = returnDate;
    }

    @Override
    public String toString() {
        return "[" + getDepartureDateAsString() + ", " + getReturnDateAsString() + "] " + price + " ("
                + depDuration + " / " + depAirline + ", " + retDuration + " / " + retAirline + ")";
    }

    public double getPriceValue() {
        return priceValue;
    }

    public void setPriceValue(double priceValue) {
        this.priceValue = priceValue;
    }
}
