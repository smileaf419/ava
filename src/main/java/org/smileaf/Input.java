package org.smileaf;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Input extends JFrame {
	
	public Input () {
		initUI();
	}
    private void initUI() {
    	Map map = new Map();
    	add(map);

        setTitle("Ava");
        
        setSize(480, 480);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public class Map extends JPanel {
    	public Map() {
            setFocusable(true);
    	}
    	
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(new Color(51, 51, 51));
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            
            ImageIcon iicon = new ImageIcon("src/images/wall.png");
            Image image = iicon.getImage();
            g.drawImage(image, 200, 200, this);
        }
    	
    }
}