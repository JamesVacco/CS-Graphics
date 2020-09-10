import java.awt.image.*;
import java.awt.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

import org.bytedeco.ffmpeg.*;
import org.bytedeco.ffmpeg.avcodec.*;
import org.bytedeco.ffmpeg.avformat.*;
import org.bytedeco.ffmpeg.avutil.*;

import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avutil.*;

import org.bytedeco.javacv.*;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.lang.String.*;

public class mandleZoom 
{
	static int 	WIDTH = 1920;
	static int	HEIGHT = 1080;

	// This is the upsampled canvas
	static int	VWIDTH = WIDTH*2;
	static int	VHEIGHT = HEIGHT*2;

	//Things needed to draw with
	static		FFmpegFrameRecorder 		recorder;
	static		File				outfile;
	static		Frame				frame;
	static		BufferedImage			buffer;


	// Arrays for the individual pixels
	static 		int 				R[][] = new int[WIDTH][HEIGHT];
	static 		int 				G[][] = new int[WIDTH][HEIGHT];
	static 		int 				B[][] = new int[WIDTH][HEIGHT];
	
	// Arrays for the individual pixels
	// Draw everything in these arrays and then anti-alias
	// DO NOT WRITE TO R[], G[], or B[]!
	static 		int 				VR[][] = new int[VWIDTH][VHEIGHT];
	static 		int 				VG[][] = new int[VWIDTH][VHEIGHT];
	static 		int 				VB[][] = new int[VWIDTH][VHEIGHT];

	// BufferedImage ---> Frame converter
	static		Java2DFrameConverter		converter;

	public static void main (String args[]) throws Exception, IOException
	{
		int				i, j, x, y;

		if (args.length != 1)
		{
			System.out.println ("Usage: java demo <output file name>");
			System.exit(0);
		}

		// Output file
		outfile = new File(args[0]);
		
		// Create the recorder for the animation
		// Args are filename, width, height
		recorder = new FFmpegFrameRecorder (outfile, WIDTH, HEIGHT);

		// Codec. This is the only one that seems to work?
		recorder.setVideoCodec (AV_CODEC_ID_WMV2);
		
		// Frame rate. Right now, 30
		recorder.setFrameRate(30);

		// Not entirely sure what these do. Have to do with quality,
		// but I don't know the specifics.
		recorder.setVideoOption ("preset", "ultrafast");

		// A decent bitrate. Could probably scale it back if needed
		recorder.setVideoBitrate(35000000);

		// Start the recorder
		recorder.start();

		// Where we "draw" the image before putting it in a frame.
		buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

		// Construct the converter
		converter = new Java2DFrameConverter();
		
		double ZOOM_X = 0.6702091877, ZOOM_Y = -0.4580609752;

		//Value should be increased for deeper zooms		
		int MAX_ITER = 500;

		//Video length and zoom speed variables
		int TOTAL_FRAMES = 900;	//Total length of video, 60 = 1sec
		int ZOOM_START = 150;	//Zoom start position
		double POW_VAL = 1.5;	//Starting power value, used to set zoom speed

		//Mandelbrot related variables
		double ZOOM_MIN = POW_VAL;
		double cX = 0, cY = 0;
		int[] rgb = new int[3];
		
		//Determines the length of the video
		for(i = 0; i<TOTAL_FRAMES; i++)
		{		
			//Increase zoom rate based on completed frames
			if(((((double)i/(double)TOTAL_FRAMES)*10)/2) > ZOOM_MIN);
				POW_VAL = (((double)i/(double)TOTAL_FRAMES)*10)/2;
	
			for (y = 0; y < HEIGHT; y++) 
			{
				for (x = 0; x < WIDTH; x++) 
				{	
					//Defines zoom starting height
					double MOD_START = Math.pow(i,POW_VAL)+ZOOM_START;
					
					//Adds zoom location and passes to mand
					cX = (x - (WIDTH/2)) / MOD_START-ZOOM_X;
					cY = (y - (HEIGHT/2)) / MOD_START+ZOOM_Y;

					//Runs mand object for current pixel and retrives color values
					Mandlebrot M = new Mandlebrot(x, y, MAX_ITER, cX, cY);
					rgb = M.pixelColor();

					//Set pixel RGB values
					R[x][y] = rgb[0];
					G[x][y] = rgb[1];
					B[x][y] = rgb[2];
				}
			}
			drawframe();
		}
		recorder.stop();
	}

	// My attempt at anti-aliasing.
	// true: anti-alias
	// false: Just copy upper left pixel to destination
	public static void aa(boolean trueaa)
	{
		int	x, y;
		int	newx, newy;

		newx = newy = 0;

		// Init "real" array
		cleararrays(true);

		// Just cut it
		if (!trueaa)
		{
			for (y=0; y<VHEIGHT; y+=2)
			{
				for (x=0; x<VWIDTH; x+=2)
				{
					R[newx][newy] = VR[x][y];
					G[newx][newy] = VG[x][y];
					B[newx][newy] = VB[x][y];
					newx++;
				}
				newx = 0;
				newy++;
			}
		}
		// Actual anti-aliasing
		else
		{
			for (y=0; y<VHEIGHT; y+=2)
			{
				for (x=0; x<VWIDTH; x+=2)
				{
					// Integer division, but I'm not inclined to
					// worry about loss of precision here
					R[newx][newy] = (VR[x][y] + VR[x+1][y] +
							 VR[x][y+1] + VR[x+1][y+1])/4;
					G[newx][newy] = (VG[x][y] + VG[x+1][y] +
							 VG[x][y+1] + VG[x+1][y+1])/4;
					B[newx][newy] = (VB[x][y] + VB[x+1][y] +
							 VB[x][y+1] + VB[x+1][y+1])/4;
					newx++;
				}
				newx = 0;
				newy++;
			}
		}

	}

	// Got tired of doing this all the time
	// Clears either the RGB arrays or the VRGB arrays. 
	// true: clear RGB, false: clear VRGB
	// You'll probably only use the latter.
	public static void cleararrays(boolean erasergb)
	{
		int x, y;

		if (erasergb)
			for (x = 0; x < WIDTH; x++)
				for (y = 0; y < HEIGHT; y++)
					R[x][y] = G[x][y] = B[x][y] = 0;
		else
			for (x = 0; x < VWIDTH; x++)
				for (y = 0; y < VHEIGHT; y++)
					VR[x][y] = VG[x][y] = VB[x][y] = 0;
	}

	public static void drawframe () throws Exception, IOException
	{
		int 	x, y;
		int	pixcolor;	
		Color	pcolor;

		for (x=0; x<WIDTH; x++)
		{
			for (y=0; y<HEIGHT; y++)
			{

				buffer.setRGB(x, y, R[x][y] << 16 | G[x][y] << 8 | B[x][y]);
			}
		}

		// Write the buffer to a frame
		frame = converter.convert(buffer);
		
		// THIS was hard to find. You need the second parameter or the colors are in 
		// some weird AGBR(?) packed format, with the red channel ignored completely.

		// It writes the frame into the video
		recorder.record(frame, AV_PIX_FMT_ARGB);
	}
}