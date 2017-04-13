import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
public class comparator{
	BufferedImage imageA, imageB;
	int height, width;
	int[][][] rgbA, rgbB, diff;
	public comparator(String fileA, String fileB){
		try{
			File inputA = new File(fileA);
			imageA = ImageIO.read(inputA);
			width = imageA.getWidth();
			height = imageA.getHeight();
			rgbA = new int[width][height][3];
			//int count = 0; //pixel count
			for(int i=0;i<width;i++){
				for(int j=0;j<height;j++){
					//count++;
					Color c = new Color(imageA.getRGB(i, j)); //grab data from image
					int red = c.getRed(), green = c.getGreen(), blue = c.getBlue(); //grab pixel data
					rgbA[i][j][0] = red; rgbA[i][j][1] = green; rgbA[i][j][2] = blue; //store pixel data
				}
			}
			System.out.println("Image #1 Read!");
		}catch(Exception e){ System.out.println(e); }
		try{
			File inputB = new File(fileB);
			imageB = ImageIO.read(inputB);
			if(height != imageB.getHeight()){
				if(width != imageB.getWidth()){
					throw new Exception("Image dimension mismatch!");
				}
			}
			rgbB = new int[width][height][3];
			//int count = 0; //pixel count
			for(int i=0;i<width;i++){
				for(int j=0;j<height;j++){
					//count++;
					Color c = new Color(imageB.getRGB(i, j)); //grab data from image
					int red = c.getRed(), green = c.getGreen(), blue = c.getBlue(); //grab pixel data
					rgbB[i][j][0] = red; rgbB[i][j][1] = green; rgbB[i][j][2] = blue; //store pixel data
				}
			}
			System.out.println("Image #2 Read!");
		}catch(Exception e){ System.out.println(e); }
		diff = new int[width][height][3];
		for(int i=0;i<width;i++){
			for(int j=0;j<height;j++){
				for(int k=0;k<3;k++){
					diff[i][j][k] = abs(rgbA[i][j][k] - rgbB[i][j][k]);
				}
			}
		}
	}
	public int[][][] getDiff(){return this.diff;}
	private int abs(int n){ return (n>0) ? n : -1*n; }
}