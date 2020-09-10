public class Mandlebrot
{
	//Mandelbrot variables
	double zx, zy, cx, cy, tmp;
	int X,Y, maxiter, iter;
	
	//Color array to hold rgb values
	int[] rgb = new int[3];

	//Set mandelbrot values
	public Mandlebrot (int x, int y, int MAX_ITER, double cX, double cY)
	{
		this.X = x;
		this.Y = y;
		this.maxiter = MAX_ITER;
		this.cx = cX;
		this.cy = cY;
	}
	
	//Performs Mandlebrot calculation for specific pixel
	public int[] pixelColor()
	{	
		//Mandelbrot Set calculation
		iter = 0;
		zx = zy = 0;
		while (zx * zx + zy * zy < 4 && iter < maxiter) {
			tmp = zx * zx - zy * zy + cx;
			zy = 2.0 * zx * zy + cy;
			zx = tmp;
			iter++;
		}
		
		//Color palette selection 
		if(iter < maxiter && iter > 0)
		{   
			int c = iter % 16;
			switch (c)
			{
				case 0:
					rgb[0] = 75;
					rgb[1] = 50;
					rgb[2] = 100;
					break;
				case 1:
					rgb[0] = 30;
					rgb[1] = 150;
					rgb[2] = 200;
					break;
				case 2:
					rgb[0] = 40;
					rgb[1] = 10;
					rgb[2] = 100;
					break;
				case 3:
					rgb[0] = 10;
					rgb[1] = 10;
					rgb[2] = 150;
					break;
				case 4:
					rgb[0] = 0;
					rgb[1] = 80;
					rgb[2] = 245;
					break;
				case 5:
					rgb[0] = 0;
					rgb[1] = 200;
					rgb[2] = 200;
					break;
				case 6:
					rgb[0] = 255;
					rgb[1] = 154;
					rgb[2] = 0;
					break;
				case 7:
					rgb[0] = 40;
					rgb[1] = 125;
					rgb[2] = 210;
					break;
				case 8:
					rgb[0] = 134;
					rgb[1] = 181;
					rgb[2] = 229;
					break;
				case 9:
					rgb[0] = 210;
					rgb[1] = 240;
					rgb[2] = 250;
					break;
				case 10:
					rgb[0] = 250;
					rgb[1] = 230;
					rgb[2] = 100;
					break;
				case 11:
					rgb[0] = 248;
					rgb[1] = 201;
					rgb[2] = 95;
					break;
				case 12:
					rgb[0] = 255;
					rgb[1] = 170;
					rgb[2] = 0;
					break;
				case 13:
					rgb[0] = 0;
					rgb[1] = 130;
					rgb[2] = 220;
					break;
				case 14:
					rgb[0] = 0;
					rgb[1] = 100;
					rgb[2] = 200;
					break;
				case 15:	
					rgb[0] = 3;
					rgb[1] = 45;
					rgb[2] = 110;
					break;
			}
		}
		return rgb;
	}
}