package nettest;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JPanel;

import net.sf.image4j.codec.ico.ICODecoder;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.samples.VertexImageShaperDemo.DemoVertexIconShapeTransformer;
import edu.uci.ics.jung.samples.VertexImageShaperDemo.DemoVertexIconTransformer;
import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class WebGraph {
	private static final int MARGIN = 5;

	private Graph<WebNode,String> graph = new DirectedOrderedSparseMultigraph<WebNode,String>();
	private VisualizationViewer<WebNode,String> _panel;

	Map<WebNode,Icon> iconMap = new HashMap<WebNode,Icon>();

	static private TagWindow _tagWindow = new TagWindow();	//	タグクラウドウィンドウの表示


	//	========プロパティ	BEGIN

	/**
	 * 	エッジのラベルを表示するかどうか
	 */
	public boolean edgeLabelFlag = false;

	/**
	 * 	エッジの色
	 */
	public Color edgeColor = Color.RED;

	//	========プロパティ	END


	WebGraph() {
	}

	public void addNode(WebNode node) {
		graph.addVertex(node);
		Icon icon = null;
		Image img = null;
		String url = node.getWebsite().getIconUrl();
		try {
			if (url != null) {
				URLConnection urlcon = new URL(url).openConnection();
				urlcon.setConnectTimeout(100);	//	タイムアウト設定〔ms〕
				urlcon.setReadTimeout(3000);
				InputStream fileIS = urlcon.getInputStream();
				img = ICODecoder.read(fileIS).get(0);
				fileIS.close();
			}
			icon = new LayeredIcon(img);
		} catch (FileNotFoundException fnfe) { 	//	アイコンが無ければ諦める
		} catch (ConnectException conex) {		//	URL::getInputStream()でのタイムアウト
		} catch (SocketTimeoutException stex) {	//	URL::getInputStream()でのタイムアウト
		} catch (EOFException eofex) {			//	ICODecoder.read()で失敗することがある
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		iconMap.put(node, icon);
	}

	public void addEdge(WebNode node1, WebNode node2) {
		graph.addEdge(""+graph.getEdgeCount(), node1, node2);
	}

	public void layoutGraph(JPanel panel, int width, int height) {
		Layout<WebNode, String> layout = new FRLayout<WebNode, String>(graph);
		layout.setSize(new Dimension(width, height));
		_panel = new VisualizationViewer<WebNode,String>(layout);
		_panel.setPreferredSize(new Dimension(width+MARGIN, height+MARGIN));
		//	エッジが直線になる
		//	_panel.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<String, String>());

		//	ノードの背景色を変更可能にする
		Transformer<WebNode, Paint> nodeColor = new Transformer<WebNode, Paint>() {
			@Override
			public Paint transform(WebNode node) {
				return node.getBackGround();
			}
		};
		_panel.getRenderContext().setVertexFillPaintTransformer(nodeColor);

		setNodeLabelFlag(true);

		//	プロパティの適用	BEGIN

		setEdgeLabelFlag(edgeLabelFlag);

		setEdgeColor(edgeColor);

		//	プロパティの適用	END

		//	アイコンでの表示	BEGIN
		final DemoVertexIconShapeTransformer<WebNode> vertexIconShapeTransformer =
				new DemoVertexIconShapeTransformer<WebNode>(new EllipseVertexShapeTransformer<WebNode>());

		final DemoVertexIconTransformer<WebNode> vertexIconTransformer =
				new DemoVertexIconTransformer<WebNode>();

		vertexIconShapeTransformer.setIconMap(iconMap);
		vertexIconTransformer.setIconMap(iconMap);

		_panel.getRenderContext().setVertexShapeTransformer(vertexIconShapeTransformer);
		_panel.getRenderContext().setVertexIconTransformer(vertexIconTransformer);
		//	アイコンでの表示	END

		//DefaultModalGraphMouse<WebNode, String> gm = new DefaultModalGraphMouse<WebNode, String>();
		//gm.setMode(ModalGraphMouse.Mode.PICKING);
		//_panel.setGraphMouse(gm);
		_panel.setGraphMouse(new GraphMouseEvent<WebNode, String>());

		panel.add(_panel);
		panel.invalidate();
		panel.validate();
		System.out.println("END");
	}

	//	======	ノードの設定系　BEGIN

	/**
	 * ノードのラベルを表示するかどうか
	 * @param isShown	表示する場合はtrue、しない場合はfalse
	 */
	public void setNodeLabelFlag(final boolean isShown) {
		Transformer<WebNode, String> edgeLabeller = new Transformer<WebNode, String>() {
			@Override
			public String transform(WebNode node) {
				return isShown? node.getTitle() : "";
			}
		};
		_panel.getRenderContext().setVertexLabelTransformer(edgeLabeller);
	}

	/**
	 * ノードの形を変更します
	 * @param s	ノードのシェイプ
	 */
	public void setNodeShape(final Shape s) {
		Transformer<WebNode, Shape> nodeShapeTransformer = new Transformer<WebNode, Shape>() {
			@Override
			public Shape transform(WebNode n) {
				return s;
			}
		};
		_panel.getRenderContext().setVertexShapeTransformer(nodeShapeTransformer);
	}

	/**
	 * 全てのノードの背景色を変更します
	 * @param c	背景色
	 */
	public void setAllNodeBackGroundColor(Color c) {
		for (WebNode node : graph.getVertices()) {
			node.setBackGround(c);
		}
	}

	/**
	 * ノードのフォントを変更します。
	 * @param f	フォント
	 */
	public void setNodeFont(final Font f) {
		Transformer<WebNode, Font> nodeFonter = new Transformer<WebNode, Font>() {
			@Override
			public Font transform(WebNode node) {
				return f;
			}
		};
		_panel.getRenderContext().setVertexFontTransformer(nodeFonter);
	}

	/**
	 * ノードのラベル表示位置を変更します
	 * @param pos	表示位置
	 */
	public void setNodeLabelPosition(Position pos) {
		_panel.getRenderer().getVertexLabelRenderer().setPosition(pos);
	}

	//	======	ノードの設定系　END


	//	======	エッジの設定系　BEGIN


	/**
	 * エッジのラベルを表示するかどうか
	 * @param isShown	表示する場合はtrue、しない場合はfalse
	 */
	private void setEdgeLabelFlag(final boolean isShown) {
		Transformer<String, String> edgeLabeller = new Transformer<String, String>() {
			@Override
			public String transform(String s) {
				return isShown? s : "";
			}
		};
		_panel.getRenderContext().setEdgeLabelTransformer(edgeLabeller);
	}

	/**
	 * エッジ(線)の色を設定する
	 * @param c	Edge color.
	 */
	private void setEdgeColor(final Color c) {
		Transformer<String, Paint> edgeColor = new Transformer<String, Paint>() {
			@Override
			public Paint transform(String s) {
				return c;
			}
		};
		_panel.getRenderContext().setEdgeDrawPaintTransformer(edgeColor);
	}

	//	======	エッジの設定系　END


	//	マウスイベント
	class GraphMouseEvent<N, E> extends DefaultModalGraphMouse<N,E>
	{
		GraphMouseEvent() {
			super();
			setMode(ModalGraphMouse.Mode.PICKING);
		}

		public void mouseClicked(MouseEvent e) {
			@SuppressWarnings("unchecked")
			VisualizationViewer<WebNode, String> v = (VisualizationViewer<WebNode, String>)e.getComponent();
			Iterator<WebNode> it = v.getPickedVertexState().getPicked().iterator();
			if (!it.hasNext()) return;
			WebNode webNode = it.next();
			System.out.println(webNode.getName());
			String text = webNode.getMainTest();
			//	タグクラウドの表示
			if (text.length() > 10) {
				_tagWindow.showTagCrowd(text.toString(), webNode.getUrl());
			} else {
				_tagWindow.showOtherText("Webページの本文の取得に失敗しました。");
				System.out.println("No main text : " + text);
			}
		}

	}
}
