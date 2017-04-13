public class runDCT{
	public static void main(String[] args){// This runs sh*t.
		String corestr = "droplet";
		new rgbTOycbcr(corestr+".png",corestr+"_YCC.png");
		/*//comparator comp = new comparator(corestr+".png",corestr+"_RGB.png");
		int[][][] d = comp.getDiff();
		System.out.println("Length of d: "+Integer.toString(d.length));
		System.out.println("Length of d[0]: "+Integer.toString(d[0].length));
		System.out.println("Length of d[0][0]: "+Integer.toString(d[0][0].length));
		System.out.println("Contents of d:");
		for(int i=0;i < d.length;i++){
			for(int j=0;j < d[0].length;j++){
				for(int k=0;k < d[0][0].length;k++){
					//if(d[i][j][k]!=0){ System.out.println(Integer.toString(d[i][j][k])+" at "+String.format("(%d,%d,%d)",i,j,k)); }
					System.out.print(Integer.toString(d[i][j][k])+" "); if(k==2){System.out.println("");}
				}
			}
		} System.out.println("");*/
		new compressor(corestr+"_YCC.png", corestr+"_DCT.txt", 1); //create DCT compressor, send output to file
		new decompressor(corestr+"_DCT.txt",  corestr+"_INV.png"); //create DCT decompressor, use file output to reconstruct image
		new ycbcrTOrgb(corestr+"_INV.png",corestr+"_RGB.png");
		System.out.println("Done!");
	}
}