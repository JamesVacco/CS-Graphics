public class lightlist
{
	double	lx, ly, lz;
	int	r, g, b;

	public lightlist (double ilx, double ily, double ilz,
		String foo, 	//This is to facilitate reading. It eats the
		// word COLOR
		int ir, int ig, int ib)
	{
		this.lx = ilx;
		this.ly = ily;
		this.lz = ilz;
		this.r = ir;
		this.g = ig;
		this.b = ib;
	}
	public void printinfo()
	{
		System.out.printf ("Sphere-->%.2f %.2f %.2f %d %d %d\n",
			lx,ly,lz,r,g,b);
	}

	
}