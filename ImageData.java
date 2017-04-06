import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
@SuppressWarnings("unused")
class ImageData{
	BufferedImage image;
	int width, height;
	public static void main(String[] args) throws Exception{
		ImageData obj = new ImageData();
	}
	public int[] RGBtoYCC(int r, int g, int b){
		int[] ycbcr = new int[3];
		ycbcr[0] = (int)(0.299*(double)r+0.587*(double)g+0.114*(double)b);
		ycbcr[1] = 128 + (int)(-0.168736*r-0.331264*g+0.5*b);
		ycbcr[2] = 128 + (int)(0.5*r-0.418688*g-0.081312*b);
		return ycbcr;
	}
	public int[] YCCtoRGB(int y, int cb, int cr){
		int[] rgb = new int[3];
		rgb[0] = (int)((double)y + 1.402 * ((double)cr - 128));
		rgb[1] = (int)((double)y - 0.344136 * ((double)cb - 128) - 0.714136 * ((double)cr - 128));
		rgb[2] = (int)((double)y + 1.772 * ((double)cb - 128));
		return rgb;
	}
	public ImageData(){
		try{
			File input = new File("Lenna_YCC.png");
			image = ImageIO.read(input);
			width = image.getWidth();
			height = image.getHeight();
			BufferedImage ycb =  new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
			int count = 0;
			for(int i=0;i<width;i++){
				for(int j=0;j<height;j++){
					count++;
					Color c = new Color(image.getRGB(i, j)); //grab data from image
					int red = c.getRed(), green = c.getGreen(), blue = c.getBlue(); //grab pixel data
					int[] converted = YCCtoRGB(red, green, blue); //run conversion
					int val = (converted[0]<<16) | (converted[1]<<8) | converted[2]; //format numbers as single int
					ycb.setRGB(i,j,val);
					//System.out.println("Pixel No. "+count+"  Red: "+red+" Green: "+green+" Blue: "+blue);
				}
			}
			System.out.println("Done!");
			ImageIO.write(ycb,"png", new File("Lenna_RGB.png"));
		} catch(Exception e){ System.out.println(e); }
	}
}