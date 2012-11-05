package cn.kk.cheapestflightfinder;

import java.awt.Dimension;

import javax.swing.JComboBox;

public class WideComboBox extends JComboBox {
  private static final long serialVersionUID = 1L;

    public WideComboBox() {
        setMaximumRowCount(20);
    }

    @Override
    public Dimension getSize() {
        final Dimension size = super.getSize();
        final Dimension preferredSize = getPreferredSize();
        return new Dimension(Math.max(preferredSize.width, size.width), size.height);
    }
}
