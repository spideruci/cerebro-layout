package org.spideruci.cerebro.layout;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;

import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.swingViewer.ViewerListener;
import org.graphstream.ui.swingViewer.ViewerPipe;
import org.graphstream.ui.swingViewer.util.Camera;
import org.graphstream.ui.swingViewer.util.DefaultMouseManager;
import org.spideruci.cerebro.layout.model.DynamicFlowGraph;
import org.spideruci.cerebro.layout.model.SourceLineNode;

public class ViewerEventListener extends DefaultMouseManager implements ViewerListener, MouseWheelListener {

	private static final double MIN_VIEW_PERCENT = 0.2;
	private static final double MAX_VIEW_PERCENT = 4;
	private static final double VIEW_PERCENT_STEP = 0.1;

	private final ViewerPipe eventSource2;
	private final DynamicFlowGraph displayedGraph;
	private Point3 previousPoint;

	public ViewerEventListener(ViewerPipe pipe, DynamicFlowGraph displayedGraph) {
		this.eventSource2 = pipe;
		this.displayedGraph = displayedGraph;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double rotation = e.getPreciseWheelRotation();

		Camera cam = view.getCamera();
		// +ve is zoom out
		// -ve is zoom in.
		double viewPercent = cam.getViewPercent();
		if(rotation < 0) { // zoom in
			viewPercent = viewPercent - VIEW_PERCENT_STEP;
			viewPercent =
					(viewPercent <= MIN_VIEW_PERCENT) ? MIN_VIEW_PERCENT 
							: viewPercent;
		} else { // zoom out
			viewPercent = viewPercent + VIEW_PERCENT_STEP;
			viewPercent = 
					(viewPercent >= MAX_VIEW_PERCENT) ? MAX_VIEW_PERCENT 
							: viewPercent;
		}
		cam.setViewPercent(viewPercent);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		eventSource2.pump();

		if(e.isControlDown()) {
			centerOn(e.getPoint());
		}
	}

	private void centerOn(Point point) {
		Camera camera = view.getCamera();
		int x = point.x;
		int y = point.y;
		Point3 newCenter = camera.transformPxToGu(x, y);
		camera.setViewCenter(newCenter.x, newCenter.y, 0);
		previousPoint = newCenter;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		previousPoint = point3(e.getPoint());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		boolean isShiftPressed = e.isShiftDown();
		if(!isShiftPressed) {
			curElement = view.findNodeOrSpriteAt(e.getX(), e.getY());
			if (curElement != null) {
				mouseButtonPressOnElement(curElement, e);
			}

			previousPoint = point3(e.getPoint());
		}

		curElement = view.findNodeOrSpriteAt(e.getX(), e.getY());

		if (curElement != null) {
			mouseButtonPressOnElement(curElement, e);
		} else {
			x1 = e.getX();
			y1 = e.getY();
			mouseButtonPress(e);
			view.beginSelectionAt(x1, y1);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		boolean isShiftPressed = e.isShiftDown();
		if(!isShiftPressed) {
			if (curElement != null) {
				mouseButtonReleaseOffElement(curElement, e);
				curElement = null;
			}
			return; 
		}

		if (curElement != null) {
			mouseButtonReleaseOffElement(curElement, e);
			curElement = null;
		} else {
			float x2 = e.getX();
			float y2 = e.getY();
			float t;

			if (x1 > x2) {
				t = x1;
				x1 = x2;
				x2 = t;
			}
			if (y1 > y2) {
				t = y1;
				y1 = y2;
				y2 = t;
			}

			mouseButtonRelease(e, view.allNodesOrSpritesIn(x1, y1, x2, y2));
			view.endSelectionAt(x2, y2);
		}

		boolean nodesWereSelected = false;
		System.out.println("## Selection of Nodes is ...");

		HashMap<String, Integer> freq = new HashMap<>();
		for(Node node : graph) {
			if(node.hasAttribute("ui.selected")) {
				nodesWereSelected = true;
				String nodeId = node.toString();
				if(displayedGraph != null) {
					SourceLineNode lineNode = displayedGraph.getNode(Integer.parseInt(nodeId));
					String sourceLine = lineNode.toString();
					if(!freq.containsKey(sourceLine)) {
						freq.put(sourceLine, 0);
					}

					int f = freq.get(sourceLine);
					freq.put(sourceLine, f + 1);
					//          System.out.println(sourceLine);
				} else {
					System.out.println(nodeId);
				}
			}
		}

		for(String line : freq.keySet()) {
			System.out.println(line +  " " + freq.get(line));
		}

		if(!nodesWereSelected) {
			System.out.println("EMPTY.");
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		boolean isShiftPressed = e.isShiftDown();
		if(!isShiftPressed) {
			if (curElement != null) {
				elementMoving(curElement, e);
			}
			Point3 currentPoint = point3(e.getPoint()); 
			double xDelta = (currentPoint.x - previousPoint.x) / 1.5;
			double yDelta = (currentPoint.y - previousPoint.y) / 1.5;
			pan(xDelta, yDelta);
			previousPoint = currentPoint;
		} else {
			if (curElement != null) {
				elementMoving(curElement, e);
			} else {
				view.selectionGrowsAt(e.getX(), e.getY());

				//        System.out.println(e.getPoint().toString());
			}
		}
	}

	private void pan(double xDelta, double yDelta) {
		Camera camera = view.getCamera();
		Point3 point = camera.getViewCenter();
		double x = point.x - xDelta;
		double y = point.y - yDelta;
		camera.setViewCenter(x, y, 0);
	}



	private Point3 point3(Point point) {
		Camera camera = view.getCamera();
		return camera.transformPxToGu(point.x, point.y);
	}


	@Override
	public void buttonPushed(String id) { 
		String nodeId = id;
		
		System.out.println(nodeId);
		try {
			SourceLineNode node = displayedGraph.getNode(Integer.parseInt(nodeId));
			System.out.print(node.toString() + "   Color: " + node.getColorString());
			
			if(displayedGraph.suspiciousness){
				System.out.print("   Suspiciousness: " + node.getSuspiciousness() + "   Confidence: " + node.getConfidence());
			}
			
			if (displayedGraph.author) {
				System.out.print("   Author: " + node.getAuthor());
			}
			
			System.out.println();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("OKAY! Something went wrong." +
					"\nFeel free to report this exception." +
					"\nContinue using the command menu... ");
			System.out.println(Commands.mainMenu);
		}

	}

	@Override
	public void buttonReleased(String id) {

	}

	@Override
	public void viewClosed(String viewName) { }
}
