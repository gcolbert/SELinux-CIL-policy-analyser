package fr.tools.archives.bunzip2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.netkernel.layer0.nkf.INKFRequestContext;
import org.netkernel.module.standard.endpoint.StandardAccessorImpl;

/**
 * Service accessor for Bunzip2.
 */
public class Bunzip2Accessor extends StandardAccessorImpl {

	private static final int bufferSize = 8192;
	
	public void onSource(INKFRequestContext context) throws Exception {
		// Support one or more operand argument instances
		int numberOfArguments = context.getThisRequest().getArgumentCount();
		String argumentName = "";
		String argumentValue = "";
		File operand = null;

		for (int i = 0; i < numberOfArguments; i++) {
			argumentName = context.getThisRequest().getArgumentName(i);
			if ("operand".equals(argumentName)) {
				argumentValue = context.getThisRequest().getArgumentValue(i);
				operand = context.source(argumentValue, File.class);
			}
		}

	    FileInputStream inputStream = new FileInputStream(operand.getPath());
		context.createResponseFrom(bunzip2(inputStream).toString());
	}

	private void uncompress(CompressorInputStream compressorInputStream, 
			ByteArrayOutputStream byteArrayOutputStream) throws IOException {
    	final byte[] buffer = new byte[bufferSize];
    	int n = 0;
    	while (-1 != (n = compressorInputStream.read(buffer))) {
    		byteArrayOutputStream.write(buffer, 0, n);
    	}
    	compressorInputStream.close();
    	byteArrayOutputStream.close();
	}

	private ByteArrayOutputStream bunzip2(FileInputStream inputStream) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		BZip2CompressorInputStream bZip2CompressorInputStream = new BZip2CompressorInputStream(inputStream);
		uncompress(bZip2CompressorInputStream, byteArrayOutputStream);
		return byteArrayOutputStream;
	}

}
