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
import java.io.File;
import java.io.IOException;


public class hw7{
	
    static int WIDTH = 1024;
    static int HEIGHT = 768;

    static int backGroundColor = 55;

    // This is the upsampled canvas
    static int VWIDTH = WIDTH * 2;
    static int VHEIGHT = HEIGHT * 2;

    //Things needed to draw with
    static FFmpegFrameRecorder recorder;
    static File outfile;
    static Frame frame;
    static BufferedImage buffer;


    // Arrays for the individual pixels
    static int R[][] = new int[WIDTH][HEIGHT];
    static int G[][] = new int[WIDTH][HEIGHT];
    static int B[][] = new int[WIDTH][HEIGHT];

    // Arrays for the individual pixels
    // Draw everything in these arrays and then anti-alias
    // DO NOT WRITE TO R[], G[], or B[]!
    static int VR[][] = new int[VWIDTH][VHEIGHT];
    static int VG[][] = new int[VWIDTH][VHEIGHT];
    static int VB[][] = new int[VWIDTH][VHEIGHT];

    // BufferedImage ---> Frame converter
    static Java2DFrameConverter converter;

    public static void main(String args[]) throws Exception, IOException {

        int i, j, x, y;


        if (args.length != 1) {
            System.out.println("Usage: java demo <output file name>");
            System.exit(0);
        }

        // Output file
        outfile = new File(args[0]);

        // Create the recorder for the animation
        // Args are filename, width, height
        recorder = new FFmpegFrameRecorder(outfile, WIDTH, HEIGHT);

        // Codec. This is the only one that seems to work?
        recorder.setVideoCodec(AV_CODEC_ID_WMV2);

        // Frame rate. Right now, 30
        recorder.setFrameRate(30);

        // Not entirely sure what these do. Have to do with quality,
        // but I don't know the specifics.
        recorder.setVideoOption("preset", "ultrafast");

        // A decent bitrate. Could probably scale it back if needed
        recorder.setVideoBitrate(35000000);

        // Start the recorder
        recorder.start();

        // Where we "draw" the image before putting it in a frame.
        buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

        // Construct the converter
        converter = new Java2DFrameConverter();

        String file = "D:\\Java\\ray\\config3.txt";
        Scanner scan = new Scanner(new File(file));
        //scan.useDelimiter(" "); //I believe this was causing issues
        scan.next(); //Read EYE
        //Create eye variables
        int eyeX = Integer.parseInt(scan.next());
        int eyeY = Integer.parseInt(scan.next());
        int eyeZ = Integer.parseInt(scan.next());
        scan.next(); //Read VIEWZ
        //Create viewZ variable
        int viewZ = Integer.parseInt(scan.next());
        scan.next(); //Read KA:
        double ka = Double.parseDouble(scan.next());
        scan.next(); //Read KD:
        double kd = Double.parseDouble(scan.next());
        scan.next(); //Read Lights:
        int numOfLights = Integer.parseInt(scan.next());
        //Create arrays to store data for each light
        int lightX[] = new int[numOfLights];
        int lightY[] = new int[numOfLights];
        int lightZ[] = new int[numOfLights];
        int lightR[] = new int[numOfLights];
        int lightG[] = new int[numOfLights];
        int lightB[] = new int[numOfLights];
        //Grab information on each light
        for (i = 0; i < numOfLights; i++) {
            scan.next(); //Read LIGHT:
            lightX[i] = Integer.parseInt(scan.next());
            lightY[i] = Integer.parseInt(scan.next());
            lightZ[i] = Integer.parseInt(scan.next());
            scan.next(); //Read Color:
            lightR[i] = Integer.parseInt(scan.next());
            lightB[i] = Integer.parseInt(scan.next());
            lightG[i] = Integer.parseInt(scan.next());
        }
        scan.next(); //Read SPHERES
        //Grab number of spheres
        int numSpheres = Integer.parseInt(scan.next());
        //Create arrays to store data for each sphere
        double sphereX[] = new double[numSpheres];
        double sphereY[] = new double[numSpheres];
        double sphereZ[] = new double[numSpheres];
        int sphereRad[] = new int[numSpheres];
        int sphereR[] = new int[numSpheres];
        int sphereG[] = new int[numSpheres];
        int sphereB[] = new int[numSpheres];
        //Grab information on each sphere
        for (i = 0; i < numSpheres; i++) {
            scan.next(); //Read SPHERE
            sphereX[i] = Double.parseDouble(scan.next());
            sphereY[i] = Double.parseDouble(scan.next());
            sphereZ[i] = Double.parseDouble(scan.next());
            sphereRad[i] = Integer.parseInt(scan.next());
            scan.next(); //Read COLOR
            sphereR[i] = Integer.parseInt(scan.next());
            sphereG[i] = Integer.parseInt(scan.next());
            sphereB[i] = Integer.parseInt(scan.next());
        }
        //Close last file
        scan.close();
        //Open next file
        Scanner scan2 = new Scanner(new File("D:\\Java\\ray\\quarter.txt"));
        //Setup transformation variables
        String tempX;
        String tempY;
        String tempZ;
        String corX;
        String corY;
        String corZ;
        int oX = 0;
        int oY = 0;
        int oZ = 0;
        boolean isRotate;

        int counter = 0;
        while (scan2.hasNextLine()) {
            //Fill background
            for (i = 0; i < WIDTH; i++) {
                for (j = 0; j < HEIGHT; j++) {
                    R[i][j] = backGroundColor;
                    G[i][j] = backGroundColor;
                    B[i][j] = backGroundColor;
                }
            }
            //Grab the first transformation  
            tempX = scan2.next();
            corX = tempX.substring(0, tempX.indexOf('x'));
            //Check if the last letter is an r, if so, this is a rotation and we will need to chop our strings differently
            if (corX.charAt(corX.length() - 1) == 'r') {
                //If so, split the strings accordingly
                isRotate = true;
                //Get X rotate coordinate
                corX = corX.substring(0, corX.indexOf('r'));
                //Get Y rotate coordinate
                tempY = scan2.next();
                corY = tempY.substring(0, tempY.indexOf('r'));
                //Get Z rotate coordinate
                tempZ = scan2.next();
                corZ = tempZ.substring(0, tempZ.indexOf('r'));
                //Get origin X offset
                tempX = scan2.next();
                oX = Integer.parseInt(tempX.substring(0, tempX.indexOf('X')));
                //Get origin Y offset
                tempY = scan2.next();
                oY = Integer.parseInt(tempY.substring(0, tempY.indexOf('Y')));
                //Get origin Z offset
                tempZ = scan2.next();
                oZ = Integer.parseInt(tempZ.substring(0, tempZ.indexOf('Z')));
            }
            //If it is not a rotation, there is less work to do
            else {
                isRotate = false;
                //We can skip x as we already have the correct coordinate
                //Get Y rotate coordinate
                tempY = scan2.next();
                corY = tempY.substring(0, tempY.indexOf('y'));
                //Get Z rotate coordinate
                tempZ = scan2.next();
                corZ = tempZ.substring(0, tempZ.indexOf('z'));
            }
            //PARSE DOUBLE AREA for x, y, z
            double dCorX = Double.parseDouble(corX);
            double dCorY = Double.parseDouble(corY);
            double dCorZ = -(Double.parseDouble(corZ));
            //If it is a rotation, run the rotation method
            if (isRotate) {
                rotate(dCorX, dCorY, dCorZ, oX, oY, oZ, sphereX, sphereY, sphereZ, numSpheres);
                //Otherwise, run the transform method
            } else {
                transform(dCorX, dCorY, dCorZ, sphereX, sphereY, sphereZ, numSpheres);
            }

            //Ray trace each pixel in the screen
            for (i = -(WIDTH / 2); i < WIDTH / 2; i++) {
                for (j = -(HEIGHT / 2); j < HEIGHT / 2; j++) {
                    rayTrace(i, j, eyeX, eyeY, eyeZ, viewZ, numSpheres, sphereX, sphereY, sphereZ, sphereRad, sphereR, sphereG, sphereB, lightX, lightY, lightZ, numOfLights, ka, kd);
                }
            }
            //Record the frame
            drawframe();
            System.out.println("Frame " + counter);
            counter++;
        }
        recorder.stop();
        //End main
    }
    public static void rotate(double rX, double rY, double rZ, int oX, int oY, int oZ, double sphereX[], double sphereY[], double sphereZ[], int numSpheres) {
        //Create and calculate variables for rotation
        double cosa = Math.cos(Math.toRadians(rZ));
        double sina = Math.sin(Math.toRadians(rZ));
        double cosb = Math.cos(Math.toRadians(rY));
        double sinb = Math.sin(Math.toRadians(rY));
        double cosc = Math.cos(Math.toRadians(rX));
        double sinc = Math.sin(Math.toRadians(rX));

        double axx = cosa * cosb;
        double axy = cosa * sinb * sinc - sina * cosc;
        double axz = cosa * sinb * cosc + sina * sinc;

        double ayx = sina * cosb;
        double ayy = sina * sinb * sinc + cosa * cosc;
        double ayz = sina * sinb * cosc - cosa * sinc;

        double azx = -sinb;
        double azy = cosb * sinc;
        double azz = cosb * cosc;

        //Create sphere place holder variables
        double px;
        double py;
        double pz;

        //Loop through all the spheres in the scene
        for (int i = 0; i < numSpheres; i++) {

            //Pull the spheres forward
            sphereX[i] -= oX;
            sphereY[i] -= oY;
            sphereZ[i] -= oZ;

            //Setup px, py, and pz variables
            px = sphereX[i];
            py = sphereY[i];
            pz = sphereZ[i];

            //Update spheres
            sphereX[i] = axx * px + axy * py + axz * pz;
            sphereY[i] = ayx * px + ayy * py + ayz * pz;
            sphereZ[i] = azx * px + azy * py + azz * pz;

            //Push spheres back
            sphereX[i] += oX;
            sphereY[i] += oY;
            sphereZ[i] += oZ;
        }
    }
    public static void transform(double corX, double corY, double corZ, double sphereX[], double sphereY[], double sphereZ[], int numSpheres) {
        //Loop through all the spheres in the scene
        for (int i = 0; i < numSpheres; i++) {
            //Add the transformation and call it a day
            sphereX[i] += corX;
            sphereY[i] += corY;
            sphereZ[i] += corZ;
        }
    }
    //Ray tracing formula
    public static void rayTrace(int pixelX, int pixelY, int eyeX, int eyeY, int eyeZ, int viewZ, int numSpheres, double sphereX[], double sphereY[], double sphereZ[], int sphereRad[],
        int sphereR[], int sphereG[], int sphereB[], int lightX[], int lightY[], int lightZ[], int numOfLights, double ka, double kd) {
        //Setup t that will be used for checking which sphere is closest
        double[] t = new double[numSpheres];
        boolean[] hit = new boolean[numSpheres];
        //Get direction (These will be updated for the pixel location when setting the RGB values)
        int dirX = pixelX;
        int dirY = pixelY;
        int dirZ = viewZ;
        //Get distance
        int dx = dirX - eyeX;
        int dy = dirY - eyeY;
        int dz = dirZ - eyeZ;
        //Loop through each sphere
        for (int i = 0; i < numSpheres; i++) {
            //Calculate A, B, and C
            int a = (int)(Math.pow(dx, 2) + Math.pow(dy, 2) + Math.pow(dz, 2));
            int b = (int)(2 * (dx) * (eyeX - sphereX[i]) + 2 * (dy) * (eyeY - sphereY[i]) + 2 * (dz) * (eyeZ - sphereZ[i]));
            int c = (int)(Math.pow(sphereX[i], 2) + Math.pow(sphereY[i], 2) + Math.pow(sphereZ[i], 2) + Math.pow(eyeX, 2) + Math.pow(eyeY, 2) + Math.pow(eyeZ, 2) +
                (-2) * ((sphereX[i] * eyeX) + (sphereY[i] * eyeY) + (sphereZ[i] * eyeZ)) - Math.pow(sphereRad[i], 2));
            //Calculate discriminant
            long bSquared = (long) Math.pow((long)(b), 2);
            long fourAC = (long)(4 * (long) a * c);
            long disc = bSquared - fourAC;

            //If discriminant is positive, mark the hit (kind of like battleship, except its raytracing!)
            if (disc >= 0)
                hit[i] = true;
            else
                hit[i] = false;
            //Calculate t
            t[i] = (-b - Math.sqrt(disc)) / (2 * a);
        }
        //Create variables for sphere coloring
        double temp = -1;
        double intersectX;
        double intersectY;
        double intersectZ;
        double normalX;
        double normalY;
        double normalZ;
        double lx = 0;
        double ly = 0;
        double lz = 0;
        double magL;
        double nl;

        for (int i = 0; i < numSpheres; i++) {
            if (hit[i] == true) {
				
                pixelX = dirX;
                pixelY = dirY;
				
                if (t[i] < temp || temp == -1) {
                    temp = t[i];
                    intersectX = eyeX + dirX * t[i];
                    intersectY = eyeY + dirY * t[i];
                    intersectZ = eyeZ + dirZ * t[i];

                    normalX = (intersectX - sphereX[i]) / sphereRad[i];
                    normalY = (intersectY - sphereY[i]) / sphereRad[i];
                    normalZ = (intersectZ - sphereZ[i]) / sphereRad[i];

                    for (int j = 0; j < numOfLights; j++) {
                        lx += lightX[j] - intersectX;
                        ly += lightY[j] - intersectY;
                        lz += lightZ[j] - intersectZ;
                    }
                    //magnitude
                    magL = Math.sqrt(Math.pow(lx, 2) + Math.pow(ly, 2) + Math.pow(lz, 2)) / numOfLights;
                    //Update lx, ly, lz
                    lx = lx / magL;
                    ly = ly / magL;
                    lz = lz / magL;

                    nl = (lx * normalX) + (ly * normalY) + (lz * normalZ);

                    pixelX += WIDTH / 2;
                    pixelY += HEIGHT / 2;
                    //Update pixel color
                    R[pixelX][pixelY] = (int)((ka * sphereR[i]) + (kd * Math.cos(nl) * sphereR[i]));
                    G[pixelX][pixelY] = (int)((ka * sphereG[i]) + (kd * Math.cos(nl) * sphereG[i]));
                    B[pixelX][pixelY] = (int)((ka * sphereB[i]) + (kd * Math.cos(nl) * sphereB[i]));
                }
            }
        }
    }


    // true: aa
    // false: straight cut
    public static void aa(boolean trueaa) {
        int x, y;
        int newx, newy;

        newx = newy = 0;

        // Init "real" array
        cleararrays(true);

        // Just cut it
        if (!trueaa) {
            for (y = 0; y < VHEIGHT; y += 2) {
                for (x = 0; x < VWIDTH; x += 2) {
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
        else {
            for (y = 0; y < VHEIGHT; y += 2) {
                for (x = 0; x < VWIDTH; x += 2) {
                    // Integer division, but I'm not inclined to
                    // worry about loss of precision here
                    R[newx][newy] = (VR[x][y] + VR[x + 1][y] +
                        VR[x][y + 1] + VR[x + 1][y + 1]) / 4;
                    G[newx][newy] = (VG[x][y] + VG[x + 1][y] +
                        VG[x][y + 1] + VG[x + 1][y + 1]) / 4;
                    B[newx][newy] = (VB[x][y] + VB[x + 1][y] +
                        VB[x][y + 1] + VB[x + 1][y + 1]) / 4;
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
    public static void cleararrays(boolean erasergb) {
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
        int red, int grn, int blu) {
        int x, y;

        VR[x0][y0] = red;
        VG[x0][y0] = grn;
        VB[x0][y0] = blu;
        VR[x1][y1] = red;
        VG[x1][y1] = grn;
        VB[x1][y1] = blu;

        y = y0;
        for (x = x0; x < x1; x++) {
            VR[x][y] = red;
            VG[x][y] = grn;
            VB[x][y] = blu;
            y++;
        }
    }



    public static void drawframe() throws Exception, IOException {
        int x, y;
        int pixcolor;
        Color pcolor;

        for (x = 0; x < WIDTH; x++) {
            for (y = 0; y < HEIGHT; y++) {

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