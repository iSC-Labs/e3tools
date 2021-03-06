package design.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.Document;

import com.hp.hpl.jena.rdf.model.Resource;
import com.mxgraph.model.mxGraphModel;

import design.E3Graph;
import design.Utils;
import design.info.Base;
import design.info.ConnectionElement;
import design.info.EndSignal;
import design.info.LogicBase;
import design.info.LogicDot;
import design.info.SignalDot;
import design.info.StartSignal;
import design.info.ValueInterface;
import design.info.ValuePort;
import e3fraud.vocabulary.E3value;

public class ConnectionVisitor {
	RDFExport exporter;
	E3Graph graph;
	mxGraphModel model;
	public Map<Object, Flow> flowMap = new HashMap<>();
	
	ConnectionVisitor(RDFExport exporter) {
		this.exporter = exporter;
		this.graph = exporter.graph;
		this.model = (mxGraphModel) graph.getModel();
	}
	
	void set(Object obj, Flow flowVal) throws MalformedFlowException {
		if (flowMap.containsKey(obj)) {
			Flow realFlowVal = flowMap.get(obj);
			if (realFlowVal != flowVal) {
				Base value = Utils.base(graph, obj);
				throw new MalformedFlowException(
						"Tried setting the flow on an element to " + flowVal.name() + ", while it was already set to " + realFlowVal.name() + ". "
						+ "This usually means an and or or gate has a start or end stimuli on both sides. "
						+ "Name of element in question: " + value.name + ". Type of element: " + value.getClass().getSimpleName(),
						obj);
			}
		} else {
			flowMap.put(obj, flowVal);
		}
	}
	
	void setSend(Object obj) throws MalformedFlowException {
		set(obj, Flow.SEND);
	}

	void setReceive(Object obj) throws MalformedFlowException {
		set(obj, Flow.RECEIVE);
	}
	
	void setBoth(Object obj) throws MalformedFlowException {
		set(obj, Flow.BOTH);
	}
	
	/**
	 * 
	 * @param startSignal The startsignal node in the graph
	 * @throws MalformedFlowException 
	 */
	void accept(Object startSignal) throws MalformedFlowException {
		StartSignal ssInfo = (StartSignal) model.getValue(startSignal);
		visit(startSignal, ssInfo);
	}
	
	void visit(Object ss, StartSignal ssInfo) throws MalformedFlowException {
		//System.out.println("Visiting StartSignal");
		
		Resource res = exporter.getResource(ssInfo.SUID);
		Object child = model.getChildAt(ss, 0);
		
		setSend(child);
		
		if (Utils.EdgeAndSides.hasDotChildEdge(graph, ss)) {
			Object edge = model.getEdgeAt(child, 0);
			ConnectionElement edgeInfo = (ConnectionElement) model.getValue(edge);
			
			Resource edgeRes = exporter.getResource(edgeInfo.SUID);
			res.addProperty(E3value.de_down_ce, edgeRes);
			
			visit(child, edge, edgeInfo);
		}
	}
	
	void visitVIFromCE(Object vi) throws MalformedFlowException {
		ValueInterface viInfo = (ValueInterface) graph.getModel().getValue(vi);
		Resource viRes = exporter.getResource(viInfo.SUID);

//		System.out.println("Visiting #" + viInfo.SUID + " from ce");
		
		Object dot = Utils.getChildrenWithValue(graph, vi, SignalDot.class).get(0);
		Object ce = graph.getEdges(dot)[0];
		ConnectionElement ceInfo = (ConnectionElement) graph.getModel().getValue(ce);
		Resource ceRes = exporter.getResource(ceInfo.SUID);

		viRes.addProperty(E3value.de_up_ce, ceRes);
		
		List<Object> valuePorts = Utils.getChildrenWithValue(graph, vi, ValuePort.class);
		for (Object vp : valuePorts) {
			Object[] edges = graph.getEdges(vp);
			
			boolean visitedVP = flowMap.containsKey(vp);
			
			if (edges.length > 1) {
				setBoth(vp);
			} else {
				setSend(vp);
			}
			
			if (!visitedVP) {
				for (Object ve : graph.getEdges(vp)) {
					Object downVP = Utils.getOpposite(graph, ve, vp);
					if (!flowMap.containsKey(downVP)) {
						Object otherVI = graph.getModel().getParent(downVP);
						
						visitVIFromVE(otherVI, ve, downVP);
					}
				}
			}
		}
	}
	
	void visitVIFromVE(Object vi, Object ve, Object vp) throws MalformedFlowException {
		// TODO: This method might have bad performance in case of looping value exchanges
		// (i.e. value exchanges forming a circle, with one entry point being a signal dot,
		// and an exit point being a signal dot) because of the recursion.
//		{
//			ValueInterface viInfo = (ValueInterface) graph.getModel().getValue(vi);
//			System.out.println("Visiting #" + viInfo.SUID + " from ve");
//		}
		
		// Check if we can travel outwards from the signal dot
		Object dot = Utils.getChildrenWithValue(graph, vi, SignalDot.class).get(0);
		
		boolean visitedDot = flowMap.containsKey(dot);
		if (!visitedDot) {
			if (model.getEdgeCount(dot) == 1) {
				setSend(dot);

				Object edge = model.getEdgeAt(dot, 0);
				ConnectionElement edgeInfo = (ConnectionElement) model.getValue(edge);

				Base viInfo = (Base) model.getValue(vi);
				Resource otherViRes = exporter.getResource(viInfo.SUID);
				otherViRes.addProperty(E3value.de_down_ce, exporter.getResource(edgeInfo.SUID));
				
				visit(dot, edge, edgeInfo);
			}
		}

		// Get the ancestor/upstream value interface
		Object upVI = graph.getModel().getParent(Utils.getOpposite(graph, ve, vp));
		
		// Get all value exchanges outgoing from this value exchange
		// And visit each one that is not the one nor leads to the value interface we came from
		for (Object otherVP : Utils.getChildrenWithValue(graph, vi, ValuePort.class)) {
			for (Object otherVE : graph.getEdges(otherVP)) {
				Object oppositeVP = Utils.getOpposite(graph, otherVE, otherVP);
				Object downVI = graph.getModel().getParent(oppositeVP);

				// Only handle this one if it's not upstream
				if (downVI == upVI) {
					continue;
				}
				
				// Only handle this one if it has not been visited
				// And test if we're still going the right way
				boolean visitedVP = flowMap.containsKey(otherVP);
				if (graph.getModel().getEdgeCount(oppositeVP) > 1) {
					setBoth(oppositeVP);
				} else {
					setReceive(oppositeVP);
				}
				
				if (visitedVP) {
					continue;
				}
				
				visitVIFromVE(downVI, otherVE, oppositeVP);
			}
		}
		
	}
	
	void visit(Object upDot, Object ce, ConnectionElement ceInfo) throws MalformedFlowException {
		//System.out.println("Visiting ConnectionElement");
		
		Resource ceRes = exporter.getResource(ceInfo.SUID);
		
		Object downDot = Utils.getOpposite(graph, ce, upDot);
		Object opposite = model.getParent(downDot);
		Base oppositeValue = (Base) model.getValue(opposite);
		
		Object up = model.getParent(upDot);
		Base upValue = (Base) model.getValue(up);
		
		ceRes.addProperty(E3value.ce_with_up_de, exporter.getResource(upValue.SUID));
		ceRes.addProperty(E3value.ce_with_down_de, exporter.getResource(oppositeValue.SUID));
		
		setSend(upDot);
		setReceive(downDot);
		
		for (Object dot : new Object[]{upDot, downDot}) {
			Base info = Utils.base(graph, dot);
			if (info instanceof LogicDot) {
				LogicDot logicDot = (LogicDot) info;
				if (dot == upDot) {
					ceRes.addProperty(E3value.up_fraction,  "" + logicDot.proportion);
				} else {
					ceRes.addProperty(E3value.down_fraction,  "" + logicDot.proportion);
				}
			}
		}
		
		// System.out.println("Opposite: " + oppositeValue.getClass().getSimpleName());
		
		if (oppositeValue instanceof ValueInterface) {
			visitVIFromCE(opposite);
		} else if (oppositeValue instanceof LogicBase) {
			LogicBase lbInfo = (LogicBase) model.getValue(opposite);
			visit(downDot, opposite, lbInfo);
		} else if (oppositeValue instanceof EndSignal) {
			EndSignal esInfo = (EndSignal) model.getValue(opposite);
			visit(opposite, esInfo);
		} else if (oppositeValue instanceof StartSignal) {
			throw new MalformedFlowException("While checking downstream a start stimuli entity was encountered. "
					+ "This usually means there is a connection to a start stimuli to both ends of an and or or gate. "
					+ "Name of faulty start stimuli: " + oppositeValue.name,
					opposite);
		} else {
			throw new MalformedFlowException("This connectionelement is not connected to a proper ending. Ending type: " +
					oppositeValue.getClass().getSimpleName() +
					". Ending name: " +
					oppositeValue.name
					);
		}
	}
	
	void visit(Object upDot, Object lb, LogicBase lbInfo) throws MalformedFlowException {
		//System.out.println("Visiting logicbase");
		// AND/OR just have de_up_ce and de_down_de's for incoming and outgoing edges.
		
		List<Object> dots = Utils.getChildrenWithValue(graph, lb, LogicDot.class);
		int unitPos = -1;
		
		for (int i = 0; i < dots.size(); i++) {
			LogicDot ld = (LogicDot) model.getValue(dots.get(i));
			if (ld.isUnit) {
				unitPos = i;
				break;
			}
		}
		
		assert(unitPos != -1);
		
		Object unitDot = dots.remove(unitPos);
		
		Resource lbRes = exporter.getResource(lbInfo.SUID);
		
		if (unitDot == upDot) {
			setReceive(upDot);
			
			Object ce = model.getEdgeAt(upDot, 0);
			ConnectionElement ceInfo = (ConnectionElement) model.getValue(ce);
			lbRes.addProperty(E3value.de_up_ce, exporter.getResource(ceInfo.SUID));
			
			for (Object dot : dots) {
				if (flowMap.containsKey(dot)) {
					setSend(dot);
				} else {
					setSend(dot);
					
					if (model.getEdgeCount(dot) == 1) {
						Object edge = model.getEdgeAt(dot, 0);
						ConnectionElement edgeInfo = (ConnectionElement) model.getValue(edge);
						lbRes.addProperty(E3value.de_down_ce, exporter.getResource(edgeInfo.SUID));
						visit(dot, edge, edgeInfo);
					}
				}
			}
		} else {
			if (flowMap.containsKey(unitDot)) {
				setSend(unitDot);
			} else {
				setSend(unitDot);

				if (model.getEdgeCount(unitDot) == 1) {
					Object ce = model.getEdgeAt(unitDot, 0);
					ConnectionElement ceInfo = (ConnectionElement) model.getValue(ce);
					lbRes.addProperty(E3value.de_down_ce, exporter.getResource(ceInfo.SUID));
				
					visit(unitDot, ce, ceInfo);
				}
			}

			for (Object dot : dots) {
				setReceive(dot);

				if (model.getEdgeCount(dot) == 1) {
					Object edge = model.getEdgeAt(dot, 0);
					ConnectionElement edgeInfo = (ConnectionElement) model.getValue(edge);
					lbRes.addProperty(E3value.de_up_ce, exporter.getResource(edgeInfo.SUID));
				}
			}
		}
	}
	
	void visit(Object es, EndSignal esInfo) {
		//System.out.println("Visiting end-signal");
		
		Object edge = model.getEdgeAt(model.getChildAt(es, 0), 0);
		ConnectionElement edgeInfo = (ConnectionElement) model.getValue(edge);
		
		Resource endRes = exporter.getResource(esInfo.SUID);
		endRes.addProperty(E3value.de_up_ce, exporter.getResource(edgeInfo.SUID));
	}
}