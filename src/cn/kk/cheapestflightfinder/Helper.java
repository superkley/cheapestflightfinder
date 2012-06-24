/*  Copyright (c) 2010 Xiaoyun Zhu
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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Helper {

    public static final int BUFFER_SIZE = 1024 * 1024 * 4;

    private final ByteBuffer IO_BB = ByteBuffer.wrap(new byte[BUFFER_SIZE]);

    public static boolean isEmptyOrNull(String text) {
        return text == null || text.isEmpty();
    }

    public final static boolean isNotEmptyOrNull(String text) {
        return text != null && text.length() > 0;
    }

    public static final String substringBetween(final String text, final String start, final String end) {
        return substringBetween(text, start, end, true);
    }

    public static final String substringBetween(final String text, final String start, final String end,
            final boolean trim) {
        final int nStart = text.indexOf(start);
        final int nEnd = text.indexOf(end, nStart + start.length() + 1);
        if (nStart != -1 && nEnd > nStart) {
            if (trim) {
                return text.substring(nStart + start.length(), nEnd).trim();
            } else {
                return text.substring(nStart + start.length(), nEnd);
            }
        } else {
            return null;
        }
    }

    public static String substringBetweenNarrow(String text, String start, String end) {
        final int nEnd = text.indexOf(end);
        int nStart = -1;
        if (nEnd != -1) {
            nStart = text.lastIndexOf(start, nEnd - 1);
        }
        if (nStart < nEnd && nStart != -1 && nEnd != -1) {
            return text.substring(nStart + start.length(), nEnd);
        } else {
            return null;
        }
    }

    public static final boolean checkUrl(final String link) {
        try {
            URL url = new URL(link);
            URLConnection conn = url.openConnection();
            conn.connect();
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static final boolean checkPort() {
        try {
            InetAddress host = InetAddress.getByName(System.getProperty("http.proxyHost"));
            Socket so = new Socket(host, Integer.parseInt(System.getProperty("http.proxyPort")));
            so.close();
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static final String chopNull(String str) {
        if (str == null) {
            return "";
        } else {
            return str;
        }
    }

    public static final String toString(final boolean[] bools) {
        StringBuffer sb = new StringBuffer(bools.length);
        for (boolean b : bools) {
            sb.append(b ? '1' : '0');
        }
        return sb.toString();
    }

    public static final void fromString(final String boolsString, final boolean[] bools) {
        if (boolsString.length() == bools.length) {
            for (int i = 0; i < bools.length; i++) {
                bools[i] = boolsString.charAt(i) == '1';
            }
        }
    }

    public static final String escapeFileName(final String str) {
        char fileSep = '/';
        char escape = '_';
        final int len = str.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (ch < ' ' || ch >= 0x7F || ch == fileSep || ch == '?' || ch == '*' || ch == '\\' || ch == ':'
                    || ch == '"' || ch == '/' || ch == '+' || ch == '$' || ch == '|' || ch == '&'
                    || (ch == '.' && i == 0) || ch == escape) {
                sb.append(escape);
                if (ch < 0x10) {
                    sb.append('0');
                }
                sb.append(Integer.toHexString(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static final boolean appendCookies(final StringBuffer cookie, final HttpURLConnection conn)
            throws IOException {
        try {
            boolean changed = false;
            List<String> values = conn.getHeaderFields().get("Set-Cookie");
            if (values != null) {
                for (String v : values) {
                    if (v.indexOf("deleted") == -1) {
                        if (cookie.length() > 0) {
                            cookie.append("; ");
                        }
                        cookie.append(v.split(";")[0]);
                        changed = true;
                    }
                }
            }
            return changed;
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    public final HttpURLConnection getUrlConnection(final String url) throws Exception {
        return getUrlConnection(url, false, null);
    }

    public final HttpURLConnection getUrlConnection(final String url, final boolean post, final String output)
            throws IOException {
        int retries = 0;
        HttpURLConnection conn;
        while (true) {
            try {
                URL urlObj = new URL(url);
                conn = (HttpURLConnection) urlObj.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                if (post) {
                    conn.setRequestMethod("POST");
                }
                final String referer;
                final int pathIdx;
                if ((pathIdx = url.lastIndexOf('/')) > "https://".length()) {
                    referer = url.substring(0, pathIdx);
                } else {
                    referer = url;
                }
                conn.setRequestProperty("Referer", referer);
                final Set<String> keys = connectionHeaders.keySet();
                for (String k : keys) {
                    final String value = connectionHeaders.get(k);
                    if (value != null) {
                        conn.setRequestProperty(k, value);
                    }
                }
                // conn.setUseCaches(false);
                if (output != null) {
                    conn.setDoOutput(true);
                    BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());
                    out.write(output.getBytes(CHARSET_UTF8));
                    out.close();
                }
                if (appendCookies(cookie, conn)) {
                    putConnectionHeader("Cookie", cookie.toString());
                }
                break;
            } catch (Throwable e) {
                // 连接中断
                System.err.println(e.toString());
                if (retries++ > 20) {
                    throw new IOException(e);
                } else {
                    try {
                        Thread.sleep(60 * retries * 100 + ((int) Math.random() * 100 * 60 * retries));
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        return conn;
    }

    public final InputStream openUrlInputStream(final String url) throws MalformedURLException, IOException {
        return openUrlInputStream(url, false, null);
    }

    public final InputStream openUrlInputStream(final String url, final boolean post, final String output)
            throws IOException {
        return getUrlConnection(url, post, output).getInputStream();
    }

    public final void putConnectionHeader(final String key, final String value) {
        connectionHeaders.put(key, value);
    }

    public final String getHeader(String key) {
        return connectionHeaders.get(key);
    }

    public final void resetConnectionHeaders() {
        connectionHeaders.clear();
        connectionHeaders.put("User-Agent", userAgent);
        if (Helper.isNotEmptyOrNull(cookieString)) {
            cookie = new StringBuffer(cookieString);
            putConnectionHeader("Cookie", cookie.toString());
        }
        connectionHeaders.put("Cache-Control", "no-cache");
        connectionHeaders.put("Pragma", "no-cache");
    }

    public static String userAgent = "Mozilla/5.0 (Windows NT " + (((int) (Math.random() * 2) + 5)) + ".1) Firefox/"
            + (((int) (Math.random() * 8) + 3)) + "." + (((int) (Math.random() * 6) + 0)) + "."
            + (((int) (Math.random() * 6) + 0));

    public String cookieString = "";

    private final Map<String, String> connectionHeaders = new HashMap<String, String>();

    public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    static {
        System.setProperty("http.keepAlive", "false");
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        HttpURLConnection.setFollowRedirects(false);
    }

    public Helper() {
        resetConnectionHeaders();
    }

    private StringBuffer cookie = new StringBuffer(512);

    public String download(final String url, final String to, boolean overwrite) throws IOException {
        final File toFile = new File(to);
        // 925 -> forbidden file
        if (!overwrite && toFile.exists() && toFile.length() > 0 && toFile.length() != 925) {
            return null;
        } else {
            OutputStream out = null;
            InputStream in = null;
            try {
                out = new BufferedOutputStream(new FileOutputStream(to));
                in = new BufferedInputStream(openUrlInputStream(url));
                Helper.write(in, out);
            } catch (IOException e) {
                new File(to).delete();
                e.printStackTrace();
                System.err.println("错误：" + e.toString());
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return to;
        }
    }

    public static final void write(final InputStream in, final OutputStream out) throws IOException {
        int len;
        byte[] bytes = new byte[1024 * 1024];
        while ((len = in.read(bytes)) > 0) {
            out.write(bytes, 0, len);
        }
    }
}
