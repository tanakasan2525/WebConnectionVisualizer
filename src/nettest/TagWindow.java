package nettest;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TagWindow extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	Random rand = new Random(System.currentTimeMillis());

	private JPanel panelTagCrowd;
	private JLabel summaryLabel;

	final String appId = "dj0zaiZpPUNSYXB2aWROVmdIOSZzPWNvbnN1bWVyc2VjcmV0Jng9ZDk-";

	String url;

	MarkovChain markov = new MarkovChain(2);	//	"2"次のマルコフ連鎖

	/**
	 * This is the default constructor
	 */
	public TagWindow() {
		super();
		initialize();
	}

	public void showTagCrowd(String text, String url) {
		this.url = url;

		String[][] str = TagExtracter.extractKeyword(appId, text);

		int x = 0, y = panelTagCrowd.getY();

		panelTagCrowd.removeAll();
		for (String[] token : str) {

			MyLabel label = new MyLabel(token[0]);
			//	token[1]は1～100の値
			int size = Integer.parseInt(token[1]) * 45 / 100;
			if (size < 10) size = 10;

			int r, g, b;
			int[] rgb = HSVtoRGB(rand.nextInt(360), 255, 210);
			r = rgb[0];
			g = rgb[1];
			b = rgb[2];

			label.setForeground(new Color(r, g, b));

			label.addMouseListener(new MyMouseListener(panelTagCrowd));

			label.setFontsize(size);

			label.setLocation(x, y);
			x += label.getWidth();
			if (x + label.getWidth() > this.getWidth()) {
				x = 0;
				y += 45;
			}
			panelTagCrowd.add(label);
		}

		jContentPane.repaint(0, 0, jContentPane.getWidth(), jContentPane.getHeight());

		markov.clear();
		for (String sentence : text.split("。")) {
			markov.add(sentence + "。");
		}
		summaryLabel.setText(markov.generate());
	}

	public void showOtherText(String text) {
		summaryLabel.setText(text);
	}


	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		this.setTitle("Tag Crowd");
		this.setBounds(10, 10, 600, 500);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		summaryLabel = new JLabel("ここに要約が表示されます。");
		summaryLabel.setBounds(0, 0, this.getWidth(), 40);
		jContentPane.add(summaryLabel);
		JButton button = new JButton("このサイトを閲覧する");
		button.setBounds(0, summaryLabel.getHeight(), 300, 40);
		button.addActionListener(this);
		jContentPane.add(button);
		panelTagCrowd = new JPanel();
		panelTagCrowd.setLayout(null);
		panelTagCrowd.setBounds(0, button.getY() + button.getHeight() + 100, 600, 500);
		jContentPane.add(panelTagCrowd);

		addComponentListener(new ComponentAdapter(){
            @Override
            //	ウィンドウのリサイズ時に呼ばれるイベント
            public void componentResized(ComponentEvent e) {
            	summaryLabel.setBounds(0, 0, getWidth(), summaryLabel.getHeight());
                panelTagCrowd.setBounds(0, summaryLabel.getY() + summaryLabel.getHeight(), getWidth(), getHeight());
            }
        });
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
		}
		return jContentPane;
	}

	/**
	 * RGBからHSVに変換します。
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	public int[] RGBtoHSV(int red, int green, int blue) {
        int[] hsv = new int[3];
        int max = Math.max(red, Math.max(green, blue));
        int min = Math.min(red, Math.min(green, blue));

        // h
        if(max == min){
            hsv[0] = 0;
        }
        else if(max == red){
            hsv[0] = (60 * (green - blue) / (max - min) + 360) % 360;
        }
        else if(max == green){
            hsv[0] = (60 * (blue - red) / (max - min)) + 120;
        }
        else if(max == blue){
            hsv[0] = (60 * (red - green) / (max - min)) + 240;
        }

        // s
        if(max == 0){
            hsv[1] = 0;
        }
        else{
            hsv[1] = (255 * ((max - min) / max));
        }

        // v
        hsv[2] = max;

        return hsv;
    }

	/**
	 * 	HSVからRGBに変換します。
	 * @param h	0~360
	 * @param s	0~255
	 * @param v	0~255
	 * @return
	 */
	public int[] HSVtoRGB(int h, int s, int v) {
        float f;
        int i, p, q, t;
        int[] rgb = new int[3];

        i = (int)Math.floor(h / 60.0f) % 6;
        f = (float)(h / 60.0f) - (float)Math.floor(h / 60.0f);
        p = (int)Math.round(v * (1.0f - (s / 255.0f)));
        q = (int)Math.round(v * (1.0f - (s / 255.0f) * f));
        t = (int)Math.round(v * (1.0f - (s / 255.0f) * (1.0f - f)));

        switch(i){
            case 0 : rgb[0] = v; rgb[1] = t; rgb[2] = p; break;
            case 1 : rgb[0] = q; rgb[1] = v; rgb[2] = p; break;
            case 2 : rgb[0] = p; rgb[1] = v; rgb[2] = t; break;
            case 3 : rgb[0] = p; rgb[1] = q; rgb[2] = v; break;
            case 4 : rgb[0] = t; rgb[1] = p; rgb[2] = v; break;
            case 5 : rgb[0] = v; rgb[1] = p; rgb[2] = q; break;
        }

        return rgb;
    }

	/**
	 * このサイトを閲覧するボタンの動作
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Browser.open(url);
	}
}

class MyMouseListener extends MouseAdapter {
	private JPanel jContentPane = null;

	MyMouseListener(JPanel panel) {
		jContentPane = panel;
	}

	public void mouseEntered(MouseEvent e) {
		MyLabel label = (MyLabel)e.getComponent();
		jContentPane.setComponentZOrder(label, 0);
		label.setFontsize(label.getFont().getSize()*2);
	}

	public void mouseExited(MouseEvent e) {
		MyLabel label = (MyLabel)e.getComponent();
		label.setFontsize(label.getFont().getSize()/2);
	}
}

class MyLabel extends JLabel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	MyLabel(String text) {
		super(text);
	}

	public void setFontsize(int size) {
		Font f = getFont().deriveFont((float)size);
		FontMetrics fm = getFontMetrics(f);
		int width = fm.stringWidth(getText());
		int height = fm.getHeight();
		setSize(width, height);
		setFont(f);
	}
}

