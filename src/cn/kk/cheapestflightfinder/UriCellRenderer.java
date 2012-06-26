package cn.kk.cheapestflightfinder;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URI;
import java.net.URL;

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
            setText("<html><font color='red'>即时报价：" + value.toString());
        } else if (hasFocus) {
            setText("<html><font color='red'>即时报价：" + value.toString());
        } else {
            setText("<html><font color='blue'>即时报价：" + value.toString());
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
                    Desktop.getDesktop().browse(new URI(url.toString()));
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

}
