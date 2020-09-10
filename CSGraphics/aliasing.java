import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.image.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;

public class aliasing
{
	/*
	 * This template for CSCI 365 does the following:
	 * 	- Read a PNG from a file
	 * 	- Place the color components in a 2-D array
	 * 	- Re-copy the array into an imagebuffer
	 * 	- Refresh the ImageBuffer
	 */

	static int	HEIGHT	= 1536;
	static int	WIDTH 	= 2048;

	static int 	R[][] 	= new int[WIDTH/2][HEIGHT/2];
	static int 	G[][] 	= new int[WIDTH/2][HEIGHT/2];
	static int 	B[][] 	= new int[WIDTH/2][HEIGHT/2];
	
	//RGB arrays for original image
	static int 	BR[][] 	= new int[WIDTH][HEIGHT];
	static int 	BG[][] 	= new int[WIDTH][HEIGHT];
	static int 	BB[][] 	= new int[WIDTH][HEIGHT];

	static BufferedImage	buffer;
	static MyCanvas		canvas;


	public static void main (String args[])
	{
		Scanner input = new Scanner (System.in);
		String	keyin;
		int	x, y;

		/*
		 * Necessary AWT/Swing steps.
		 */
		JFrame	frame = new JFrame();
		canvas = new MyCanvas();
		frame.add (canvas, "Center");

		/*
		 * Boilerplate. Just do this. I don't even 
		 * remember what it all does any more, but 
		 * it's required.
		 */
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Change if you like.
		frame.setTitle("CSCI 365");

		/*
		 * Y > HEIGHT because of "Window Decorations" 
		 * (Java's terminology, not mine.)
		 */ 
		frame.setSize(WIDTH/2, (HEIGHT/2)+15);
		frame.setVisible(true);

		while(true)
		{
			printmenu();
			keyin = input.next();
			switch (keyin)
			{
				/*
				 * Anything your program "does" goes in
				 * here. Right now, read and display
				 */
				case "r":
				case "R":
					System.out.printf ("Name: ");
					readimage(input.next());
					break;
				case "s":
				case "S":
					cut();
					break;
				case "a":
				case "A":
					ssaa();
					break;						
				case "d":
				case "D":
					displayimage();
					break;
				case "c":
				case "C":
					reduceColors();
					break;
				case "x":
				case "X":
					System.exit(0);
			}
		}
	}
	public static void printmenu ()
	{
		System.out.printf ("Menu:\n");
		System.out.printf ("r:\tread image\n");
		System.out.printf ("s:\tapply straight cut AA\n");
		System.out.printf ("a:\tapply SSAA\n");
		System.out.printf ("c:\treduce colors");
		System.out.printf ("d:\tdisplay image in memory\n");
		System.out.printf ("x:\texit\n");
		System.out.printf ("Enter an option: ");
	}

	public static void readimage(String name)
	{
		int	x, y;
		Color 	c;
		try
		{
			buffer = ImageIO.read(new File(name));
			for (x = 0; x < WIDTH; x++)
			{
				for (y = 0; y < HEIGHT; y++)
				{
					c = new Color(buffer.getRGB(x, y));
					
					//Sets RGB values at pixel location
					BR[x][y] = c.getRed();
					BG[x][y] = c.getGreen();
					BB[x][y] = c.getBlue();
				}
			}
		}
		catch (IOException e)
		{
			System.out.println ("HALP ME.");
			e.printStackTrace();
		}
	}


	public static void cut()
	{
		for (int x = 0; x < WIDTH; x++)
		{
			for (int y = 0; y < HEIGHT; y++)
			{

				if((x % 2 != 0) && (y % 2 != 0))
				{
					R[(int)Math.ceil(x/2)][(int)Math.ceil(y/2)] = BR[x][y];
					G[(int)Math.ceil(x/2)][(int)Math.ceil(y/2)] = BG[x][y];
					B[(int)Math.ceil(x/2)][(int)Math.ceil(y/2)] = BB[x][y];
				}
			}
		}
	}
	
	public static void ssaa()
	{
		//Top right
		int trR[][] = new int[WIDTH/2][HEIGHT/2];
		int trG[][] = new int[WIDTH/2][HEIGHT/2];
		int trB[][] = new int[WIDTH/2][HEIGHT/2];
		
		//Top left
		int tBR[][] = new int[WIDTH/2][HEIGHT/2];
		int tBG[][] = new int[WIDTH/2][HEIGHT/2];
		int tBB[][] = new int[WIDTH/2][HEIGHT/2];
		
		//Bottom right
		int brR[][] = new int[WIDTH/2][HEIGHT/2];
		int brG[][] = new int[WIDTH/2][HEIGHT/2];
		int brB[][] = new int[WIDTH/2][HEIGHT/2];
		
		//Bottom left
		int bBR[][] = new int[WIDTH/2][HEIGHT/2];
		int bBG[][] = new int[WIDTH/2][HEIGHT/2];
		int bBB[][] = new int[WIDTH/2][HEIGHT/2];
		

		for (int x = 0; x < WIDTH; x++)
		{
			for (int y = 0; y < HEIGHT; y++)
			{
				if((x % 2 == 0) && (y % 2 == 0))
				{
					tBR[x-(x/2)][y-(y/2)] = BR[x][y];
					tBG[x-(x/2)][y-(y/2)] = BG[x][y];
					tBB[x-(x/2)][y-(y/2)] = BB[x][y];
				}

				if((x % 2 != 0) && (y % 2 == 0))
				{
					trR[(int)Math.ceil(x/2)][y-(y/2)] = BR[x][y];
					trG[(int)Math.ceil(x/2)][y-(y/2)] = BG[x][y];
					trB[(int)Math.ceil(x/2)][y-(y/2)] = BB[x][y];
				}

				if((x % 2 == 0) && (y % 2 != 0))
				{
					bBR[x-(x/2)][(int)Math.ceil(y/2)] = BR[x][y];
					bBG[x-(x/2)][(int)Math.ceil(y/2)] = BG[x][y];
					bBB[x-(x/2)][(int)Math.ceil(y/2)] = BB[x][y];	
				}

				if((x % 2 != 0) && (y % 2 != 0))
				{
					brR[(int)Math.ceil(x/2)][(int)Math.ceil(y/2)] = BR[x][y];
					brG[(int)Math.ceil(x/2)][(int)Math.ceil(y/2)] = BG[x][y];
					brB[(int)Math.ceil(x/2)][(int)Math.ceil(y/2)] = BB[x][y];
				}
			}
		}
		

		for (int x = 0; x < WIDTH/2; x++)
		{
			for (int y = 0; y < HEIGHT/2; y++)
			{
				R[x][y] = ((tBR[x][y] + trR[x][y] + bBR[x][y] + brR[x][y])/4);
				G[x][y] = ((tBG[x][y] + trG[x][y] + bBG[x][y] + brG[x][y])/4);
				B[x][y] = ((tBB[x][y] + trB[x][y] + bBB[x][y] + brB[x][y])/4);
			}
		}
	}

	public static void displayimage()
	{
		int	x, y;
		for (x=0; x<WIDTH/2; x++)
			for (y=0; y<HEIGHT/2; y++)
			{
				buffer.setRGB(x, y,
					((R[x][y] << 16 |
						G[x][y] << 8 |
						B[x][y])));
			}
			canvas.repaint();
	}
	
	public static void reduceColors() 
	{
		HashMap<Byte, Integer> rMap = new HashMap<>();
		HashMap<Byte, Integer> gMap = new HashMap<>();
		HashMap<Byte, Integer> bMap = new HashMap<>();
		
		
		try
		{
			for (int row = 0; row < HEIGHT; row++) 
			{
				for (int col = 0; col < WIDTH; col++) 
				{
					int value = rMap.getOrDefault(R[row][col], 0);
					rMap.put((byte)R[row][col], ++value);
					
					value = gMap.getOrDefault(G[row][col], 0);
					gMap.put((byte)G[row][col], ++value);
					
					value = bMap.getOrDefault(B[row][col], 0);
					bMap.put((byte)R[row][col], ++value);
				}
			}
		}
		catch (Exception E)
		{}
		
		List<Byte> red = new ArrayList(rMap.keySet());
		List<Byte> green = new ArrayList(gMap.keySet());
		List<Byte> blue = new ArrayList(bMap.keySet());
		
		Collections.sort(red, (Comparator<Byte>) (Byte a, Byte b) -> { return rMap.get(b) - rMap.get(a); });
		Collections.sort(green, (Comparator<Byte>) (Byte a, Byte b) -> { return gMap.get(b) - gMap.get(a); });
		Collections.sort(blue, (Comparator<Byte>) (Byte a, Byte b) -> { return bMap.get(b) - bMap.get(a); });
		
		byte[] selectedRed = new byte[256];
		byte[] selectedGreen = new byte[256];
		byte[] selectedBlue = new byte[256];
		
		IntStream.range(0,256).forEach(i -> {
			try
			{
				selectedRed[i] = red.get(i);
				selectedGreen[i] = green.get(i);
				selectedBlue[i] = blue.get(i);
			}
			catch(Exception E)
			{}
			}
		);
		
		IndexColorModel model = new IndexColorModel(8, 256, selectedRed, selectedGreen, selectedBlue, 255);
		BufferedImage n = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_INDEXED, model);
		Graphics2D g2d = (Graphics2D) n.getGraphics();
		g2d.drawImage(buffer, null, 0, 0);
		buffer = n;
		canvas.repaint();
	}


	/*
	 * Inner classes. Only exist for graphics, as far as I know
	 */
	public static class MyCanvas extends JPanel
	{
		public MyCanvas()
		{
			super(true);
		}
		
		public void paint(Graphics g)
		{
			g.drawImage(buffer, 0, 0, Color.red, null);
		}
	}
}
