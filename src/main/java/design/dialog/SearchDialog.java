package design.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import design.E3Graph;
import design.Main;
import design.Utils;
import design.info.Actor;
import design.info.Base;
import design.info.EndSignal;
import design.info.LogicBase;
import design.info.MarketSegment;
import design.info.Note;
import design.info.StartSignal;
import design.info.ValueActivity;
import design.info.ValueExchange;
import design.info.ValueInterface;
import design.info.ValuePort;
import net.miginfocom.swing.MigLayout;
import java.awt.Font;

public class SearchDialog extends JDialog {

	private static final long serialVersionUID = 3947607765814853010L;

	public static class SearchHit {
		String objectType;
		long SUID;
		String description;
		int padding;
		
		SearchHit(String objectType, long SUID, String description) {
			this.objectType = objectType;
			this.SUID = SUID;
			this.description = description;
			this.padding = 0;
		}
		
		public long getSUID() {
			return SUID;
		}
		
		public String getType() {
			return objectType;
		}
		
		public String getDescription() {
			return description;
		}
		
		public void setTagWidth(int w) {
			padding = w - ("[" + objectType + "]").length();
		}
		
		public int getBareTagWidth() {
			return objectType.length() + 2;
		}
		
		@Override
		public String toString() {
			return "[" + objectType + "] " + description;
		}
	}
	
	// @Incomplete make sure this variable is properly set
	public static boolean isOpen = false;
	public static SearchDialog dialogInstance = null;

	private JTextField suidField;
	private JTextField textField;
	private E3Graph graph;
	private DefaultListModel<SearchHit> listModel;
	private JList<SearchHit> resultsList;

	private Map<String, Class<?>> stringToObjectClass;
	private JComboBox<String> filterType;
	
	public SearchDialog(Main main) {
		// @Incomplete add bindings for switching tabs
		
		this.graph = main.getCurrentGraph();
		
		stringToObjectClass = new HashMap<>();
		Map<String, Class<?>> s = stringToObjectClass;

		s.put("Actor", Actor.class);
		s.put("Market Segment", MarketSegment.class);
		s.put("Value Activity", ValueActivity.class);
		s.put("Start Stimulus", StartSignal.class);
		s.put("End Stimulus", EndSignal.class);
		s.put("Value Exchange", ValueExchange.class);
		s.put("Value Interface", ValueInterface.class);
		s.put("Value Port", ValuePort.class);
		s.put("Note", Note.class);
		
		/////////////////////
		// Building dialog //
		/////////////////////
		
		JPanel selectorPanel = new JPanel();
		getContentPane().add(selectorPanel, BorderLayout.NORTH);
		selectorPanel.setLayout(new MigLayout("", "[][grow][][]", "[][]"));
		
		JLabel suidLabel = new JLabel("SUID");
		selectorPanel.add(suidLabel, "cell 0 0,alignx left,aligny center");
		
		JLabel textLabel = new JLabel("Text");
		selectorPanel.add(textLabel, "cell 1 0,alignx left,aligny center");
		
		JLabel typeLabel = new JLabel("Filter");
		selectorPanel.add(typeLabel, "cell 2 0,alignx left,aligny center");
		
		suidField = new JTextField();
		selectorPanel.add(suidField, "cell 0 1,growx,aligny center");
		suidField.setColumns(10);
		
		textField = new JTextField();
		textField.setColumns(10);
		selectorPanel.add(textField, "cell 1 1,growx,aligny center");
		
		Utils.addChangeListener(textField, e -> {
			updateSearch();
		});
		
		filterType = new JComboBox<>();
		filterType.setModel(new DefaultComboBoxModel<String>(new String[] {"", "Actor", "Market Segment", "Value Activity", "Start Stimulus", "End Stimulus", "AND", "OR", "Value Exchange", "Value Interface", "Value Port", "Note", "Formula", "Formulavalue"}));
		selectorPanel.add(filterType, "cell 2 1,alignx left,aligny center");
		
		JButton searchButton = new JButton("Search");
		selectorPanel.add(searchButton, "cell 3 1");
		
		JPanel resultsPanel = new JPanel();
		getContentPane().add(resultsPanel, BorderLayout.CENTER);
		resultsPanel.setLayout(new BorderLayout(0, 0));
		
		listModel = new DefaultListModel<>();
		
		resultsList = new JList<>(listModel);
		resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JScrollPane sp = new JScrollPane(resultsList);
		resultsPanel.add(sp);
		
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, (Color) new Color(192, 192, 192)), new EmptyBorder(2, 2, 2, 2)));
		getContentPane().add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel statusLabel = new JLabel("0 matches found");
		statusPanel.add(statusLabel);
		
		//////////////////////////////
		// Finished building dialog //
		//////////////////////////////
		
		resultsList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				// Only continue if the list is stable, i.e. not inbetween switching selections
				if (arg0.getValueIsAdjusting()) return;
				
				int selectedIndex = resultsList.getSelectedIndex();
				if (selectedIndex == -1) return;
				
				SearchHit sh = listModel.getElementAt(selectedIndex);
				
				Utils.removeHighlight(graph);
				Utils.highlight(graph, sh.getSUID(), "#00FF00");
			}
		});
		
		updateSearch();
		
		setSize(400, 300);
	}
	
	public void updateSearch() {
		listModel.clear();
		
		String filter = (String) filterType.getSelectedItem();
		String text = textField.getText();
		
		List<SearchHit> hits = new ArrayList<>();
		
		// @Incomplete The case that the SUID field is filled it just that element should be shown
		// @Idea if the SUID field is filled the other two should be greyed out, and vice versa for text field
		
		if (filter == "") {
			// Do all of them!!!!

			for (String objType : stringToObjectClass.keySet()) {
				hits.addAll(classicSearch(graph, text, objType, false));
			}
			
			hits.addAll(classicSearch(graph, text, "AND", false));
			hits.addAll(classicSearch(graph, text, "OR", true));
			
			hits.addAll(modernSearch(graph, text, true));
			hits.addAll(modernSearch(graph, text, false));
		} else if (stringToObjectClass.containsKey(filter)) {
			// Classic search

			hits.addAll(classicSearch(graph, text, filter, false));
		} else if (filter.equals("AND") || filter.equals("OR")) {
			// Classic special search

			hits.addAll(classicSearch(graph, text, filter, filter.equals("OR")));
		} else if (filter.equals("Formula") || filter.equals("Formulavalue")) {
			// Modern search

			hits.addAll(modernSearch(graph, text, filter.equals("Formula")));
		} else {
			// Broken search

			System.out.println("ERROR! Filter \"" + filter + "\" is not known/implemented!");
		}
		
//		int width = hits.parallelStream()
//			.map(SearchHit::getBareTagWidth)
//			.reduce(Math::max)
//			.orElse(-1);
//		
//		if (width != -1) {
//			hits.parallelStream()
//				.forEach(s -> s.setTagWidth(width));
//		}
		
		// Readd the results
		for (SearchHit sh : hits) {
			listModel.addElement(sh);
		}
	}
	
	public List<SearchHit> classicSearch(E3Graph graph, String namePart, String infoClassName, boolean isOr) {
		if (!(infoClassName.equals("AND") || infoClassName.equals("OR"))) {
			Class<?> infoClass = stringToObjectClass.get(infoClassName);

			return Utils.getAllCells(graph).stream()
				.map(cell -> graph.getModel().getValue(cell))
				.filter(infoClass::isInstance)
				.map(val -> (Base) val)
				.filter(info -> info.name.contains(namePart))
				.map(info -> new SearchHit(infoClassName, info.SUID, info.name))
				.collect(Collectors.toList());
		} else {
			return Utils.getAllCells(graph).stream()
				.map(cell -> graph.getModel().getValue(cell))
				.filter(val -> val instanceof LogicBase)
				.map(val -> (LogicBase) val)
				.filter(info -> info.isOr == isOr)
				.filter(info -> info.name.contains(namePart))
				.map(info -> new SearchHit(infoClassName, info.SUID, info.name))
				.collect(Collectors.toList());
		}
	}
	
	public List<SearchHit> modernSearch(E3Graph graph, String textPart, boolean lookAtFormula) {
		return Utils.getAllCells(graph).stream()
				.map(cell -> graph.getModel().getValue(cell))
				.filter(val -> val instanceof Base)
				.map(info -> (Base) info)
				.map(info -> {
					Collection<String> vals;

					if (lookAtFormula) {
						vals = info.formulas.keySet();
					} else {
						vals = info.formulas.values();
					}
					
					Optional<String> matches = vals.stream()
						.filter(s -> s.contains(textPart))
						.reduce((l, r) -> l + ", " + r);
					
					if (matches.isPresent()) {
						String matchType = lookAtFormula ? "Formula" : "Formulavalue";
						return Optional.of(new SearchHit(matchType, info.SUID, matches.get()));
					} else {
						// @Hack this is so ugly
						return Optional.<SearchHit>empty();
					}
				})
				.filter(opt -> opt.isPresent())
				.map(opt -> opt.get())
				.collect(Collectors.toList());
	}

}
