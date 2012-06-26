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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class AirportsDownloader {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        int page = 1;
        int counter = 0;
        String url = "http://flug.idealo.de/flughafen/?o=3";
        Helper helper = new Helper();
        BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\airports.lst", true));
        try {
            while (true) {
                System.out.println("... " + url);
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
                            helper.openUrlInputStream(url))));
                    String line;
                    int c = 0;
                    while (null != (line = reader.readLine())) {
                        if (line.startsWith("<a class=\"bold\" href=\"http://flug.idealo.de/flughafen/")) {
                            String name = Helper.substringBetweenLast(line, "/\">", "</a>");
                            String country = Helper.substringBetweenLast(line, ", ", ")");
                            String id = Helper.substringBetweenLast(line, "</a> (", ", ");
                            writer.write(country + " / " + name + " [" + id + "]");
                            writer.write('\n');
                            c++;
                        }
                    }
                    if (c == 0) {
                        break;
                    }
                    counter += c;
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                url = "http://flug.idealo.de/flughafen/?o=3&p=" + (++page);
            }
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
