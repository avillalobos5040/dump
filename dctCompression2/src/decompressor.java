import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

@SuppressWarnings("unused")
public class decompressor{ //use dct-inverse to decompress from data in text file
	BufferedImage image;
	int width, height, amt, dctflag;
	int[][] Q_lum, Q_chrom, Q_unit;
	int[][][] pixelColor;
	int[][][][] imageBlocks, finalblocks;
	double[][][][] dctblocks;
	String leadline;
	BufferedImage ycb;
	public decompressor(String infile, String outfile){ //infile = outfile from compressor, outfile = name of reconstructed image
		//outfile should be of form:
		//(DriveLetter):\\folder1\\folder2\\filename.extension
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
	    Charset charset = Charset.forName("US-ASCII"); //set charset
	    Path file = FileSystems.getDefault().getPath(infile); //gets file name from path that current .class file is executed in
	    String[] triplets;
	    String[] rgb = new String[3];
	    try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
	        String line = null; int flag = 0, a = 0, b = 0;
	        while ((line = reader.readLine()) != null) {
	        	if(flag==0){
	        		leadline=line; String[] temp = leadline.trim().split(" ");
	        		dctflag = Integer.parseInt(temp[0]); amt = Integer.parseInt(temp[1]);
	        		width = Integer.parseInt(temp[2]); height = Integer.parseInt(temp[3]);
	    			imageBlocks = new int[amt][8][8][3]; //create storage for 8x8-block partition of image
	    			dctblocks = new double[amt][8][8][3]; //create parallel storage size for transformed blocks
	    			finalblocks = new int[amt][8][8][3]; //create parallel storage size for quantized dct values
	    			pixelColor = new int[width][height][3];
	        	} else {
		            //System.out.println(line); //line is individual line from file
		            line = line.trim();
		            if(line.equals("")){break;}
		            triplets = line.split(" "); //split line into comma-separated rgb triplets
		            //System.out.print(Integer.toString(triplets.length) + "\n");
		            for(int i=0;i<8;i++){
		            	rgb = triplets[i].trim().split(",");
		            	finalblocks[b][a][i][0] = Integer.parseInt(rgb[0]);
		            	finalblocks[b][a][i][1] = Integer.parseInt(rgb[1]);
		            	finalblocks[b][a][i][2] = Integer.parseInt(rgb[2]);
		            } //split triplets into rgb values
		            a++; if(a==8){ b++; a=0; } //a = block height, b = block count
	        	} 
	            flag = 1; //grab 1st line, then ignore the rest
	        }
	    } catch (IOException x) {
	        System.err.format("IOException: %s%n", x);
	    }
	    for(int i=0;i<amt;i++){
			int[][] red = new int[8][8], green = new int[8][8], blue = new int[8][8];
			int[][] finalred = new int[8][8], finalgreen = new int[8][8], finalblue = new int[8][8];
			int[][] dctred = new int[8][8], dctgreen = new int[8][8], dctblue = new int[8][8];
			//store blocks in final(RGB), inverse quantize into dct(RGB), then inverse dct into (RGB)
			//finally, store (RGB) in 'imageBlocks'
			for(int a=0;a<8;a++){
				for(int b=0;b<8;b++){
					finalred[a][b] = finalblocks[i][a][b][0];
					finalgreen[a][b] = finalblocks[i][a][b][1];
					finalblue[a][b] = finalblocks[i][a][b][2];
				}
			} //store quantized values
			if(dctflag==1){ //YCbCr inverse quantization
				dctred = quantizeInverse(finalred, Q_lum);
				dctgreen = quantizeInverse(finalgreen, Q_chrom);
				dctblue = quantizeInverse(finalblue, Q_chrom);
			}else{ //RGB inverse quantization
				dctred = quantizeInverse(finalred, Q_unit);
				dctgreen = quantizeInverse(finalgreen, Q_unit);
				dctblue = quantizeInverse(finalblue, Q_unit);
			} //inverse quantization, store
			red = dctInverse(dctred); green = dctInverse(dctgreen); blue = dctInverse(dctblue);
			//invert dct to get RGB values
			for(int a=0;a<8;a++){
				for(int b=0;b<8;b++){
					imageBlocks[i][a][b][0] = red[a][b];
					imageBlocks[i][a][b][1] = green[a][b];
					imageBlocks[i][a][b][2] = blue[a][b];
				}
			} //push color components to storage
		} //invert 'finalblocks' into 'imageBlocks'
	    int count=0;
	    for(int a=0;a<(width/8);a++){
			for(int b=0;b<(height/8);b++){
				for(int i=0;i<8;i++){
					for(int j=0;j<8;j++){
						pixelColor[(a*8)+i][(b*8)+j][0] = imageBlocks[count][i][j][0];
						pixelColor[(a*8)+i][(b*8)+j][1] = imageBlocks[count][i][j][1];
						pixelColor[(a*8)+i][(b*8)+j][2] = imageBlocks[count][i][j][2];
					}
				} count++;
			}
		} //write 'imageBlocks' to 'pixelColor'
	    BufferedImage ycb =  new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
	    for(int i=0;i<width;i++){
			for(int j=0;j<height;j++){
				int val = (pixelColor[i][j][0]<<16) | (pixelColor[i][j][1]<<8) | pixelColor[i][j][2]; //format as single int
				ycb.setRGB(i,j,val); //set value
			}
		}
	    try { ImageIO.write(ycb,"png", new File(outfile)); } catch (IOException e) { e.printStackTrace(); } //write image
	}
	public int[][] dctInverse(int[][] block){
		int[][] dctInverse = new int[8][8]; //create dctInverse storage
		double inner = 0;
		for(int i=0;i<dctInverse.length;i++){ //i=x
			for(int j=0;j<dctInverse[0].length;j++){ //j=y
				inner = 0;
				for(int l=0;l<8;l++){ //l=u
					for(int m=0;m<8;m++){ //m=v
						inner += alpha(l)*alpha(m)*block[l][m]*Math.cos((((2*i)+1)*l*Math.PI)/16)*Math.cos((((2*j)+1)*m*Math.PI)/16);
					}
				}
				dctInverse[i][j] = (int)((0.25)*(inner));
				dctInverse[i][j] += 128;  //map value from [-128,127] to [0,255]
			}
		}
		return dctInverse;
	}
	public double alpha(double n){return (n==0)?(1/(Math.sqrt(2))):1;} //used by DCT
	public int round(double arg){long ipart = (long)arg; double fpart = (arg-ipart); return (fpart<0.5)?(int)ipart:(int)(ipart+1);} //used by quantize
	public int[][] quantizeInverse(int[][] quant, int[][] q){
		int[][] coeffs = new int[8][8];
		for(int i=0;i<8;i++){
			for(int j=0;j<8;j++){
				coeffs[i][j] = (quant[i][j] * q[i][j]);
			}
		}
		return coeffs;
	}
}