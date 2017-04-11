import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ycbcrTOrgb{
	BufferedImage image;
	int width, height;
	public ycbcrTOrgb(String infile, String outfile){
		try{
			File input = new File(infile);
			image = ImageIO.read(input);
			width = image.getWidth();
			height = image.getHeight();
			BufferedImage ycb =  new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
			//int count = 0; //counts pixels
			for(int i=0;i<width;i++){
				for(int j=0;j<height;j++){
					//count++;
					Color c = new Color(image.getRGB(i, j)); //grab data from image
					int red = c.getRed(), green = c.getGreen(), blue = c.getBlue(); //grab pixel data
					int[] converted = YCCtoRGB(red, green, blue); //run conversion
					int val = (converted[0]<<16) | (converted[1]<<8) | converted[2]; //format as single int
					ycb.setRGB(i,j,val);
					//System.out.println("Pixel No. "+count+"  Red: "+red+" Green: "+green+" Blue: "+blue);
				}
			}
			System.out.println("RGB Conversion Done!");
			ImageIO.write(ycb,"png", new File(outfile));
		} catch(Exception e){ System.out.println(e); }
	}
	public int[] YCCtoRGB(int y, int cb, int cr){
		int[] rgb = new int[3];
		rgb[0] = (int)((double)y + 1.402 * ((double)cr - 128));
		rgb[1] = (int)((double)y - 0.344136 * ((double)cb - 128) - 0.714136 * ((double)cr - 128));
		rgb[2] = (int)((double)y + 1.772 * ((double)cb - 128));
		return rgb;
	}
}