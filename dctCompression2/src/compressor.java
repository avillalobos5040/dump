import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

public class compressor{ //use dct to compress
	BufferedImage image;
	BufferedWriter bw;
	FileWriter fw;
	int width, height, amt;
	int[][] Q_lum, Q_chrom, Q_unit;
	int[][][] pixelColor;
	int[][][][] imageBlocks, finalblocks;
	public compressor(String infile, String outfile, int flag){
		//outfile should be of form:
		//(DriveLetter):\\folder1\\folder2\\filename.extension
		if((flag < 0)||(flag > 1)){flag = 1;} //0=rgb, 1=ycbcr
		Q_lum = new int[][] {{16,11,10,16,24,40,51,61},
				 {12,12,14,19,26,58,60,55},
				 {14,13,16,24,40,57,69,56},
				 {14,17,22,29,51,87,80,62},
				 {18,22,37,56,68,109,103,77},
				 {24,36,55,64,81,104,113,92},
				 {49,64,78,87,103,121,120,101},
				 {72,92,95,98,112,100,103,99}};
		//default JPEG luminance quantization table
		Q_chrom = new int[][] {{17,18,24,47,99,99,99,99},
			 {18,21,26,66,99,99,99,99},
			 {24,26,56,99,99,99,99,99},
			 {47,66,99,99,99,99,99,99},
			 {99,99,99,99,99,99,99,99},
			 {99,99,99,99,99,99,99,99},
			 {99,99,99,99,99,99,99,99},
			 {99,99,99,99,99,99,99,99}};
		//default JPEG chrominance quantization table
		Q_unit = new int[][] {{1,1,1,1,1,1,1,1},
				 {1,1,1,1,1,1,1,1},
				 {1,1,1,1,1,1,1,1},
				 {1,1,1,1,1,1,1,1},
				 {1,1,1,1,1,1,1,1},
				 {1,1,1,1,1,1,1,1},
				 {1,1,1,1,1,1,1,1},
				 {1,1,1,1,1,1,1,1}};
		//unit quantization table
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
					} count++;
				}
			} //imageBlocks is a partition of pixelColor into 8x8 RGB blocks
			for(int i=0;i<amt;i++){
				int[][] red = new int[8][8], green = new int[8][8], blue = new int[8][8];
				int[][] finalred = new int[8][8], finalgreen = new int[8][8], finalblue = new int[8][8];
				double[][] dctred = new double[8][8], dctgreen = new double[8][8], dctblue = new double[8][8];
				for(int a=0;a<8;a++){
					for(int b=0;b<8;b++){
						red[a][b] = imageBlocks[i][a][b][0];
						green[a][b] = imageBlocks[i][a][b][1];
						blue[a][b] = imageBlocks[i][a][b][2];
					}
				} //pull out color components
				dctred = dct(red); dctgreen = dct(green); dctblue = dct(blue); //run DCT on components
				if(flag==1){ //YCbCr quantization
					finalred = quantize(dctred, Q_lum);
					finalgreen = quantize(dctgreen, Q_chrom);
					finalblue = quantize(dctblue, Q_chrom);
				}else{ //RGB quantization
					finalred = quantize(dctred, Q_unit);
					finalgreen = quantize(dctgreen, Q_unit);
					finalblue = quantize(dctblue, Q_unit);
				} //quantize, store in 'finalblocks'
				for(int a=0;a<8;a++){
					for(int b=0;b<8;b++){
						finalblocks[i][a][b][0] = finalred[a][b];
						finalblocks[i][a][b][1] = finalgreen[a][b];
						finalblocks[i][a][b][2] = finalblue[a][b];
					}
				} //store quantized values
			} //run DCT into 'dctblocks'; quantize, store in 'finalblocks'
		}catch(Exception e){System.out.println("Image conversion error!");}
		try{
			String content = Integer.toString(flag)+" "+Integer.toString(amt)+" "+Integer.toString(width)+" "+Integer.toString(height)+"\n";
			fw = new FileWriter(outfile);
			bw = new BufferedWriter(fw);
			bw.write(content); content = ""; //write flag value to first line, clear line
			for(int i=0;i<amt;i++){
				for(int a=0;a<8;a++){
					for(int b=0;b<8;b++){
						content += Integer.toString(finalblocks[i][a][b][0])+",";
						content += Integer.toString(finalblocks[i][a][b][1])+",";
						content += Integer.toString(finalblocks[i][a][b][2])+" ";
					}
					String str = (i!=(amt))?"\n":"";
					bw.write(content+str); //write line
					content = ""; //clear line
				}
				content = ""; //clear line (just to be extra sure)
			} //write 'finalblocks' to text file
		}catch(IOException e){ e.printStackTrace(); }finally{
			try{
				if(bw!=null){bw.close();}
				if(fw!=null){fw.close();}
			}catch(IOException ex){ ex.printStackTrace(); }
		}
	}
	public double[][] dct(int[][] block) throws Exception{
		if(block.length != 8){if(block[0].length != 8){ throw new Exception("Block size out of range!"); }}
		int[][] normalized = new int[8][8];
		for(int i=0;i<8;i++){ for(int j=0;j<8;j++){ normalized[i][j] = ((block[i][j])-128); } } //map pixel values from [0,255] to [-128,127]
		double[][] dct = new double[8][8]; //create DCT storage
		double inner = 0;
		for(int i=0;i<dct.length;i++){ //i=u
			for(int j=0;j<dct[0].length;j++){ //j=v
				inner = 0;
				for(int l=0;l<8;l++){ //l=x
					for(int m=0;m<8;m++){ //m=y
						inner += normalized[l][m]*Math.cos((((2*l)+1)*i*Math.PI)/16)*Math.cos((((2*m)+1)*j*Math.PI)/16);
					}
				}
				dct[i][j] = (0.25)*alpha(i)*alpha(j)*(inner);
			}
		}
		//
		return dct;
	}
	public double alpha(double n){return (n==0)?(1/(Math.sqrt(2))):1;} //used by DCT
	public int round(double arg){long ipart = (long)arg; double fpart = (arg-ipart); return (fpart<0.5)?(int)ipart:(int)(ipart+1);} //used by quantize
	public int[][] quantize(double[][] coeffs, int[][] q) throws Exception{
		if(coeffs.length != 8){if(coeffs[0].length != 8){ throw new Exception("Block size out of range!"); }}
		int[][] quant = new int[8][8];
		for(int i=0;i<8;i++){
			for(int j=0;j<8;j++){
				quant[i][j] = round((double)coeffs[i][j] / (double)q[i][j]);
			}
		}
		return quant;
	}
}