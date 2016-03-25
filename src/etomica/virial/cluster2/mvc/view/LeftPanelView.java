/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.virial.cluster2.mvc.view;

import com.jgoodies.looks.Options;
import com.jgoodies.uif_lite.component.Factory;
import com.jgoodies.uif_lite.panel.SimpleInternalFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LeftPanelView {

  public static JComponent build() {

    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    panel.add(buildVerticalSplit());
    return panel;
  }

  private static JComponent buildVerticalSplit() {

    JSplitPane pane = Factory.createStrippedSplitPane(
        JSplitPane.VERTICAL_SPLIT, buildTopPanel(), buildBottomPanel(), 0.3f);
    pane.setOpaque(false);
    return pane;
  }

  private static JComponent buildTopPanel() {

    SimpleInternalFrame sif = new SimpleInternalFrame("Navigator");
    JScrollPane pane = Factory.createStrippedScrollPane(TreeView.build());
    pane.setBorder(new EmptyBorder(2, 2, 2, 2));
    sif.add(pane);
    sif.setPreferredSize(new Dimension(150, 450));
    return sif;
  }

  private static JComponent buildBottomPanel() {

    SimpleInternalFrame sif = new SimpleInternalFrame("Designer");
    JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
    tabbedPane.putClientProperty(Options.EMBEDDED_TABS_KEY, Boolean.TRUE);
    tabbedPane.addTab("Generation", buildPanel());
    tabbedPane.addTab("Manipulation", buildPanel());
    JScrollPane pane = Factory.createStrippedScrollPane(tabbedPane);
    pane.setBorder(new EmptyBorder(2, 2, 2, 2));
    sif.add(pane);
    sif.setPreferredSize(new Dimension(150, 250));
    return sif;
  }

  private static JComponent buildPanel() {

    JPanel panel = new JPanel();
// panel.setBackground(Color.WHITE);
    return panel;
  }
}