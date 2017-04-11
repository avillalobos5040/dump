import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class rgbTOycbcr{
	BufferedImage image;
	int width, height;
	public rgbTOycbcr(String infile, String outfile){
		try{
			File input = new File(infile);
			image = ImageIO.read(input);
			width = image.getWidth();
			height = image.getHeight();
			BufferedImage rgb =  new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
			//int count = 0; //counts pixels
			for(int i=0;i<width;i++){
				for(int j=0;j<height;j++){
					//count++;
					Color c = new Color(image.getRGB(i, j)); //grab data from image
					int red = c.getRed(), green = c.getGreen(), blue = c.getBlue(); //grab pixel data
					int[] converted = RGBtoYCC(red, green, blue); //run conversion
					int val = (converted[0]<<16) | (converted[1]<<8) | converted[2]; //format as single int
					rgb.setRGB(i,j,val);
					//System.out.println("Pixel No. "+count+"  Red: "+red+" Green: "+green+" Blue: "+blue);
				}
			}
			System.out.println("YCbCr Conversion Done!");
			ImageIO.write(rgb,"png", new File(outfile));
		} catch(Exception e){ System.out.println(e); }
	}
	public int[] RGBtoYCC(int r, int g, int b){
		int[] ycbcr = new int[3];
		ycbcr[0] = (int)(0.299*(double)r+0.587*(double)g+0.114*(double)b);
		ycbcr[1] = 128 + (int)(-0.168736*r-0.331264*g+0.5*b);
		ycbcr[2] = 128 + (int)(0.5*r-0.418688*g-0.081312*b);
		return ycbcr;
	}
//	public int[] RGBtoYCC(int r, int g, int b){
//		int[] ycbcr = new int[3];
//		ycbcr[0] = (int)(((double)r*77/256)+((double)g*150/256)+((double)b*29/256));
//		ycbcr[1] = 128 + (int)((-1*(double)r*44/256)+(-1*(double)g*87/256)+((double)b*131/256));
//		ycbcr[2] = 128 + (int)(((double)r*131/256)+(-1*(double)g*110/256)+(-1*(double)b*21/256));
//		return ycbcr;
//	}
}