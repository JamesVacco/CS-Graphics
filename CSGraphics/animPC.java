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


public class animPC 
{
	static int 	WIDTH = 768;
	static int	HEIGHT = 768;

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
		if (args.length != 1)
		{
			System.out.println ("Usage: java demo <output file name>");
			System.exit(0);
		}

		// Output file
		outfile = new File(args[0]);
		
		// Create the recorder for the animation
		// Args are filename, width, height
		recorder = new FFmpegFrameRecorder (outfile, VWIDTH, VHEIGHT);

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

		//four lines next to each other
		for (int j = 0; j < 102; j++)
		{
			drawline (0, j, j, 1530, 255, 255, 255);
			drawline (0, 102+j, 102+j, 1535, 255, 255, 255);
			drawline (0, 204+j, 204+j, 1535, 255, 255, 255);
			drawline (0, 306+j, 306+j, 1535, 255, 255, 255);
			drawline (0, 408+j, 408+j, 1535, 255, 255, 255);
			drawline (0, 510+j, 510+j, 1535, 255, 255, 255);
			drawline (0, 612+j, 612+j, 1535, 255, 255, 255);
			drawline (0, 714+j, 714+j, 1535, 255, 255, 255);
			drawline (0, 816+j, 816+j, 1535, 255, 255, 255);
			drawline (0, 918+j, 918+j, 1535, 255, 255, 255);
			drawline (0, 1020+j, 1020+j, 1535, 255, 255, 255);
			drawline (0, 1122+j, 1122+j, 1535, 255, 255, 255);
			drawline (0, 1224+j, 1224+j, 1535, 255, 255, 255);
			drawline (0, 1326+j, 1326+j, 1535, 255, 255, 255);
			drawline (0, 1428+j, 1428+j, 1535, 255, 255, 255);
			 
			drawline (1535, 1530-j, 1530-j, 0, 255, 255, 255);
			drawline (1535, 1428-j, 1428-j, 0, 255, 255, 255);
			drawline (1535, 1326-j, 1326-j, 0, 255, 255, 255);
			drawline (1535, 1224-j, 1224-j, 0, 255, 255, 255);
			drawline (1535, 1122-j, 1122-j, 0, 255, 255, 255);
			drawline (1535, 1020-j, 1020-j, 0, 255, 255, 255);
			drawline (1535, 918-j, 918-j, 0, 255, 255, 255);
			drawline (1535, 816-j, 816-j, 0, 255, 255, 255);
			drawline (1535, 714-j, 714-j, 0, 255, 255, 255);
			drawline (1535, 612-j, 612-j, 0, 255, 255, 255);
			drawline (1535, 510-j, 510-j, 0, 255, 255, 255);
			drawline (1535, 408-j, 408-j, 0, 255, 255, 255);
			drawline (1535, 306-j, 306-j, 0, 255, 255, 255);
			drawline (1535, 204-j, 204-j, 0, 255, 255, 255);
			drawline (1535, 102-j, 102-j, 0, 255, 255, 255);
			
			drawline (1535, j, 1530-j, 1535, 255, 255, 255);
			drawline (1535, 102+j, 1428-j, 1535, 255, 255, 255);
			drawline (1535, 204+j, 1326-j, 1535, 255, 255, 255);
			drawline (1535, 306+j, 1224-j, 1535, 255, 255, 255);
			drawline (1535, 408+j, 1122-j, 1535, 255, 255, 255);
			drawline (1535, 510+j, 1020-j, 1535, 255, 255, 255);
			drawline (1535, 612+j, 918-j, 1535, 255, 255, 255);
			drawline (1535, 714+j, 816-j, 1535, 255, 255, 255);
			drawline (1535, 816+j, 714-j, 1535, 255, 255, 255);
			drawline (1535, 918+j, 612-j, 1535, 255, 255, 255);
			drawline (1535, 1020+j, 510-j, 1535, 255, 255, 255);
			drawline (1535, 1122+j, 408-j, 1535, 255, 255, 255);
			drawline (1535, 1224+j, 306-j, 1535, 255, 255, 255);
			drawline (1535, 1326+j, 204-j, 1535, 255, 255, 255);
			drawline (1535, 1428+j, 102-j, 1535, 255, 255, 255);
			
			drawline (0, 1530-j, j, 0, 255, 255, 255);
			drawline (0, 1428-j, 102+j, 0, 255, 255, 255);
			drawline (0, 1326-j, 204+j, 0, 255, 255, 255);
			drawline (0, 1224-j, 306+j, 0, 255, 255, 255);
			drawline (0, 1122-j, 408+j, 0, 255, 255, 255);
			drawline (0, 1020-j, 510+j, 0, 255, 255, 255);
			drawline (0, 918-j, 612+j, 0, 255, 255, 255);
			drawline (0, 816-j, 714+j, 0, 255, 255, 255);
			drawline (0, 714-j, 816+j, 0, 255, 255, 255);
			drawline (0, 612-j, 918+j, 0, 255, 255, 255);
			drawline (0, 510-j, 1020+j, 0, 255, 255, 255);
			drawline (0, 408-j, 1122+j, 0, 255, 255, 255);
			drawline (0, 306-j, 1224+j, 0, 255, 255, 255);
			drawline (0, 204-j, 1326+j, 0, 255, 255, 255);
			drawline (0, 102-j, 1428+j, 0, 255, 255, 255);
			
			
			
			aa(true);
			drawframe();
			cleararrays(false);
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

	public static void drawline(int x0, int y0, int x1, int y1, 
		int red, int grn, int blu)
	{	
		int d = 0;
 
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
 
        int dx2 = 2 * dx; // slope scaling factors to
        int dy2 = 2 * dy; // avoid floating point
 
        int ix = x0 < x1 ? 1 : -1; // increment direction
        int iy = y0 < y1 ? 1 : -1;
 
        int x = x0;
        int y = y0;
 
        if (dx >= dy) 
		{
            while (true) 
			{
				VR[x][y] = red;
				VG[x][y] = grn;
				VB[x][y] = blu;
                if (x == x1)
                    break;
                x += ix;
                d += dy2;
                if (d > dx) 
				{
                    y += iy;
                    d -= dx2;
                }
            }
        } 
		else 
		{
            while (true) 
			{
				VR[x][y] = red;
				VG[x][y] = grn;
				VB[x][y] = blu;
                if (y == y1)
                    break;
                y += iy;
                d += dx2;
                if (d > dy) 
				{
                    x += ix;
                    d -= dy2;
                }
            }
        }
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
