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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class Main extends javax.swing.JFrame
{
  private static final long serialVersionUID = 1L;

  private static final String TEXT_WAIT = Messages.getString("main.waiting"); //$NON-NLS-1$

  private static final String TEXT_STOP = Messages.getString("main.cancel"); //$NON-NLS-1$

  private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();

  private static final Color SELECTION_COLOR = new java.awt.Color(146, 173, 247);

  private static final int CURRENT_YEAR;

  private static final int CURRENT_MONTH;

  private static final int CURRENT_DAY;

  static
  {
    Calendar now = Calendar.getInstance();
    now.add(Calendar.DAY_OF_YEAR, 7);
    CURRENT_YEAR = now.get(Calendar.YEAR);
    CURRENT_MONTH = now.get(Calendar.MONTH);
    CURRENT_DAY = now.get(Calendar.DAY_OF_MONTH);
  }


  private static final void checkMaxDays(JComboBox cbYear, JComboBox cbMonth, JComboBox cbDay)
  {
    final int selected = cbDay.getSelectedIndex();
    if (selected > 0)
    {
      cbDay.setSelectedIndex(0);
      Calendar cal = new GregorianCalendar(Main.getYear(cbYear), Main.getMonth(cbMonth), Main.getDay(cbDay));
      final int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
      final int days = cbDay.getItemCount();
      if (days < maxDays)
      {
        for (int i = days + 1; i <= maxDays; i++)
        {
          cbDay.addItem(Main.fill(i));
        }
      } else if (days > maxDays)
      {
        for (int i = days - maxDays; i > 0; i--)
        {
          cbDay.removeItemAt(cbDay.getItemCount() - 1);
        }
      }
      cbDay.setSelectedIndex(Math.min(selected, cbDay.getItemCount() - 1));
    }
  }


  private static final String fill(int i)
  {
    if (i < 10)
    {
      return "0" + i;
    } else
    {
      return String.valueOf(i);
    }
  }


  private static final int getDay(JComboBox cbDay)
  {
    return cbDay.getSelectedIndex() + 1;
  }


  private static final int getMonth(JComboBox cbMonth)
  {
    return cbMonth.getSelectedIndex();
  }


  private static final int getYear(JComboBox cbYear)
  {
    return Main.CURRENT_YEAR + cbYear.getSelectedIndex();
  }


  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    System.out.println(Locale.getDefault());
    try
    {
      UIManager.put("ComboBox.selectionBackground", Main.SELECTION_COLOR);
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e)
    {
      System.err.println(Messages.getString("main.systemError")); //$NON-NLS-1$
    }
    java.awt.EventQueue.invokeLater(new Runnable()
    {

      @Override
      public void run()
      {
        try
        {
          final Main m = new Main();
          m.disableMain();
          m.setVisible(true);
          SwingUtilities.invokeLater(new Runnable()
          {
            @Override
            public void run()
            {
              try
              {
                m.initValues();
              } catch (Exception e)
              {
                e.printStackTrace();
              } finally
              {
                m.reset();
              }
            }
          });
        } catch (IOException e)
        {
          System.err.println(Messages.getString("main.systemError")); //$NON-NLS-1$
        }
      }
    });
  }


  private static void updateLocation(JComboBox cb)
  {
    JTextField tf = (JTextField) cb.getEditor().getEditorComponent();
    String val = tf.getText();
    ComboBoxModel model = cb.getModel();
    final int size = model.getSize();
    int idx = -1;
    for (int i = 0; i < size; i++)
    {
      String test = (String) model.getElementAt(i);
      if (test.toUpperCase().startsWith(val.toUpperCase()))
      {
        idx = i;
        tf.setText(test);
        break;
      }
    }
    if (idx != -1)
    {
      cb.setSelectedIndex(idx);
    }
  }


  // Variables declaration - do not modify
  private javax.swing.JButton btnSave;


  private javax.swing.JButton btnSearch;


  private javax.swing.JComboBox cbDepDay;


  private javax.swing.JComboBox cbDepMonth;


  private javax.swing.JComboBox cbDepYear;


  private javax.swing.JComboBox cbFrom;


  private javax.swing.JComboBox cbReturnDay;

  private javax.swing.JComboBox cbReturnMonth;

  private javax.swing.JComboBox cbReturnYear;

  private javax.swing.JComboBox cbTo;

  private javax.swing.JCheckBox chkDirect;

  private javax.swing.JCheckBox chkEconomy;

  private javax.swing.JCheckBox chkReturn;

  private javax.swing.JLabel lblDep;

  private javax.swing.JLabel lblFrom;

  private javax.swing.JLabel lblMax;

  private javax.swing.JLabel lblMin;

  private javax.swing.JLabel lblReturn;

  private javax.swing.JLabel lblTo;

  private javax.swing.JPanel pnlForm;

  private javax.swing.JPanel pnlProgress;

  private javax.swing.JScrollPane spResults;

  private javax.swing.JTable tblResults;

  private javax.swing.JFormattedTextField tfMax;

  private javax.swing.JFormattedTextField tfMin;

  private JLabel lblProgress;

  /**
   * Creates new form Find
   * 
   * @throws IOException
   */
  public Main() throws IOException
  {
    setIconImage(ImageIO.read(getClass().getResource("/flight.png")));
    initComponents();
    setLocation((Main.SCREEN_SIZE.width - getWidth()) / 2, (Main.SCREEN_SIZE.height - getHeight()) / 2);

    ActionListener daysCorrector = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        Calendar calDep = createCalendarDep();
        Calendar calRet = createCalendarRet();
        int maxDays = (int) (Math.abs(calRet.getTimeInMillis() - calDep.getTimeInMillis()) / 1000 / 60 / 60 / 24);
        tfMax.setValue(Integer.valueOf(maxDays));
        int minDays = Math.max(0, maxDays - 21);
        if (minDays == 0)
        {
          minDays = Math.max(0, maxDays - 14);
          if (minDays == 0)
          {
            minDays = Math.max(0, maxDays - 7);
          }
        }
        tfMin.setValue(Integer.valueOf(minDays));
      }
    };

    this.cbDepDay.addActionListener(daysCorrector);
    this.cbDepMonth.addActionListener(daysCorrector);
    this.cbDepYear.addActionListener(daysCorrector);
    this.cbReturnDay.addActionListener(daysCorrector);
    this.cbReturnMonth.addActionListener(daysCorrector);
    this.cbReturnYear.addActionListener(daysCorrector);

    ((JTextField) this.cbFrom.getEditor().getEditorComponent()).addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_RIGHT)
        {
          updateLocation(Main.this.cbFrom);
        }
      }
    });
    ((JTextField) this.cbFrom.getEditor().getEditorComponent()).addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent e)
      {
        updateLocation(Main.this.cbFrom);
      }
    });
    ((JTextField) this.cbTo.getEditor().getEditorComponent()).addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_RIGHT)
        {
          updateLocation(Main.this.cbTo);
        }
      }
    });
    ((JTextField) this.cbTo.getEditor().getEditorComponent()).addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusLost(FocusEvent e)
      {
        updateLocation(Main.this.cbTo);
      }
    });
  }

  private void btnSaveActionPerformed(ActionEvent evt)
  {
    JFileChooser fc = new JFileChooser();
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fc.setFileFilter(new FileFilter()
    {
      @Override
      public boolean accept(File pathname)
      {
        return pathname.getName().endsWith(".txt");
      }


      @Override
      public String getDescription()
      {
        return Messages.getString("main.exportFile"); //$NON-NLS-1$
      }
    });
    int ret = fc.showSaveDialog(this);
    if (ret == JFileChooser.APPROVE_OPTION)
    {
      File f = fc.getSelectedFile();
      saveResults(f);
    }
  }

  private void btnSearchActionPerformed(java.awt.event.ActionEvent evt)
  {
    if (Main.TEXT_STOP.equals(this.btnSearch.getText()))
    {
      this.btnSearch.setEnabled(false);
      CheapestFlightFinder.cancelSearch();
      reset();
    } else
    {
      try
      {
        disableMain();
        setTitle(Messages.getString("main.title") + " - " + Main.TEXT_WAIT);
        this.pnlProgress.setVisible(true);
        this.btnSearch.setText(Main.TEXT_STOP);
        this.btnSearch.setEnabled(true);

        DefaultTableModel model = ((DefaultTableModel) this.tblResults.getModel());
        while (model.getRowCount() > 0)
        {
          model.removeRow(0);
        }

        new Thread()
        {
          @Override
          public void run()
          {
            try
            {
              Calendar calDep = createCalendarDep();
              Calendar calRet = createCalendarRet();
              CheapestFlightFinder.setMain(Main.this);
              CheapestFlightFinder
                  .search(Helper.substringBetweenLast((String) Main.this.cbFrom.getSelectedItem(), "[", "]"),
                      Helper.substringBetweenLast((String) Main.this.cbTo.getSelectedItem(), "[", "]"), calDep, calRet,
                      Integer.parseInt(Main.this.tfMin.getText()), Integer.parseInt(Main.this.tfMax.getText()),
                      Main.this.chkDirect.isSelected(), Main.this.chkEconomy.isSelected(),
                      Main.this.chkReturn.isSelected());
            } catch (Exception e)
            {
              e.printStackTrace();
            } finally
            {
              try
              {
                SwingUtilities.invokeAndWait(new Runnable()
                {

                  @Override
                  public void run()
                  {
                    reset();
                  }
                });
              } catch (Exception ex)
              {
                reset();
              }
            }
          }

        }.start();
      } catch (Throwable t)
      {
        reset();
      }
    }
  }

  private void cbDepMonthActionPerformed(java.awt.event.ActionEvent evt)
  {
    Main.checkMaxDays(this.cbDepYear, this.cbDepMonth, this.cbDepDay);
  }

  private void cbReturnMonthActionPerformed(java.awt.event.ActionEvent evt)
  {
    Main.checkMaxDays(this.cbReturnYear, this.cbReturnMonth, this.cbReturnDay);
  }

  private void checkReturnState()
  {
    if (this.chkReturn.isSelected())
    {
      this.tfMin.setEnabled(true);
      this.tfMax.setEnabled(true);
      this.lblReturn.setText(Messages.getString("main.latest")); //$NON-NLS-1$
    } else
    {
      this.tfMin.setEnabled(false);
      this.tfMax.setEnabled(false);
      this.lblReturn.setToolTipText(Messages.getString("main.latestDeparture")); //$NON-NLS-1$
    }
  }

  private void chkReturnActionPerformed(java.awt.event.ActionEvent evt)
  {
    checkReturnState();
  }


  // End of variables declaration

  private GregorianCalendar createCalendarDep()
  {
    return new GregorianCalendar(Main.getYear(Main.this.cbDepYear), Main.getMonth(Main.this.cbDepMonth),
        Main.getDay(Main.this.cbDepDay));
  }


  private GregorianCalendar createCalendarRet()
  {
    return new GregorianCalendar(Main.getYear(Main.this.cbReturnYear), Main.getMonth(Main.this.cbReturnMonth),
        Main.getDay(Main.this.cbReturnDay));
  }


  private void disableMain()
  {
    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    this.btnSave.setEnabled(false);
    this.btnSearch.setEnabled(false);
    this.chkReturn.setEnabled(false);
    this.chkDirect.setEnabled(false);
    this.chkEconomy.setEnabled(false);
    this.cbDepDay.setEnabled(false);
    this.cbDepMonth.setEnabled(false);
    this.cbDepYear.setEnabled(false);
    this.cbReturnDay.setEnabled(false);
    this.cbReturnMonth.setEnabled(false);
    this.cbReturnYear.setEnabled(false);
    this.tfMin.setEnabled(false);
    this.tfMax.setEnabled(false);
    this.cbFrom.setEnabled(false);
    this.cbTo.setEnabled(false);
  }


  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">
  private void initComponents()
  {
    this.pnlProgress = new JPanel();
    this.pnlForm = new javax.swing.JPanel();
    this.lblFrom = new javax.swing.JLabel();
    this.cbFrom = new WideComboBox();
    cbFrom.setEditable(true);
    this.lblTo = new javax.swing.JLabel();
    this.cbTo = new WideComboBox();
    cbTo.setEditable(true);
    this.lblDep = new javax.swing.JLabel();
    this.cbDepDay = new WideComboBox();
    this.cbDepMonth = new WideComboBox();
    this.cbDepYear = new WideComboBox();
    this.lblReturn = new javax.swing.JLabel();
    this.cbReturnDay = new WideComboBox();
    this.cbReturnMonth = new WideComboBox();
    this.cbReturnYear = new WideComboBox();
    this.lblMin = new javax.swing.JLabel();
    this.tfMin =
        new javax.swing.JFormattedTextField(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###")));
    this.lblMax = new javax.swing.JLabel();
    this.tfMax =
        new javax.swing.JFormattedTextField(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###")));
    this.chkDirect = new javax.swing.JCheckBox();
    this.chkEconomy = new javax.swing.JCheckBox();
    this.chkReturn = new javax.swing.JCheckBox();
    this.btnSearch = new javax.swing.JButton();
    this.btnSave = new javax.swing.JButton();
    this.spResults = new javax.swing.JScrollPane();
    this.tblResults = new javax.swing.JTable();
    this.lblProgress = new JLabel(Messages.getString("main.progress")); //$NON-NLS-1$

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle(Messages.getString("main.title")); //$NON-NLS-1$
    setBackground(new java.awt.Color(255, 255, 255));

    this.pnlForm.setBackground(new java.awt.Color(207, 245, 255));
    this.pnlForm.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

    this.lblFrom.setLabelFor(this.cbFrom);
    this.lblFrom.setText(Messages.getString("main.origin")); //$NON-NLS-1$
    this.lblFrom.setToolTipText(Messages.getString("main.originAirport")); //$NON-NLS-1$

    this.cbFrom.setModel(new javax.swing.DefaultComboBoxModel(new String[]{}));
    this.cbFrom.setToolTipText(Messages.getString("main.originAirport")); //$NON-NLS-1$
    this.cbFrom.setOpaque(false);

    this.lblTo.setLabelFor(this.cbTo);
    this.lblTo.setText(Messages.getString("main.target")); //$NON-NLS-1$
    this.lblTo.setToolTipText(Messages.getString("main.targetAirport")); //$NON-NLS-1$

    this.cbTo.setModel(new javax.swing.DefaultComboBoxModel(new String[]{}));
    this.cbTo.setToolTipText(Messages.getString("main.targetAirport")); //$NON-NLS-1$
    this.cbTo.setOpaque(false);

    this.lblDep.setLabelFor(this.cbDepDay);
    this.lblDep.setText(Messages.getString("main.earliest")); //$NON-NLS-1$
    this.lblDep.setToolTipText(Messages.getString("main.earliestDeparture")); //$NON-NLS-1$

    this.cbDepDay.setToolTipText(Messages.getString("main.earliestDeparture")); //$NON-NLS-1$
    this.cbDepDay.setOpaque(false);

    this.cbDepMonth.setToolTipText(Messages.getString("main.earliestDeparture")); //$NON-NLS-1$
    this.cbDepMonth.setOpaque(false);
    this.cbDepMonth.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbDepMonthActionPerformed(evt);
      }
    });

    this.cbDepYear.setToolTipText(Messages.getString("main.earliestDeparture")); //$NON-NLS-1$
    this.cbDepYear.setOpaque(false);

    this.lblReturn.setLabelFor(this.cbReturnDay);
    this.lblReturn.setText(Messages.getString("main.latest")); //$NON-NLS-1$
    this.lblReturn.setToolTipText(Messages.getString("main.latestReturn")); //$NON-NLS-1$

    this.cbReturnDay.setToolTipText(Messages.getString("main.latestReturn")); //$NON-NLS-1$
    this.cbReturnDay.setOpaque(false);

    this.cbReturnMonth.setToolTipText(Messages.getString("main.latestReturn")); //$NON-NLS-1$
    this.cbReturnMonth.setOpaque(false);
    this.cbReturnMonth.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbReturnMonthActionPerformed(evt);
      }
    });

    this.cbReturnYear.setToolTipText(Messages.getString("main.latestReturn")); //$NON-NLS-1$
    this.cbReturnYear.setOpaque(false);

    this.lblMin.setText(Messages.getString("main.minStay")); //$NON-NLS-1$
    this.lblMin.setToolTipText(Messages.getString("main.minStayDays")); //$NON-NLS-1$

    this.tfMin.setHorizontalAlignment(SwingConstants.RIGHT);
    this.tfMin.setText("14");
    this.tfMin.setToolTipText(Messages.getString("main.minStayDays")); //$NON-NLS-1$
    this.tfMin.setSelectionColor(getBackground());

    this.lblMax.setText(Messages.getString("main.maxStay")); //$NON-NLS-1$
    this.lblMax.setToolTipText(Messages.getString("main.maxStayDays")); //$NON-NLS-1$

    this.tfMax.setHorizontalAlignment(SwingConstants.RIGHT);
    this.tfMax.setText("45");
    this.tfMax.setToolTipText(Messages.getString("main.maxStayDays")); //$NON-NLS-1$
    this.tfMax.setSelectionColor(this.tfMin.getSelectionColor());

    this.chkDirect.setSelected(true);
    this.chkDirect.setText(Messages.getString("main.direct")); //$NON-NLS-1$
    this.chkDirect.setToolTipText(Messages.getString("main.displayDirectFlightsOnly")); //$NON-NLS-1$
    this.chkDirect.setOpaque(false);

    this.chkEconomy.setSelected(true);
    this.chkEconomy.setText(Messages.getString("main.economy")); //$NON-NLS-1$
    this.chkEconomy.setToolTipText(Messages.getString("main.economy")); //$NON-NLS-1$
    this.chkEconomy.setOpaque(false);

    this.chkReturn.setSelected(true);
    this.chkReturn.setText(Messages.getString("main.roundTrip")); //$NON-NLS-1$
    this.chkReturn.setToolTipText(Messages.getString("main.roundTripTicket")); //$NON-NLS-1$
    this.chkReturn.setOpaque(false);
    this.chkReturn.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        chkReturnActionPerformed(evt);
      }
    });

    this.btnSearch.setText(Messages.getString("main.search")); //$NON-NLS-1$
    this.btnSearch.setToolTipText(Messages.getString("main.startCalculating")); //$NON-NLS-1$
    this.btnSearch.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btnSearchActionPerformed(evt);
      }
    });

    this.btnSave.setText(Messages.getString("main.export")); //$NON-NLS-1$
    this.btnSave.setToolTipText(Messages.getString("main.exportResults")); //$NON-NLS-1$
    this.btnSave.setEnabled(false);
    this.btnSave.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btnSaveActionPerformed(evt);
      }
    });

    GroupLayout pnlFormLayout = new GroupLayout(this.pnlForm);
    this.pnlForm.setLayout(pnlFormLayout);
    pnlFormLayout.setHorizontalGroup(pnlFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
        pnlFormLayout
            .createSequentialGroup()
            .addGroup(
                pnlFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(this.lblFrom, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(this.lblTo, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(
                pnlFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING, true)
                    .addComponent(this.cbFrom, GroupLayout.PREFERRED_SIZE, 280, Short.MAX_VALUE)
                    .addComponent(this.cbTo, GroupLayout.PREFERRED_SIZE, 280, Short.MAX_VALUE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(
                pnlFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(this.lblReturn, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(this.lblDep, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(
                pnlFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(this.cbDepYear, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(this.cbReturnYear, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(
                pnlFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(this.cbDepMonth, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(this.cbReturnMonth, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(
                pnlFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(this.cbDepDay, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(this.cbReturnDay, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(
                pnlFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(this.lblMax, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(this.lblMin, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(
                pnlFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(this.tfMin, GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(this.tfMax, GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(
                pnlFormLayout
                    .createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(
                        pnlFormLayout.createSequentialGroup().addComponent(this.chkEconomy, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(
                        pnlFormLayout.createSequentialGroup().addComponent(this.chkDirect, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(
                pnlFormLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(this.btnSave, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(this.chkReturn, GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(
                pnlFormLayout.createSequentialGroup().addComponent(this.btnSearch, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))

    );
    pnlFormLayout.setVerticalGroup(pnlFormLayout
        .createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(
            pnlFormLayout
                .createSequentialGroup()
                .addGroup(
                    pnlFormLayout
                        .createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(this.lblFrom)
                        .addComponent(this.cbFrom, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE)
                        .addComponent(this.lblDep)
                        .addComponent(this.cbDepDay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE)
                        .addComponent(this.cbDepMonth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE)
                        .addComponent(this.cbDepYear, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE)
                        .addComponent(this.lblMin)
                        .addComponent(this.tfMin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                            GroupLayout.PREFERRED_SIZE).addComponent(this.chkDirect).addComponent(this.chkReturn))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(
                    pnlFormLayout
                        .createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(this.lblTo)
                        .addGroup(
                            pnlFormLayout.createParallelGroup(GroupLayout.Alignment.BASELINE, false)
                                .addComponent(this.lblReturn).addComponent(this.cbReturnDay)
                                .addComponent(this.cbReturnMonth).addComponent(this.cbReturnYear)
                                .addComponent(this.lblMax).addComponent(this.tfMax).addComponent(this.chkEconomy)
                                .addComponent(this.btnSave).addComponent(this.cbTo))))
        .addGroup(
            pnlFormLayout.createSequentialGroup().addComponent(this.btnSearch, GroupLayout.PREFERRED_SIZE,
                GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));

    this.spResults.setOpaque(false);

    this.tblResults.setAutoCreateRowSorter(true);
    this.tblResults
        .setModel(new javax.swing.table.DefaultTableModel(
            new Object[][]{

            },
            new String[]
            {
                Messages.getString("main.price"), Messages.getString("main.departure"), Messages.getString("main.depDuration"), Messages.getString("main.depAirline"), Messages.getString("main.return"), Messages.getString("main.retDuration"), Messages.getString("main.retAirline"), Messages.getString("main.liveUrl") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
            })
        {
          private static final long serialVersionUID = 1L;

          @SuppressWarnings("rawtypes")
          Class[] types = new Class[]
          {java.lang.Double.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
              java.lang.String.class, java.lang.String.class, java.lang.String.class, URL.class};

          boolean[] canEdit = new boolean[]
          {false, false, false, false, false, false, false, false};


          @SuppressWarnings(
          {"rawtypes", "unchecked"})
          @Override
          public Class getColumnClass(int columnIndex)
          {
            return this.types[columnIndex];
          }


          @Override
          public boolean isCellEditable(int rowIndex, int columnIndex)
          {
            return this.canEdit[columnIndex];
          }
        });
    UriCellRenderer renderer = new UriCellRenderer();
    this.tblResults.setDefaultRenderer(URL.class, renderer);
    this.tblResults.addMouseListener(renderer);
    this.tblResults.addMouseMotionListener(renderer);
    // this.tblResults.setCellSelectionEnabled(true);
    this.tblResults.setGridColor(Color.DARK_GRAY);
    this.tblResults.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    TableColumnModel colModel = this.tblResults.getColumnModel();
    colModel.getColumn(0).setPreferredWidth(160);
    colModel.getColumn(1).setPreferredWidth(180);
    colModel.getColumn(2).setPreferredWidth(180);
    colModel.getColumn(3).setPreferredWidth(220);
    colModel.getColumn(4).setPreferredWidth(180);
    colModel.getColumn(5).setPreferredWidth(180);
    colModel.getColumn(6).setPreferredWidth(220);
    colModel.getColumn(7).setPreferredWidth(400);
    // this.tblResults.setSelectionBackground(this.tfMin.getSelectionColor());
    // this.tblResults.setSelectionForeground(new java.awt.Color(0, 153, 102));
    this.spResults.setViewportView(this.tblResults);
    this.spResults.getViewport().setBackground(new Color(0xf0f0fe));

    JLayeredPane layeredPane = new JLayeredPane()
    {
      private static final long serialVersionUID = 1L;


      @Override
      public void setBounds(int x, int y, int width, int height)
      {
        super.setBounds(x, y, width, height);
        updateProgressPanel(width, height);
      }
    };

    GroupLayout layout = new GroupLayout(layeredPane);
    layeredPane.setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.spResults)
        .addComponent(this.pnlForm, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
    layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
        layout
            .createSequentialGroup()
            .addComponent(this.pnlForm, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                GroupLayout.PREFERRED_SIZE)
            .addComponent(this.spResults, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)));

    this.pnlProgress.setVisible(false);
    this.pnlProgress.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, new Color(204, 204, 255),
        new Color(0, 51, 102)));
    this.pnlProgress.setBackground(new Color(0x30f9e0e0));
    this.pnlProgress.add(this.lblProgress);
    layeredPane.add(this.pnlProgress, JLayeredPane.PALETTE_LAYER);
    getContentPane().add(layeredPane);

    setPreferredSize(new Dimension(900, 460));
    setMinimumSize(getPreferredSize());
  }// </editor-fold>


  private void initValues() throws IllegalArgumentException, IOException
  {
    BufferedReader reader = new BufferedReader(new InputStreamReader(Helper.findResourceAsStream("airports.lst")));
    String line;
    while (null != (line = reader.readLine()))
    {
      this.cbFrom.addItem(line);
      this.cbTo.addItem(line);
    }
    reader.close();
    for (int i = 0; i < this.cbFrom.getItemCount(); i++)
    {
      if (((String) this.cbFrom.getItemAt(i)).contains("FRA"))
      {
        this.cbFrom.setSelectedIndex(i);
      }
      if (((String) this.cbFrom.getItemAt(i)).contains("PVG"))
      {
        this.cbTo.setSelectedIndex(i);
      }
    }

    checkReturnState();
    for (int i = Main.CURRENT_YEAR; i < (Main.CURRENT_YEAR + 10); i++)
    {
      this.cbDepYear.addItem(String.valueOf(i));
      this.cbReturnYear.addItem(String.valueOf(i));
    }
    this.cbDepYear.setSelectedIndex(0);
    this.cbReturnYear.setSelectedIndex(0);

    for (int i = 1; i <= 12; i++)
    {
      this.cbDepMonth.addItem(Main.fill(i));
      this.cbReturnMonth.addItem(Main.fill(i));
    }
    this.cbDepMonth.setSelectedIndex(Main.CURRENT_MONTH);
    if (Main.CURRENT_MONTH < 9)
    {
      this.cbReturnMonth.setSelectedIndex(Main.CURRENT_MONTH + 3);
    } else
    {
      this.cbReturnMonth.setSelectedIndex((Main.CURRENT_MONTH + 2) - 11);
      this.cbReturnYear.setSelectedIndex(1);
    }

    for (int i = 1; i <= 31; i++)
    {
      this.cbDepDay.addItem(Main.fill(i));
      this.cbReturnDay.addItem(Main.fill(i));
    }
    this.cbDepDay.setSelectedIndex(Main.CURRENT_DAY - 1);
    this.cbReturnDay.setSelectedIndex(Main.CURRENT_DAY - 1);

    Main.checkMaxDays(this.cbDepYear, this.cbDepMonth, this.cbDepDay);
    Main.checkMaxDays(this.cbReturnYear, this.cbReturnMonth, this.cbReturnDay);

    this.tblResults.getRowSorter().toggleSortOrder(0);
  }


  public void onFlightFound(final Flight flight)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        if (flight != null)
        {
          String url = Helper.chopNull(flight.getSearchPage());
          if (url.isEmpty())
          {
            url = "http://www.fluege.de?k=1";
          }
          try
          {
            ((DefaultTableModel) Main.this.tblResults.getModel()).insertRow(
                0,
                new Object[]
                {Double.valueOf(flight.getPriceValue()), Helper.chopNull(flight.getDepartureDateAsString()),
                    Helper.chopNull(flight.getDepDuration()), Helper.chopNull(flight.getDepAirline()),
                    Helper.chopNull(flight.getReturnDateAsString()), Helper.chopNull(flight.getRetDuration()),
                    Helper.chopNull(flight.getRetAirline()), new URL(url)});
            Main.this.btnSave.setEnabled(true);
          } catch (Exception e)
          {
            e.printStackTrace();
          }
        }
        final int finished = CheapestFlightFinder.LOCK.availablePermits();
        final int total = CheapestFlightFinder.totalFlights;
        final long finishedTime = System.currentTimeMillis() - CheapestFlightFinder.started;
        long estimatedTime = (long) ((finishedTime / (double) finished) * total);
        if (estimatedTime > (1000 * 60 * 60 * 24 * 365))
        {
          estimatedTime = -1;
        }
        Main.this.lblProgress.setText(Messages.getString("main.status") + finished + " / " + total + Messages.getString("main.timeRemaining") //$NON-NLS-1$ //$NON-NLS-3$
            + Helper.formatDuration(estimatedTime));
        updateProgressPanel(getWidth(), getHeight());
      }
    });
  }


  private void reset()
  {
    this.cbFrom.setEnabled(true);
    this.cbTo.setEnabled(true);
    this.chkReturn.setEnabled(true);
    this.chkDirect.setEnabled(true);
    this.chkEconomy.setEnabled(true);
    this.cbDepDay.setEnabled(true);
    this.cbDepMonth.setEnabled(true);
    this.cbDepYear.setEnabled(true);
    this.cbReturnDay.setEnabled(true);
    this.cbReturnMonth.setEnabled(true);
    this.cbReturnYear.setEnabled(true);
    this.tfMin.setEnabled(true);
    this.tfMax.setEnabled(true);
    this.btnSearch.setEnabled(true);
    checkReturnState();
    this.btnSearch.setText(Messages.getString("main.search")); //$NON-NLS-1$
    setTitle(Messages.getString("main.title")); //$NON-NLS-1$
    this.pnlProgress.setVisible(false);
    setCursor(Cursor.getDefaultCursor());
  }


  private void saveResults(final File f)
  {
    try
    {
      try
      {
        FileWriter writer = new FileWriter(f);
        try
        {
          final int rows = Main.this.tblResults.getRowCount();
          final int cols = Main.this.tblResults.getColumnCount();
          for (int i = 0; i < rows; i++)
          {
            for (int j = 0; j < cols; j++)
            {
              writer.write(Main.this.tblResults.getValueAt(i, j).toString());
              writer.write("; ");
            }
            writer.write("\r\n");
          }
        } finally
        {
          writer.close();
        }
      } catch (Exception e)
      {
      }
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }


  private void updateProgressPanel(int width, int height)
  {
    FontMetrics fm = Main.this.lblProgress.getFontMetrics(Main.this.lblProgress.getFont());
    final int pw = SwingUtilities.computeStringWidth(fm, Main.TEXT_WAIT) * 2;
    final int ph = (fm.getHeight()) + 16;
    final int px = (width - pw) / 2;
    final int py = height - (ph * 2);
    Main.this.pnlProgress.setBounds(px, py, pw, ph);
    // System.out.println("px: " + px + ", py: " + py);
  }

}
