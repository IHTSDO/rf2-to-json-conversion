package org.ihtsdo.json.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class CommonUtils {
	private static final String encoding = "8859_1";
	
	public static String[] getSmallestArray(HashMap<BufferedReader, String[]> passedMap, int[] sortColumns) {

		List<String[]> mapValues = new ArrayList<String[]>(passedMap.values());
		//
		Collections.sort(mapValues, new ArrayComparator(sortColumns, false));

		return mapValues.get(0);
	}
	private static byte[] getRawBytes(UUID uid) {
		String id = uid.toString();
		if (id.length() != 36) {
			throw new NumberFormatException("UUID has to be represented by the standard 36-char representation");
		}
		byte[] rawBytes = new byte[16];

		for (int i = 0, j = 0; i < 36; ++j) {
			// Need to bypass hyphens:
			switch (i) {
			case 8:
			case 13:
			case 18:
			case 23:
				if (id.charAt(i) != '-') {
					throw new NumberFormatException("UUID has to be represented by the standard 36-char representation");
				}
				++i;
			}
			char c = id.charAt(i);

			if (c >= '0' && c <= '9') {
				rawBytes[j] = (byte) ((c - '0') << 4);
			} else if (c >= 'a' && c <= 'f') {
				rawBytes[j] = (byte) ((c - 'a' + 10) << 4);
			} else if (c >= 'A' && c <= 'F') {
				rawBytes[j] = (byte) ((c - 'A' + 10) << 4);
			} else {
				throw new NumberFormatException("Non-hex character '" + c + "'");
			}

			c = id.charAt(++i);

			if (c >= '0' && c <= '9') {
				rawBytes[j] |= (byte) (c - '0');
			} else if (c >= 'a' && c <= 'f') {
				rawBytes[j] |= (byte) (c - 'a' + 10);
			} else if (c >= 'A' && c <= 'F') {
				rawBytes[j] |= (byte) (c - 'A' + 10);
			} else {
				throw new NumberFormatException("Non-hex character '" + c + "'");
			}
			++i;
		}
		return rawBytes;
	}

	public static UUID get(UUID namespace, String name) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest sha1Algorithm = MessageDigest.getInstance("SHA-1");

		// Generate the digest.
		sha1Algorithm.reset();
		if (namespace != null) {
			sha1Algorithm.update(getRawBytes(namespace));
		}
		sha1Algorithm.update(name.getBytes(encoding));
		byte[] sha1digest = sha1Algorithm.digest();

		sha1digest[6] &= 0x0f; /* clear version */
		sha1digest[6] |= 0x50; /* set to version 5 */
		sha1digest[8] &= 0x3f; /* clear variant */
		sha1digest[8] |= 0x80; /* set to IETF variant */

		long msb = 0;
		long lsb = 0;
		for (int i = 0; i < 8; i++)
			msb = (msb << 8) | (sha1digest[i] & 0xff);
		for (int i = 8; i < 16; i++)
			lsb = (lsb << 8) | (sha1digest[i] & 0xff);

		return new UUID(msb, lsb);
	}

	public static void MergeFile(HashSet<File> hFile, File outputfile) {

		try{
			if (outputfile.exists())
				outputfile.delete();
			
			outputfile.createNewFile();

			String fileName=outputfile.getName();
			File fTmp = new File(outputfile.getParentFile()  + "/tmp_" + fileName);


			boolean first = true;
			String nextLine;
			for (File file:hFile){


				if (fTmp.exists())
					fTmp.delete();
				
				FileOutputStream fos = new FileOutputStream( fTmp);
				OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
				BufferedWriter bw = new BufferedWriter(osw);

				FileInputStream fis = new FileInputStream(file	);
				InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
				BufferedReader br = new BufferedReader(isr);

				FileInputStream ofis = new FileInputStream(outputfile	);
				InputStreamReader oisr = new InputStreamReader(ofis,"UTF-8");
				BufferedReader obr = new BufferedReader(oisr);

				
				nextLine=br.readLine();
				if (first && nextLine!=null){
					bw.append(nextLine);
					bw.append("\r\n");
					first=false;
				}

				while ((nextLine=obr.readLine())!=null){
					bw.append(nextLine);
					bw.append("\r\n");

				}
				while ((nextLine=br.readLine())!=null){
					bw.append(nextLine);
					bw.append("\r\n");

				}
				bw.close();
				br.close();
				

				if (outputfile.exists())
					outputfile.delete();
				fTmp.renameTo(outputfile) ;
			}

			if (fTmp.exists())
				fTmp.delete();

		} catch (IOException e) {
			e.printStackTrace();
		}finally{

		}
	}
	 public static UUID fromSNOMED(String id) {
	        String name = "org.snomed." + id;
	        try {
	            return UUID.nameUUIDFromBytes(name.getBytes(encoding));
	        } catch (UnsupportedEncodingException e) {
	            throw new RuntimeException(e);
	        }
	    }
	 /**
		 * Concatenate files.
		 *
		 * @param hFile the set with files to concatenate
		 * @param outputfile the outputfile
		 */
		public static void concatFile(HashSet<File> hFile, File outputfile) {

			try{
				
				String fileName=outputfile.getName();
				File fTmp = new File(outputfile.getParentFile()  + "/tmp_" + fileName);

				if (fTmp.exists())
					fTmp.delete();
				
				FileOutputStream fos = new FileOutputStream( fTmp);
				OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
				BufferedWriter bw = new BufferedWriter(osw);

				boolean first = true;
				String nextLine;
				for (File file:hFile){

					FileInputStream fis = new FileInputStream(file	);
					InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
					BufferedReader br = new BufferedReader(isr);
					
					nextLine=br.readLine();
					if (first && nextLine!=null){
						bw.append(nextLine);
						bw.append("\r\n");
						first=false;
					}

					while ((nextLine=br.readLine())!=null){
						bw.append(nextLine);
						bw.append("\r\n");

					}
					br.close();
					isr.close();
					fis.close();
					br=null;
					isr=null;
					fis=null;

				}

				bw.close();

				if (outputfile.exists())
					outputfile.delete();
				fTmp.renameTo(outputfile) ;
				
				if (fTmp.exists())
					fTmp.delete();

			} catch (IOException e) {
				e.printStackTrace();
			}finally{

			}
		}
}
