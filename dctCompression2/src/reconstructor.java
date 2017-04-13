import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

public class reconstructor{ //use dct to compress
	BufferedImage image;
	BufferedWriter bw;
	FileWriter fw;
	int width, height, amt;
	int[][][] pixelColor;
	int[][][][] imageBlocks, finalblocks;
	public reconstructor(String infile, String outfile){
		//outfile should be of form:
		//(DriveLetter):\\folder1\\folder2\\filename.extension
		try{
			File input = new File(infile);
			image = ImageIO.read(input);
			width = image.getWidth();
			height = image.getHeight();
			pixelColor = new int[width][height][3];
			if((width%8)!=0){if((height%8)!=0){throw new Exception("Image size not divisible by 8!");}}
			amt = ((width/8)*(height/8)); //number of 8x8 blocks in image
			imageBlocks = new int[amt][8][8][3]; //create storage for 8x8-block partition of image
			finalblocks = new int[amt][8][8][3]; //create parallel storage size for quantized dct values
			for(int i=0;i<width;i++){
				for(int j=0;j<height;j++){
					Color c = new Color(image.getRGB(i, j)); //grab data from image
					pixelColor[i][j][0] = c.getRed();    // -> this is the luminance value in a YCbCr image
					pixelColor[i][j][1] = c.getGreen();  // -> this is the blue chrominance value in a YCbCr image
					pixelColor[i][j][2] = c.getBlue();   // -> this is the red chrominance value in a YCbCr image
				}
			} //all pixel data stored in pixelColor
			int count = 0;
			for(int a=0;a<(width/8);a++){
				for(int b=0;b<(height/8);b++){
					for(int i=0;i<8;i++){
						for(int j=0;j<8;j++){
							imageBlocks[count][i][j][0]=pixelColor[(a*8)+i][(b*8)+j][0];
							imageBlocks[count][i][j][1]=pixelColor[(a*8)+i][(b*8)+j][1];
							imageBlocks[count][i][j][2]=pixelColor[(a*8)+i][(b*8)+j][2];
							
						}
					}
					count++;
				}
			} //imageBlocks is a partition of pixelColor into 8x8 RGB blocks
			count = 0;
			for(int a=0;a<(width/8);a++){
				for(int b=0;b<(height/8);b++){
					for(int i=0;i<8;i++){
						for(int j=0;j<8;j++){
							pixelColor[(a*8)+i][(b*8)+j][0]=imageBlocks[count][i][j][0];
							pixelColor[(a*8)+i][(b*8)+j][1]=imageBlocks[count][i][j][1];
							pixelColor[(a*8)+i][(b*8)+j][2]=imageBlocks[count][i][j][2];
						}
					}
					count++;
				}
			} //test reconstruction
		}catch(Exception e){System.out.println("Image conversion error!"); e.printStackTrace();}
		BufferedImage ycb =  new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
	    for(int i=0;i<width;i++){
			for(int j=0;j<height;j++){
				int val = (pixelColor[i][j][0]<<16) | (pixelColor[i][j][1]<<8) | pixelColor[i][j][2]; //format as single int
				ycb.setRGB(i,j,val); //set value
			}
		}
	    try { ImageIO.write(ycb,"png", new File(outfile)); } catch (IOException e) { e.printStackTrace(); } //write image
	}
}