import java.util.PriorityQueue;

/**
 * Although this class has a history of several years,
 * it is starting from a blank-slate, new and clean implementation
 * as of Fall 2018.
 * <P>
 * Changes include relying solely on a tree for header information
 * and including debug and bits read/written information
 * 
 * @author Owen Astrachan
 */

public class HuffProcessor {

	public static final int BITS_PER_WORD = 8;
	public static final int BITS_PER_INT = 32;
	public static final int ALPH_SIZE = (1 << BITS_PER_WORD); 
	public static final int PSEUDO_EOF = ALPH_SIZE;
	public static final int HUFF_NUMBER = 0xface8200;
	public static final int HUFF_TREE  = HUFF_NUMBER | 1;

	private final int myDebugLevel;
	
	public static final int DEBUG_HIGH = 4;
	public static final int DEBUG_LOW = 1;
	
	public HuffProcessor() {
		this(0);
	}
	
	public HuffProcessor(int debug) {
		myDebugLevel = debug;
	}

	/**
	 * Compresses a file. Process must be reversible and loss-less.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be compressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void compress(BitInputStream in, BitOutputStream out){

		while (true){
			int val = in.readBits(BITS_PER_WORD);
			if (val == -1) break;
			out.writeBits(BITS_PER_WORD, val);
		}
		out.close();
	}
	/**
	 * Decompresses a file. Output file must be identical bit-by-bit to the
	 * original.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be decompressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void decompress(BitInputStream in, BitOutputStream out){
		int bits = in.readBits(BITS_PER_INT);
		if (bits != HUFF_TREE) {
			throw new HuffException("illegal header starts with " + bits);
		}
		HuffNode root = readTreeHeader(in);
		readCompressedBits(root, in, out);
		out.close();
	}
	

	
	
	private HuffNode readTreeHeader(BitInputStream in) {
		int bit = in.readBits(1);
		
		if (bit == -1) {
			throw new HuffException("Nothing Was Read");
		}
		
		if (bit == 0){
		    HuffNode left = readTreeHeader(in);
		    HuffNode right = readTreeHeader(in);
		    HuffNode internal = new HuffNode(-1, 0, left, right); 
		    return internal;
		}
		
		HuffNode leaf = new HuffNode(in.readBits(BITS_PER_WORD + 1), 0);
		return leaf;
	}
	
	/*
	 * Reads the BitInputStream and uses these bits to traverse the tree.
	 * 0 = left. 1 = right.
	 * Once you reach a leaf, write out the value stored within it to BitOutputStream.
	 */
	private void readCompressedBits(HuffNode root, BitInputStream in, BitOutputStream out) {
		if (root == null) return;
		HuffNode curr = root;
		while (true) {
			if (curr.myValue != -1) {
				if (curr.myValue == PSEUDO_EOF) {
					break;
				}
				out.writeBits(BITS_PER_WORD, curr.myValue);
				curr = root;
			}
			int bit = in.readBits(1);
			if (bit == -1) {
				throw new HuffException("invalid bit");
			}
			else if (bit == 0) {
				curr = curr.myLeft;
			}
			else if (bit == 1) {
				curr = curr.myRight;
			}
			if (curr == null) {
				throw new HuffException("PSEUDO_EOF never reached");
			}
			
		}
	}

    
}
	
	
