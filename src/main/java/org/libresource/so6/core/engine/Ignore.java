/**
 * LibreSource
 * Copyright (C) 2004-2008 Artenum SARL / INRIA
 * http://www.libresource.org - contact@artenum.com
 *
 * This file is part of the LibreSource software, 
 * which can be used and distributed under license conditions.
 * The license conditions are provided in the LICENSE.TXT file 
 * at the root path of the packaging that enclose this file. 
 * More information can be found at 
 * - http://dev.libresource.org/home/license
 *
 * Initial authors :
 *
 * Guillaume Bort / INRIA
 * Francois Charoy / Universite Nancy 2
 * Julien Forest / Artenum
 * Claude Godart / Universite Henry Poincare
 * Florent Jouille / INRIA
 * Sebastien Jourdain / INRIA / Artenum
 * Yves Lerumeur / Artenum
 * Pascal Molli / Universite Henry Poincare
 * Gerald Oster / INRIA
 * Mariarosa Penzi / Artenum
 * Gerard Sookahet / Artenum
 * Raphael Tani / INRIA
 *
 * Contributors :
 *
 * Stephane Bagnier / Artenum
 * Amadou Dia / Artenum-IUP Blois
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package org.libresource.so6.core.engine;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;


public class Ignore {
    public static String ignoreFile = ".ignore.so6";
    private String root;
    private String ignorePath;
    private Vector ignore = new Vector();
    private IgnoreModel ignoreModel = new IgnoreModel();

    public Ignore(String pathReplicateRoot, String ignorePath) {
        try {
            this.root = (new File(pathReplicateRoot)).getAbsolutePath();
            this.ignorePath = ignorePath;
            this.load();
        } catch (Exception ce) {
            Logger.getLogger("ui.log").finest("Ignore:" + ce.getMessage());
        }
    }

    // add a new entry to ignore...
    public void addIgnore(String regexp) {
        ignore.addElement(regexp);

        try {
            this.save();
            ignoreModel.setChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeIgnore(int index) {
        ignore.removeElementAt(index);

        try {
            this.save();
            ignoreModel.setChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean match(File dir, String name) {
        String path = dir.getAbsolutePath() + File.separator + name;
        String rpath = path.substring(path.lastIndexOf(root) + root.length());
        rpath = rpath.replace(File.separatorChar, ':');

        for (Enumeration e = ignore.elements(); e.hasMoreElements();) {
            String ign = (String) e.nextElement();

            if (rpath.matches(ign)) {
                return true;
            }
        }

        return false;
    }

    public boolean match(String path) {
        String localPath = path.replace(File.separatorChar, ':');

        for (Enumeration e = ignore.elements(); e.hasMoreElements();) {
            String ign = (String) e.nextElement();

            if (localPath.matches(ign)) {
                return true;
            }
        }

        return false;
    }

    public void load() throws Exception {
        File f = new File(ignorePath);

        if (!(f.exists())) {
            throw new Exception(f.getPath() + " does not exists");
        }

        try {
            ignore.clear();

            String tmp;
            BufferedReader in = new BufferedReader(new FileReader(ignorePath));

            while ((tmp = in.readLine()) != null)
                ignore.add(tmp);

            in.close();
            in = null;
            f = null;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public void save() throws Exception {
        File f = new File(ignorePath);

        if (f.exists()) {
            if (!(f.delete())) {
                throw new Exception("Cannot delete:" + ignorePath);
            }
        }

        try {
            FileWriter file = new FileWriter(ignorePath);
            Vector v = new Vector(ignore);

            while (!v.isEmpty()) {
                file.write((String) v.remove(0) + "\n");
            }

            file.close();
            file = null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Cannot ignore:" + e.getMessage());
        }
    }

    // UI Part now...
    public JComponent getComponent() {
        return new IgnorePanel();
    }

    public class IgnoreModel extends AbstractListModel {
        public Object getElementAt(int index) {
            return ignore.elementAt(index);
        }

        public void setRemoved() {
            fireIntervalRemoved(this, 0, ignore.size());
        }

        public void setChanged() {
            fireContentsChanged(this, 0, ignore.size());
        }

        public int getSize() {
            return ignore.size();
        }
    }

    class IgnorePanel extends JPanel {
        private JList jlist = new JList(ignoreModel);

        public IgnorePanel() {
            //jlist.setCellRenderer(new MyCellRenderer());
            this.setLayout(new BorderLayout());

            JScrollPane scrollList = new JScrollPane(jlist);
            final JTextField add = new JTextField(25);
            add.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Ignore.this.addIgnore(add.getText());
                    }
                });

            JButton remove = new JButton("Remove");
            remove.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final int index = jlist.getSelectedIndex();

                        if (index >= 0) {
                            Ignore.this.removeIgnore(index);
                        }
                    }
                });

            JPanel control = new JPanel(new FlowLayout());
            control.add(remove);
            this.add(BorderLayout.NORTH, add);
            this.add(BorderLayout.EAST, control);
            this.add(BorderLayout.CENTER, scrollList);
        }

        class MyCellRenderer extends JLabel implements ListCellRenderer {
            private ImageIcon regexpIcon;

            MyCellRenderer() {
                ClassLoader cl = this.getClass().getClassLoader();
                regexpIcon = new ImageIcon(cl.getResource("icons/GreenFlag.gif"));
            }

            // This is the only method defined by ListCellRenderer.
            // We just reconfigure the JLabel each time we're called.
            public java.awt.Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String regexp = (String) value;
                setIcon(regexpIcon);
                setText(regexp);

                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }

                setEnabled(list.isEnabled());
                setFont(list.getFont());
                setOpaque(true);

                return this;
            }
        }
    }
}