/**
 * *****************************************************************************
 * Copyright (C) 2016 Bob Rubbens
 *
 *
 * This file is part of e3tool.
 *
 * e3tool is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * e3tool is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with e3tool.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************
 */
package design.main;

import static design.main.Utils.openWebpage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;

import com.mxgraph.util.mxPoint;

import design.main.Utils.ClosableTabHeading;
import design.main.export.JSONExport;
import design.main.export.RDFExport;
import e3fraud.gui.FraudWindow;
import e3fraud.gui.ProfitabilityAnalyser;
import e3fraud.model.E3Model;
import static design.main.Utils.openWebpage;

public class Main {
    public static final JFrame mainFrame = new JFrame("e3tools editor");
    public static Object contextTarget = null;
    public static mxPoint contextPos = new mxPoint(-1, -1);
    public static int newGraphCounter = 1;
    public static ToolComponent globalTools;
    public static final boolean mirrorMirrorOnTheWallWhoIsTheFairestOfThemAll = true;
    public static final boolean DEBUG = true;
    
    int CHART_WIDTH = 500;
    int CHART_HEIGHT = 400;

    public JTabbedPane views;
	private JToolBar toolbar;

    public E3GraphComponent getCurrentGraphComponent() {
        JSplitPane pane = (JSplitPane) views.getComponentAt(views.getSelectedIndex());
        return (E3GraphComponent) pane.getRightComponent();
    }

    public E3Graph getCurrentGraph() {
        return (E3Graph) getCurrentGraphComponent().getGraph();
    }

    public String getCurrentGraphName() {
        return ((ClosableTabHeading) views.getTabComponentAt(views.getSelectedIndex())).title;
    }

    public void addNewTabAndSwitch(boolean isFraud) {
        addNewTabAndSwitch(new E3Graph(isFraud));
    }

    public void addNewTabAndSwitch(E3Graph graph) {
        addNewTabAndSwitch(graph, null);
    }

    public void addNewTabAndSwitch(E3Graph graph, String title) {
        E3GraphComponent graphComponent = new E3GraphComponent(graph);

        graph.getModel().beginUpdate();
        try {
            // Playground for custom shapes
        } finally {
            graph.getModel().endUpdate();
        }

        graphComponent.refresh();

        // Create split view
        JSplitPane mainpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new ToolComponent(), graphComponent);
        mainpane.setResizeWeight(0.025);

        if (title == null) {
            if (graph.isFraud) {
                title = "New fraud model " + newGraphCounter++;
            } else {
                title = "New value model " + newGraphCounter++;
            }
        }

        if (graph.isFraud) {
            Utils.addClosableTab(views, title, mainpane, IconStore.getImage("/e3f.png", 25, 25));
        } else {
            Utils.addClosableTab(views, title, mainpane, IconStore.getImage("/e3v.png", 25, 25));
        }

        views.setSelectedIndex(views.getTabCount() - 1);
    }

    public void addToolbarButton(String icon, String keyStroke, Runnable action) {
		JButton button = new JButton();
		button.setFocusPainted(false);
		button.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action.run();
			}
		});
		button.setIcon(IconStore.getIcon(icon));
		
		// TODO: Implement keyStroke
		
		toolbar.add(button);
	}

    public Main() {
        // Silly log4j
        Logger.getRootLogger().setLevel(Level.OFF);

        if (mirrorMirrorOnTheWallWhoIsTheFairestOfThemAll) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("Couldn't set Look and Feel to system");
            }
        }

        // Add menubar
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        // TODO: Implement keyboard shortcut
        fileMenu.add(new JMenuItem(new AbstractAction("New e3value model (ctrl+n)") {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewTabAndSwitch(false);
            }
        }));
        // TODO: Implement keyboard shortcut
        // TODO: Implement e3fraud model
        fileMenu.add(new JMenuItem(new AbstractAction("New e3fraud model (ctrl+m)") {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewTabAndSwitch(true);
            }
        }));
        fileMenu.addSeparator();
        // TODO: Implement open functionality
        fileMenu.add(new JMenuItem(new AbstractAction("Open...") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));
        // TODO: Implement save functionality
        // TODO: Implement save shortcut
        fileMenu.add(new JMenuItem(new AbstractAction("Save (ctrl+s)") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));
        // TODO: Implement save functionality
        JMenuItem saveAs = new JMenuItem(new AbstractAction("Save as...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(mainFrame, "Save functionality is not implemented yet", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        fileMenu.add(saveAs);
        fileMenu.addSeparator();

        // TODO: Implement export functionality
        JMenu exportMenu = new JMenu("Export...");

        JMenuItem exportRDF = new JMenuItem(new AbstractAction("RDF") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(new RDFExport(getCurrentGraph()).toString());
            }
        });
        exportMenu.add(exportRDF);
        JMenuItem exportJSON = new JMenuItem(new AbstractAction("JSON") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new JSONExport(getCurrentGraph(), getCurrentGraphName()).generateJSON();
			}
        });
        exportMenu.add(exportJSON);
        JMenuItem exportImage = new JMenuItem(new AbstractAction("Image") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub

            }
        });
        exportMenu.add(exportImage);

        fileMenu.add(exportMenu);

        JMenu importMenu = new JMenu("Import...");

        importMenu.add(new JMenuItem(new AbstractAction("RDF") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));

        importMenu.add(new JMenuItem(new AbstractAction("XSVG") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));

        fileMenu.add(importMenu);

        fileMenu.add(new JMenuItem(new AbstractAction("Print...") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));

        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");

        // TODO: Implement functinonality
        editMenu.add(new JMenuItem(new AbstractAction("Cut (ctrl+x)") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));

        // TODO: Implement functinonality
        editMenu.add(new JMenuItem(new AbstractAction("Copy (ctrl+c)") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));

        // TODO: Implement functinonality
        editMenu.add(new JMenuItem(new AbstractAction("Paste (ctrl+v)") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));

        // TODO: Implement functinonality
        editMenu.add(new JMenuItem(new AbstractAction("Delete (delete)") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));

        // TODO: Implement functinonality
        editMenu.add(new JMenuItem(new AbstractAction("Select all (ctrl+a)") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));

        editMenu.addSeparator();

        editMenu.add(new JMenuItem(new AbstractAction("Undo (ctrl+z)") {
            @Override
            public void actionPerformed(ActionEvent e) {
                getCurrentGraphComponent().undoManager.undo();
            }
        }));

        editMenu.add(new JMenuItem(new AbstractAction("Undo (ctrl+y)") {
            @Override
            public void actionPerformed(ActionEvent e) {
                getCurrentGraphComponent().undoManager.redo();
            }
        }));

        editMenu.addSeparator();

        editMenu.add(new JMenuItem("Find"));

        menuBar.add(editMenu);

        JMenu viewMenu = new JMenu("View");

        viewMenu.add(new JMenuItem(new AbstractAction("Zoom in (ctrl+numpad plus)") {
            @Override
            public void actionPerformed(ActionEvent e) {
                getCurrentGraphComponent().zoomIn();
            }
        }));

        viewMenu.add(new JMenuItem(new AbstractAction("Zoom out (ctrl+numpad minus)") {
            @Override
            public void actionPerformed(ActionEvent e) {
                getCurrentGraphComponent().zoomOut();
            }
        }));

        viewMenu.addSeparator();

        // TODO: Implement grid
        viewMenu.add(new JMenuItem(new AbstractAction("Toggle grid") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));

        viewMenu.addSeparator();

        // TODO: Implement functionality
        viewMenu.add(new JMenuItem("Show all labels"));
        viewMenu.add(new JMenuItem("Hide all labels"));
        viewMenu.add(new JMenuItem("Show all ValueObjects"));
        viewMenu.add(new JMenuItem("Hide all ValueObjects"));

        menuBar.add(viewMenu);

        // TODO: Disable model menu when there's no model
        JMenu modelMenu = new JMenu("Model");
        JMenuItem duplicateGraph = new JMenuItem(new AbstractAction("Create duplicate") {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewTabAndSwitch(new E3Graph(getCurrentGraph()));
            }
        });
        modelMenu.add(duplicateGraph);
        JMenuItem changeType = new JMenuItem(new AbstractAction("Change type") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getCurrentGraph().isFraud) {
                    // TODO: Make this possible
                    JOptionPane.showMessageDialog(
                            Main.mainFrame,
                            "Conversion from fraud to value model is not yet implemented",
                            "Functionality not implemented",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    E3Graph newGraph = new E3Graph(getCurrentGraph());
                    newGraph.isFraud = true;
                    addNewTabAndSwitch(newGraph, "Fraud model of " + getCurrentGraphName());
                }
            }
        });
        modelMenu.add(changeType);
        modelMenu.addSeparator();
        modelMenu.add(new JMenuItem(new AbstractAction("ValueObjects...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Maybe prefer greyed out menu item?
                if (views.getTabCount() == 0) {
                    JOptionPane.showMessageDialog(
                            Main.mainFrame,
                            "A model must be opened to display its ValueObjects. Click File ➡ New model to open a new model.",
                            "No model available",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                new ValueObjectDialog(getCurrentGraph()).show();;
            }
        }));
        // TODO: Implement ValueTransactions
        modelMenu.add(new JMenuItem(new AbstractAction("ValueTransactions...") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));

        // To change "change type" to "convert to fraud" or "convert to value" depending on current graph
        modelMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                if (getCurrentGraph().isFraud) {
                    changeType.setText("Convert to e3value model");
                } else {
                    changeType.setText("Convert to e3fraud model");
                }
            }

            @Override
            public void menuDeselected(MenuEvent e) { }

            @Override
            public void menuCanceled(MenuEvent e) { }
        });

        menuBar.add(modelMenu);

        JMenu toolMenu = new JMenu("Tools");

        // TODO: Implement keyboard shortcut
        // TODO: Implement net value flow
        toolMenu.add(new JMenuItem(new AbstractAction("Net value flow (ctrl+f)...") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));

        // TODO: Implement keyboard shortcut
        toolMenu.add(new JMenuItem(new AbstractAction("Analyze transactions (ctrl+h)...") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }));

        toolMenu.addSeparator();

        toolMenu.add(new JMenuItem(new AbstractAction("Fraud generation...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Convert this option to something greyed out, just like in the file menu?
                if (views.getTabCount() == 0) {
                    JOptionPane.showMessageDialog(
                            Main.mainFrame,
                            "A model must be opened to analyze. Click File ➡ New model to open a new model.",
                            "No model available",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JFrame frame = new JFrame("Fraud analysis of \"" + getCurrentGraphName() + "\"");
                RDFExport rdfExporter = new RDFExport(getCurrentGraph());
                FraudWindow fraudWindowInstance = new FraudWindow(new E3Graph(getCurrentGraph()), new E3Model(rdfExporter.model), Main.this, frame); //, getCurrentGraphName());
                // TODO: Maybe add icons for fraud analysis as well?
                frame.add(fraudWindowInstance);
                frame.pack();
                frame.setVisible(true);
            }
        }));

        toolMenu.add(new JMenuItem(new AbstractAction("Profitability chart...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                RDFExport rdfExporter = new RDFExport(getCurrentGraph());
                JFreeChart chart = ProfitabilityAnalyser.getProfitabilityAnalysis(new E3Model(rdfExporter.model));
                if(chart!=null){
                ChartFrame chartframe1 = new ChartFrame("Profitability analysis of \"" + getCurrentGraphName() + "\"", chart);
                chartframe1.setPreferredSize(new Dimension(CHART_WIDTH, CHART_HEIGHT));
                chartframe1.pack();
                chartframe1.setLocationByPlatform(true);
                chartframe1.setVisible(true);
                }
            }
        }));

        menuBar.add(toolMenu);

        JMenu exampleMenu = new JMenu("Examples");
        
        exampleMenu.add(new JMenuItem(new ExampleModels.SingleTransaction(this)));
        exampleMenu.add(new JMenuItem(new ExampleModels.FlatRateTelephony(this)));
        exampleMenu.add(new JMenuItem(new ExampleModels.LogicGate(this)));
        
        menuBar.add(exampleMenu);

        JMenu helpMenu = new JMenu("Help");

        // TODO: Add shortcuts
        // Find the HelpSet file and create the HelpSet object:
        helpMenu.add(new JMenuItem(new AbstractAction("Help Wiki (F1)") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    openWebpage(new URL("https://github.com/danionita/e3tools/wiki"));
                } catch (MalformedURLException ex) {
                    java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
            }
        }));
        helpMenu.addSeparator();
        helpMenu.add(new JMenuItem(new AbstractAction("e3value website") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    openWebpage(new URL("http://e3value.few.vu.nl"));
                } catch (MalformedURLException ex) {
                    java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
            }
        }));

        helpMenu.addSeparator();
        helpMenu.add(new JMenuItem(new AbstractAction("About...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "The e3tool integrates the e3value value modelling and e-business analysis methodology developed by Jaap Gordjin with the e3fraud fraud assessment methodology developed by Dan Ionita. \n This tool was developed at the University of Twente by Dan Ionita and Bob Rubbens. ");
            }
        }));

        menuBar.add(helpMenu);

        mainFrame.setJMenuBar(menuBar);

        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        // New value
        addToolbarButton("page_green", "control N", () -> {
			addNewTabAndSwitch(false);
        });
        
        // New fraud
        addToolbarButton("page_red", "control M", () -> {
        	addNewTabAndSwitch(true);
        });

        // Close
        addToolbarButton("page_delete", null, () -> {
        	views.remove(views.getSelectedIndex());
        });
        
        // Duplicate
        addToolbarButton("page_copy", null, () -> {
        	addNewTabAndSwitch(new E3Graph(getCurrentGraph()));
        });
        
        // TODO: I don't know what this is (last of the first group)
        // There are a few more below
        addToolbarButton("thumb_up", null, null);

        toolbar.addSeparator();
        
        // Cut
        addToolbarButton("cut", null, () -> {
        	TransferHandler.getCutAction().actionPerformed(new ActionEvent(getCurrentGraphComponent(), -1, null));
        });
        
        // Copy
        addToolbarButton("page_white_copy", null, () -> {
        	TransferHandler.getCopyAction().actionPerformed(new ActionEvent(getCurrentGraphComponent(), -1, null));
        });
        
        // Paste
        addToolbarButton("paste_plain", null, () -> {
        	TransferHandler.getPasteAction().actionPerformed(new ActionEvent(getCurrentGraphComponent(), -1, null));
        });

        toolbar.addSeparator();
        
        // Zoom in
        addToolbarButton("magnifier_zoom_in", null, () -> {
        	getCurrentGraphComponent().zoomIn();
        });
        
        // Zoom out
        addToolbarButton("magnifier_zoom_out", null, () -> {
        	getCurrentGraphComponent().zoomOut();
        });

        toolbar.addSeparator();
        
        // Dunno
        addToolbarButton("thumb_up", null, null);
        
        // Dunno
        addToolbarButton("thumb_up", null, null);
        
        // Dunno
        addToolbarButton("tux", null, null);
        
        // Dunno
        addToolbarButton("thumb_up", null, null);

        toolbar.addSeparator();
        
        // Dunno
        addToolbarButton("thumb_up", null, null);
        
        // Dunno
        addToolbarButton("thumb_up", null, null);
        
        // Dunno
        addToolbarButton("thumb_up", null, null);

        toolbar.addSeparator();
        
        // Help
        addToolbarButton("Help", null, () -> {
			try {
				openWebpage(new URL("https://github.com/danionita/e3tools/wiki"));
			} catch (MalformedURLException ex) {
				java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
			}
        });

        mainFrame.getContentPane().add(toolbar, BorderLayout.PAGE_START);

        globalTools = new ToolComponent();

        views = new JTabbedPane();

        addNewTabAndSwitch(false);

        mainFrame.getContentPane().add(views, BorderLayout.CENTER);

        // Show main screen
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1024, 768);
        mainFrame.setVisible(true);
    }

	public static void main(String[] args) {
        Main t = new Main();
    }
}
