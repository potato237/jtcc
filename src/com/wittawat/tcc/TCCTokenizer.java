/*
 *   Copyright 2009, 2010 Wittawat Jitkrittum
 *   http://wittawat.com
 *
 *   This file is part of JTCC.
 *
 *   JTCC is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   JTCC is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with JTCC.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.wittawat.tcc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.TokenStream;

import com.wittawat.tcc.TCCParser.stream_return;
import java.io.Serializable;
import java.util.Vector;

/**
 * A TCC (Thai Character Cluter) tokenizer. This class makes use of
 * <code>TCCLexer</code> and <code>TCCParser</code> which both are automatically
 * generated by ANTLR from supplying a TCC grammar. For more information, see
 * the grammar file.
 * 
 * Command line usage is available by calling main(..) of this class.
 * 
 * A new object of this class needs to be created for each new content to
 * tokenize.
 * 
 * @author Wittawat Jitkrittum 
 * 
 * */
public class TCCTokenizer implements Serializable{

    private TCCLexer tccLexer;
    private TCCParser tccParser;
    /**True if the TCCParser has already performed the tokenization*/
    private boolean tokenized = false;

    /**
     * Create a new <code>TCCTokenizer</code> to tokenize the specified content.
     */
    public TCCTokenizer(String content) {
        initialize(new ANTLRStringStream(content));
    }

    /**
     * Create a new <code>TCCTokenizer</code> to tokenize the content inside the
     * specified text file, with the specified encoding.
     */
    public TCCTokenizer(File file, String encoding) throws IOException {
        initialize(new ANTLRFileStream(file.getAbsolutePath(), encoding));

    }

    /**
     * Create a new <code>TCCTokenizer</code> to tokenize the content inside the
     * specified text file.
     */
    public TCCTokenizer(File file) throws IOException {
        initialize(new ANTLRFileStream(file.getAbsolutePath()));
    }

    public TCCTokenizer(InputStream ins) throws IOException {
        initialize(new ANTLRInputStream(ins));
    }

    public TCCTokenizer(InputStream ins, String encoding) throws IOException {
        initialize(new ANTLRInputStream(ins, encoding));
    }

    public TCCTokenizer(Reader reader) throws IOException {
        initialize(new ANTLRReaderStream(reader));
    }

    private void initialize(CharStream input) {
        tccLexer = new TCCLexer(input);
        TokenStream tokens = new TokenRewriteStream(tccLexer);
        tccParser = new TCCParser(tokens);
    }

    private void activateParser() throws RecognitionException {
        if (!tokenized) {
            stream_return re = tccParser.stream(); // Call the start symbol (of the grammar) method
            tokenized = true;
        }

    }

    /**Set the delimiter used to split consecutive TCCs.
     * By default, | is used. The delimiter is placed at the end of
    each TCC.*/
    public void setDelimiter(String delimiter) {
        if(tokenized){
            throw new IllegalStateException("Cannot set the delimiter after tokenized.");
        }
        tccParser.setDelimiter(delimiter);
        
    }

    /**@return a list of end indexes of TCCs. For example, if the
    input is tokenized as "ab|c|def|g|, then end indexes will be
     * [2,3,6,7]. Can be used directly with String.substring() method."*/
    public Vector<Integer> getEndIndexes() throws RecognitionException {
        activateParser();
        Vector<Integer> endIndexes = tccParser.getEndIndexes();
        return endIndexes;
    }

    public Vector<Integer> getEndIndexesOrNull() {
        try {
            Vector<Integer> endIndexes = getEndIndexes();
            return endIndexes;
        } catch (RecognitionException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * @return a String output after TCCs are tokenized. The output
     * is of the form tcc1|tcc2|....|tcc_last| if | is used
     * as the delimiter. Consecutive English characters, spaces, and digits
     * are treated as one TCC. Other unknown characters are treated
     * as if each character is one TCC.
     * */
    public String tokenize() throws RecognitionException {
        activateParser();
        
        return tccParser.getDelimitedOutput();
    }

    /** A more convenient
     * method in the case that recognition error's detail is not needed.
     * @return Like tokenize() but return null on error.*/
    public String tokenizeOrNull() {
        try {
            String out = tokenize();
            return out;
        } catch (RecognitionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
//        args = new String[]{"file", "/home/nook/Desktop/thai_text.txt"};
        if (args.length < 1) {
            System.out.println("TCC implemented in Java by Wittawat Jitkrittum (wittawatj@gmail.com)");
            System.out.println("Based on grammar proposed in the paper \"Character Cluster Based Thai Information Retrieval\" \n");
            System.out.println("USAGE: program {stdin | file <filePath> | content <content>}");
            System.out.println(" - Use stdin to tokenize the input from stdin. Often used with | (redirect pipe).");
            System.out.println(" - Use file to specify in the argument the path of file containing the content to tokenize");
            System.out.println(" - Use content to specify in the argument directly the content to tokenize");
            System.out.println("Sample usages:");
            System.out.println(" - java -jar JTCC.jar file C:/thaitext.txt");
            System.out.println(" - cat thaitext.txt | java -jar JTCC.jar stdin");
            System.out.println(" - java -jar JTCC.jar content \"ตรงนี้เป็นเนื้อหาที่ต้องการตัด TCC. Content to tokenize into TCCs here.\" ");
            System.exit(1);
        }

        String mode = args[0];
        TCCTokenizer tcc = null;
        if (mode.equals("stdin")) {
            tcc = new TCCTokenizer(System.in);

        } else if (mode.equals("file")) {
            if (args.length < 2) {
                System.out.println("Mode 'file' must have an argument specifying the file path.");
                System.exit(1);
            }
            String path = args[1];
            tcc = new TCCTokenizer(new File(path));

        } else if (mode.equals("content")) {
            if (args.length < 2) {
                System.out.println("Mode 'content' must have a content argument. Should be in quotes.");
                System.exit(1);
            }
            String content = args[1];
            tcc = new TCCTokenizer(content);
        } else {
            System.out.println("Unknown mode.");
            System.exit(1);
        }
        assert tcc != null;

        // Print
        String tokenized = tcc.tokenize();
        System.out.println(tokenized);
    }
}