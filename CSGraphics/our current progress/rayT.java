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


public class rayT
{
	static int 	WIDTH = 1024;
	static int	HEIGHT = 768;

	//Things needed to draw with
	static		FFmpegFrameRecorder 		recorder;
	static		File				outfile;
	static		File				infile;
	static		Frame				frame;
	static		BufferedImage			buffer;


	// 3D stuff
	static		point		EYE;
	static		spherelist	sphere[];
	static		double		VIEWZ;


	// Arrays for the individual pixels
	static 		int 				R[][] = new int[WIDTH][HEIGHT];
	static 		int 				G[][] = new int[WIDTH][HEIGHT];
	static 		int 				B[][] = new int[WIDTH][HEIGHT];
	
	//constants
	static		double		ka = 0;
	static		double		kd = 0;
	static		double		 l = 0;
	static		double		dp = 0;
	
	static		int 		xcopy=0;
	static		int 		ycopy = 0;
	
	static		lightlist	light[];
	// BufferedImage ---> Frame converter
	static		Java2DFrameConverter		converter;

	public static void main (String args[]) throws Exception, IOException
	{

		int				i, j, x, y,z;
		String				input;
		Scanner				filein;
		

		// Mathy stuff
		double				dx, dy, dz;
		double				a, b, c;
		double				x0, x1, y0, y1, z0, z1;
		double				disc;

		double		t0,t1,inter,nx,ny,nz,lx,ly,lz;

		if (args.length != 2)
		{
			System.out.println
				("Usage: java ray <input file name> <output file name>");
			System.exit(0);
		}

		// Files
		infile = new File(args[0]);
		filein = new Scanner(infile);

		outfile = new File(args[1]);

		System.out.printf ("Processing...\n");
		input = (filein.next());
		EYE = new point(Integer.parseInt(filein.next()),
					Integer.parseInt(filein.next()),
					Integer.parseInt(filein.next()));
		
		System.out.printf ("Eye point:\n");
		System.out.printf ("\tx: %.02f\n", EYE.x);
		System.out.printf ("\ty: %.02f\n", EYE.y);
		System.out.printf ("\tz: %.02f\n", EYE.z);
		

		// View z (may eventually tag corners, but not as of now)
		filein.next();
		VIEWZ = Integer.parseInt(filein.next());
		System.out.printf ("View z plane: %.02f\n", VIEWZ);


		//load ka/kd values
		filein.next();
		ka = Double.parseDouble(filein.next());
		filein.next();
		kd = Double.parseDouble(filein.next());
		
		
		//how many lights
		filein.next();
		light = new lightlist[Integer.parseInt(filein.next())];
		
		for (z=0; z<light.length; z++){
			System.out.printf ("Light %d\n", z);
			filein.next();
			
			light[z] = new lightlist (Double.parseDouble(filein.next()),
						Double.parseDouble(filein.next()),
						Double.parseDouble(filein.next()),
						//Eat COLOR:
						filein.next(),
						Integer.parseInt(filein.next()),
						Integer.parseInt(filein.next()),
						Integer.parseInt(filein.next()));
			System.out.printf ("\tcx: %.02f\n", light[z].lx);
			System.out.printf ("\tcy: %.02f\n", light[z].ly);
			System.out.printf ("\tcz: %.02f\n", light[z].lz);
			System.out.printf ("\tr: %d\n", light[z].r);
			System.out.printf ("\tg: %d\n", light[z].g);
			System.out.printf ("\tb: %d\n", light[z].b);
			System.out.printf ("\n");
		}

		// How many spheres do we have?
		filein.next();
		sphere = new spherelist[Integer.parseInt(filein.next())];

		System.out.printf ("%d spheres...\n", sphere.length);
		System.out.printf ("-------\n");
		

		for (i=0; i<sphere.length; i++)
		{
			System.out.printf ("Sphere %d\n", i);
			//Eat SPHERE:
			filein.next();
			sphere[i] = new spherelist (Double.parseDouble(filein.next()),
						Double.parseDouble(filein.next()),
						Double.parseDouble(filein.next()),
						Double.parseDouble(filein.next()),
						//Eat COLOR:
						filein.next(),
						Integer.parseInt(filein.next()),
						Integer.parseInt(filein.next()),
						Integer.parseInt(filein.next()));
			System.out.printf ("\tcx: %.02f\n", sphere[i].cx);
			System.out.printf ("\tcy: %.02f\n", sphere[i].cy);
			System.out.printf ("\tcz: %.02f\n", sphere[i].cz);
			System.out.printf ("\tradius: %.02f\n", sphere[i].radius);
			System.out.printf ("\tr: %d\n", sphere[i].r);
			System.out.printf ("\tg: %d\n", sphere[i].g);
			System.out.printf ("\tb: %d\n", sphere[i].b);
			System.out.printf ("\n");
		}



		cleararrays();

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

		///////////////////
		// THE MAIN LOOP //
		///////////////////
		for(z=0; z<light.length; z++)
		{
		for (i = 0; i<sphere.length; i++)
		{
			for (x = -(WIDTH/2); x < WIDTH/2; x++)
			{
				for (y=-(HEIGHT/2); y < HEIGHT/2; y++)
				{
					x0 = EYE.x;
					y0 = EYE.y;
					z0 = EYE.z;
					
					x1 = x;
					y1 = y;
					z1 = VIEWZ;

					dx = x1 - x0;
					dy = y1 - y0;
					dz = z1 - z0;
					
					t0 = 0;
					t1 = 0; 
					inter = 0;
					
					nx = 0;
					ny = 0;
					nz = 0;
					
					lx = 0;
					ly = 0;
					lz = 0;
					
					xcopy = x;
					ycopy = y;
					

					a = sq(dx) + sq(dy) + sq(dz);
					b = 2 * dx * (x0 - sphere[i].cx) + 
						2 * dy * (y0 - sphere[i].cy) + 
						2 * dz * (z0 - sphere[i].cz);
					c = sq(sphere[i].cx) + sq(sphere[i].cy) + sq(sphere[i].cz) +
						sq(x0) + sq(y0) + sq(z0) +
						(-2) * (sphere[i].cx * x0 +
						sphere[i].cy * y0 + 
						sphere[i].cz * z0) -
						sphere[i].radius * sphere[0].radius;

					disc = sq(b) - 4 * a * c;

					if (disc > 0)
					{
						//R[x][y] = sphere[i].r;
						//G[x][y] = sphere[i].g;
						//B[x][y] = sphere[i].b;
						
						t0 = (-b + Math.sqrt(disc))/(2 * a);
						t1 = (-b - Math.sqrt(disc))/(2 * a);
						inter = Math.min(t0,t1);
						
						nx = ((inter + (inter*0))-sphere[i].cx)/sphere[i].radius;
						ny = ((y1 + (inter*0))-sphere[i].cy)/sphere[i].radius;
						nz = ((z1 + (inter*0))-sphere[i].cz)/sphere[i].radius;
						
						
							lx = light[z].lx - (inter-(inter*0));
							ly = light[z].ly - (y1-(inter*0));
							lz = light[z].lz - (z1-(inter*0));
						
						
						l = Math.sqrt((sq(lx)+sq(ly)+sq(lz)));
						
						lx = lx/l;
						ly = ly/l;
						lz = lz/l;
						
						dp = (nx*lx)+(ny+ly)+(nz*lz);
						
						
						//R[x][y] = (int)((ka*sphere[i].r)+((kd*Math.cos(dp))*sphere[i].r));
						//G[x][y] = (int)((ka*sphere[i].g)+((kd*Math.cos(dp))*sphere[i].g));
						//B[x][y] = (int)((ka*sphere[i].b)+((kd*Math.cos(dp))*sphere[i].b));
						if(xcopy<0)
							xcopy += WIDTH/2;
						else if(xcopy>=0)
							xcopy = WIDTH/2 + xcopy;
						
						if(ycopy<0)
							ycopy += HEIGHT/2;
						else if(xcopy>=0)
							ycopy = HEIGHT/2 + ycopy;
						
						//System.out.println("X "+xcopy+" y "+ycopy);
						
						R[xcopy][ycopy] = (int)((ka*sphere[i].r)+((kd*Math.cos(dp))*sphere[i].r));
						G[xcopy][ycopy] = (int)((ka*sphere[i].g)+((kd*Math.cos(dp))*sphere[i].g));
						B[xcopy][ycopy] = (int)((ka*sphere[i].b)+((kd*Math.cos(dp))*sphere[i].b));
						
					}
					
						
					
						
					
					}
					
				}
			}
		}
		for (i=0; i<30; i++)
			drawframe();

		recorder.stop();
	}

	//Dumb function, but easier than typing Math.POW(x, 2) every time
	public static double sq (double i)
	{
		return i * i;
	}
	//Polymorphism for the win
	public static int sq (int i)
	{
		return i * i;
	}

	// Got tired of doing this all the time
	// Clears either the RGB arrays or the VRGB arrays. 
	// true: clear RGB, false: clear VRGB
	// You'll probably only use the latter.
	public static void cleararrays()
	{
		int x, y;

		for (x = 0; x < WIDTH; x++)
			for (y = 0; y < HEIGHT; y++)
				R[x][y] = G[x][y] = B[x][y] = 0;
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