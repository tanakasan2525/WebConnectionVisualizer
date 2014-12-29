package nettest;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class GraphFrame extends JFrame {

	private JPanel contentPane;

	private WebGraph _webGraph;



	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//	My Addition : 見た目をWindows風に
					String winTheme = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
					UIManager.setLookAndFeel(winTheme);
					GraphFrame frame = new GraphFrame();
					frame.setWebGraph("http://www.aoyama.ac.jp/");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GraphFrame() {
		setTitle("Graph");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 900);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		setContentPane(contentPane);
		setVisible(true);
	}

	public void setWebGraph(String url) {
		_webGraph = new WebGraph();
		_webGraph.edgeColor = Color.GRAY;
		_webGraph.edgeLabelFlag = false;
		//_webGraph.setAllNodeBackGroundColor(Color.BLUE);

		new WebGraphCreater().create(url);
	}

	WebGraphCreater webGraphCreater;
	class WebGraphCreater extends Thread {
		String url;
		JProgressBar bar;

		WebGraphCreater() {
			bar = new JProgressBar();
			bar.setStringPainted(true);
			bar.setAlignmentX(CENTER_ALIGNMENT);
		}

		public void run() {
			Website web = null;
			try {
				web = new Website(url);
			}catch(Exception e) {
				return;
			}

			ArrayList<String> allLinks = web.getLinksWithoutSameDomain();

			String[] badSuffix = {
				"pdf", "zip", "rar", "mp3", "mp4", "wav", ".jpg", ".png",
			};

			for (Iterator<String> it = allLinks.iterator(); it.hasNext(); ) {
				String url = it.next();
				for (String s : badSuffix)
					if (url.endsWith(s)) {
						it.remove();
						break;
					}
			}

			String[] links = allLinks.toArray(new String[0]);

			int count = 0;	//	正しく取得されたWebsiteの数
			WebNode[] webNode = new WebNode[links.length+1];

			bar.setMaximum(links.length-1);

			for (int i = 0; i < links.length; i++) {
				try {
					Website wn = new Website(links[i]);
					webNode[count] = new WebNode(wn, links[i]);
					_webGraph.addNode(webNode[count]);
					bar.setValue((int)(++count));
				} catch (Exception e) {}
			}
			webNode[count] = new WebNode(web, web.getUrl());

			for (int i = 0; i < count; i++) {
				_webGraph.addEdge(webNode[count], webNode[i]);
			}
			contentPane.remove(bar);
			_webGraph.layoutGraph(contentPane, 500, 500);
		}

		void create(String url) {
			this.url = url;
			contentPane.removeAll();
			contentPane.add(bar);
			bar.setValue(0);
			contentPane.revalidate();

			//	スレッドで実行しないと、プログレスバーが追加されない。
			//	メソッドの終了後にSwingのイベントが呼ばれて、コンポーネントの更新を行う模様
			start();
		}
	}

}
