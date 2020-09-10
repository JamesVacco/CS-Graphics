public class spherelist
{
	double	cx, cy, cz, radius;
	int	r, g, b;

	public spherelist (double icx, double icy, double icz, double iradius,
		String foo, 	//This is to facilitate reading. It eats the
				// word COLOR
		int ir, int ig, int ib)
	{
		this.cx = icx;
		this.cy = icy;
		this.cz = icz;
		this.radius = iradius;
		this.r = ir;
		this.g = ig;
		this.b = ib;
	}
	public void printinfo()
	{
		System.out.printf ("Sphere-->%.2f %.2f %.2f %d %d %d\n",
			cx,cy,cz,r,g,b);
	}

	
}