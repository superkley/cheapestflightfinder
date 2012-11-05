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

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Flight {
    private static String fill(int i) {
        if (i < 10) {
            return "0" + i;
        } else {
            return String.valueOf(i);
        }
    }
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

    private String resultPage;

    private String searchPage;

    public Flight(GregorianCalendar start, GregorianCalendar stop) {
        this.departureDate = start;
        this.returnDate = stop;
    }

    public String getAccArr() {
        return this.accArr;
    }

    public String getAccDep() {
        return this.accDep;
    }

    private String getCalendarString(GregorianCalendar calendar) {
        if (this.returnDate != null) {
            return fill(calendar.get(Calendar.DAY_OF_MONTH)) + "." + fill(calendar.get(Calendar.MONTH) + 1) + "."
                    + calendar.get(Calendar.YEAR);
        } else {
            return "";
        }
    }

    public String getDepAirline() {
        return this.depAirline;
    }

    public GregorianCalendar getDepartureDate() {
        return this.departureDate;
    }

    public String getDepartureDateAsString() {
        return getCalendarString(this.departureDate);
    }

    public String getDepDuration() {
        return this.depDuration;
    }

    public String getFlightType() {
        return this.flightType;
    }

    public String getMode() {
        return this.mode;
    }

    public String getPrice() {
        return this.price;
    }

    public double getPriceValue() {
        return this.priceValue;
    }

    public String getResultPage() {
        return this.resultPage;
    }

    public String getRetAirline() {
        return this.retAirline;
    }

    public String getRetDuration() {
        return this.retDuration;
    }

    public GregorianCalendar getReturnDate() {
        return this.returnDate;
    }

    public String getReturnDateAsString() {
        if (this.flightType.endsWith("OW")) {
            return "";
        } else {
            return getCalendarString(this.returnDate);
        }
    }

    public String getSearchPage() {
        return searchPage;
    }

    public boolean isCabinClass() {
        return this.cabinClass;
    }

    public boolean isNonStop() {
        return this.nonStop;
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
            this.priceValue = Double.parseDouble(price);
        } catch (NumberFormatException e) {
            // ignore
        }
    }

    public void setPriceValue(double priceValue) {
        this.priceValue = priceValue;
    }

    public void setResultPage(String resultPage) {
        this.resultPage = resultPage;
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

    public void setSearchPage(String searchPage) {
        this.searchPage = searchPage;
    }

    @Override
    public String toString() {
        return "[" + getDepartureDateAsString() + ", " + getReturnDateAsString() + "] " + this.price + " ("
                + this.depDuration + " / " + this.depAirline + ", " + this.retDuration + " / " + this.retAirline + ")";
    }
}
