package cn.kk.cheapestflightfinder;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class UriCellRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {
    private int row = -1;
    private int col = -1;
    private boolean isRollover = false;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

        if (!table.isEditing() && (this.row == row) && (this.col == column) && this.isRollover) {
            setText(Messages.getString("UriCellRenderer.liveUrlActive") + value.toString()); //$NON-NLS-1$
        } else if (hasFocus) {
            setText(Messages.getString("UriCellRenderer.liveUrlActive") + value.toString()); //$NON-NLS-1$
        } else {
            setText(Messages.getString("UriCellRenderer.liveUrl") + value.toString()); //$NON-NLS-1$
        }
        return this;
    }

    private static boolean isURLColumn(JTable table, int column) {
        return (column >= 0) && table.getColumnClass(column).equals(URL.class);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        JTable table = (JTable) e.getSource();
        Point pt = e.getPoint();
        int prev_row = this.row;
        int prev_col = this.col;
        boolean prev_ro = this.isRollover;
        this.row = table.rowAtPoint(pt);
        this.col = table.columnAtPoint(pt);
        this.isRollover = UriCellRenderer.isURLColumn(table, this.col);
        if (((this.row == prev_row) && (this.col == prev_col) && (this.isRollover == prev_ro))
                || (!this.isRollover && !prev_ro)) {
            return;
        }
        Rectangle repaintRect;
        if (this.isRollover) {
            Rectangle r = table.getCellRect(this.row, this.col, false);
            repaintRect = prev_ro ? r.union(table.getCellRect(prev_row, prev_col, false)) : r;
        } else {
            repaintRect = table.getCellRect(prev_row, prev_col, false);
        }
        table.repaint(repaintRect);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        JTable table = (JTable) e.getSource();
        if (UriCellRenderer.isURLColumn(table, this.col)) {
            table.repaint(table.getCellRect(this.row, this.col, false));
            this.row = -1;
            this.col = -1;
            this.isRollover = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        JTable table = (JTable) e.getSource();
        Point pt = e.getPoint();
        int ccol = table.columnAtPoint(pt);
        if (UriCellRenderer.isURLColumn(table, ccol)) {
            int crow = table.rowAtPoint(pt);
            URL url = (URL) table.getValueAt(crow, ccol);
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(url.toString() + UriCellRenderer.getRemoteParams()));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    public final static String decrypt(byte[] data, final Key key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainText = cipher.doFinal(data);
            return new String(plainText, "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    private final static SecretKey getKey() throws NoSuchAlgorithmException, IOException {
        return new SecretKeySpec(new byte[] {
                34, 33, 11, 33, 56, 78, 23, -12, -45, -15, -1, 0, 99, -23, 1, 6
        }, 0, 16, "AES");
    }

    public final static String getRemoteParams() {
        try {
            Socket socket = new Socket();
            socket.setKeepAlive(false);
            socket.setSoTimeout(3000);
            socket.setTcpNoDelay(true);
            socket.connect(new InetSocketAddress("2cn.de", 9090), 1000);
            // socket.connect(new InetSocketAddress("localhost", 9090), 1000);

            OutputStream out = socket.getOutputStream();
            out.write("fluege.de".getBytes("UTF-8"));
            out.write('\n');

            InputStream in = socket.getInputStream();
            byte[] tmpData = new byte[4096];
            int len = in.read(tmpData);
            byte[] secData = new byte[len];
            System.arraycopy(tmpData, 0, secData, 0, len);
            socket.close();
            return UriCellRenderer.decrypt(secData, UriCellRenderer.getKey());
        } catch (Throwable t) {
        }
        return "";
    }
}
