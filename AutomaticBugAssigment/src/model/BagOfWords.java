package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import bean.Bug;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

/**
 * This class use the Apache Opennlp from:
 * https://opennlp.apache.org/docs/1.9.0/manual/opennlp.html This class product
 * a bag of words from file source code or a bug. 1. step tokenizing with
 * Opennlp tokenizer 2. step Lemmatizing 3. step stop words filtering
 * 
 **/

public class BagOfWords implements Serializable {

	
	private static final long serialVersionUID = -8589648061274982318L;
	
	File file = null;
	Bug bug = null;
	String words = null;
	String wordsToken[] = null;
	String bagOfWords[] = null;

	public BagOfWords(File file) throws IOException { // constructor when get a source code filepath
		this.file = file;

		StringBuilder s = new StringBuilder();
		int c;
		int cPrev = 0; // for previous character to define whitespace and the java name convention

		FileInputStream fin = null;

		/* Read Input file */
		try {
			fin = new FileInputStream(file);
		} catch (FileNotFoundException fex) {
			fex.printStackTrace();
			System.err.println(0);
		}

		/* Reads individual characters till End of file is encountered */
		try {
			while ((c = fin.read()) != -1) {

				if (('A' <= c && c <= 'Z') && ('a' <= cPrev && cPrev <= 'z')) // if c between 65-90 (ABC) and previous
																				// char
																				// between 97-122 (abc) like varKey =
																				// var
																				// Key
					s.append(' ');

				if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z'))
					s.append((char) c);

				else
					s.append(' ');

				cPrev = c;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(0);
		}

		words = s.toString();

		try {
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(0);
		}

	}

	public BagOfWords(Bug bug) { // constructor when get a bug
		this.bug=bug;
		
		words = bug.getBugShortDesc() + bug.getBugLongDesc();

	}

	private String[] getTokenizedText() { // get an array of tokenized text

		@SuppressWarnings("deprecation")
		SimpleTokenizer tokenizer = new SimpleTokenizer();

		String wordsToken[] = tokenizer.tokenize(words);

		return wordsToken;

	}

	private String[] removeStopWords(String[] sourceString) { // remove stopwords the stop words dictionary download
																// from:
																// https://gist.github.com/carloschavez9/63414d83f68b09b4ef2926cc20ad641c

		String[] resultTextArray;
		ArrayList<String> wordsList = new ArrayList<String>(Arrays.asList(sourceString));
		FileReader fileR = null;
		FileReader fileR2 = null;

		/* Read StopWord files */
		try {
			fileR = new FileReader(".\\OuterFiles\\nlp_en_stop_words.txt");
			fileR2 = new FileReader(".\\OuterFiles\\java_stop_words.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(0);
		}

		String sCurrentLine;
		ArrayList<String> stopwords = new ArrayList<String>();
		try {

			BufferedReader br = new BufferedReader(fileR);

			
			while ((sCurrentLine = br.readLine()) != null) 
				stopwords.add(sCurrentLine);
		
			

			br = new BufferedReader(fileR2);

			while ((sCurrentLine = br.readLine()) != null) 
				stopwords.add(sCurrentLine);
			
			
			Collections.sort(stopwords);
			Collections.sort(wordsList);
			
			stopwords.replaceAll(String::toLowerCase);
			wordsList.replaceAll(String::toLowerCase);
			
			for (int ii = 0; ii < wordsList.size(); ii++) {
				for (int jj = 0; jj < stopwords.size(); jj++) {
					if (stopwords.get(jj).contains(wordsList.get(ii).toLowerCase())) {
						wordsList.remove(ii--);
						break;
					}
				}
			}

		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println(0);
		}

		
		resultTextArray = wordsList.toArray(new String[wordsList.size()]);
		
		return resultTextArray;
	}

	private String[] lemmatizingWords(String[] sourceString) {

		String[] resultTextArray;
		POSTaggerME tagger = null;
		
		try (InputStream modelIn = new FileInputStream(".\\OuterFiles\\en-pos-maxent.bin")) {
			
			POSModel model = new POSModel(modelIn);
			tagger = new POSTaggerME(model);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(0);
		}
		
		String postags[] = tagger.tag(sourceString);
		
		DictionaryLemmatizer lemmatizer = null;
		
		
		try (InputStream modelIn = new FileInputStream(".\\OuterFiles\\en-lemmatizer.dict")) {
			lemmatizer = new DictionaryLemmatizer(modelIn);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(0);
		} 
		
		
		
	
	
		resultTextArray = lemmatizer.lemmatize(sourceString, postags);
		
		//We get the lemma for every token. “O” indicates that the lemma could not be determined as the word is a proper noun. 
		//Must fill the original word these records:
		
		for (int i = 0; i < resultTextArray.length; i++) {
			if (resultTextArray[i] == "O")
				resultTextArray[i] = sourceString[i];
		}
		
		
		
		
		return resultTextArray;
	}
	

	
	public void buildBagOfWords() {
		bagOfWords = this.lemmatizingWords(this.removeStopWords(this.getTokenizedText()));
	}
	
	
	public String[] getBagOfWords() {
		return this.bagOfWords;
	}
	
	
	public boolean isItSourceCode() {
		if (file == null)
			return false;
		else return true;
	}
	
	
	
	public Bug getBug() {
		return bug;		
	}
	
	public File getFile() {
		return file;
	}

}
