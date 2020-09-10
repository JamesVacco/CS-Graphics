public class hist implements Comparable<hist>
{
	int	colorvalue;
	int	count;

	public hist(int a, int b)
	{
		this.count = a;
		this.colorvalue = b;
	}
	
	public int compareTo(hist in)
	{
		return in.count - this.count;
	}
}