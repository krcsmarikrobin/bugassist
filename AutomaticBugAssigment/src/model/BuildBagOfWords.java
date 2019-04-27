package model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

public class BuildBagOfWords implements Runnable {
	/*
	 * Ez az osztály objektumai felelõsek azért, hogy az Apache Opennlp-t használva
	 * a bagOfWords objektumoknak létrehozza a szózsák modelljüket. 1. lépésként
	 * tokenziál az OpenNLP tokenizerrel 2. lépésként Lemmatizál 3. lépésként stop
	 * szót szûr
	 */

	private static String workingDir;
	// stop szó szótárak:
	private static FileReader fileR;
	private static FileReader fileR2;
	private static List<String> stopwords;

	// lemmatizing metódushoz:
	// a tagger nem lehet statikus mert akkor a szálkezelésnél hibát okoz
	private POSTaggerME tagger;
	private static POSModel model;

	private static DictionaryLemmatizer lemmatizer;

	BagOfWordsV2 bow;
	private String words = null;

	public BuildBagOfWords(BagOfWordsV2 bow, String workingDir) {
		if (BuildBagOfWords.workingDir == null) {
			// ha még nincs inicializálva
			BuildBagOfWords.workingDir = workingDir;
			initBuildBagOfWords();
		}
		this.bow = bow;
		tagger = new POSTaggerME(model);
	}

	@Override
	public void run() {

		/*
		 * A BagOfWords objektum típusától függõen feldolgozza a tartalmukat.
		 */
		if (bow.isItSourceCode()) {
			StringBuilder s = new StringBuilder();
			int c;
			int cPrev = 0; // // Megelõzõ karakter ahhoz hogy definiálni tudjuk az üres és a java
							// elnevezési konnveciók szerinti karaktereket

			FileInputStream fin = null;

			/* Read Input file */
			try {
				fin = new FileInputStream(bow.getFile());
			} catch (FileNotFoundException fex) {
				fex.printStackTrace();
				System.err.println(0);
			}

			/*
			 * Beolvassuk a karaktereket, ügyelve a java szerinti elnevezési konvennciókra.
			 * Tehát ha kis betût kapunk de az elõzõ nagy betû, akkor az egy egybeírt
			 * változónév, ami külön szavakat jelent.
			 */
			try {
				while ((c = fin.read()) != -1) {

					if (('A' <= c && c <= 'Z') && ('a' <= cPrev && cPrev <= 'z'))
						// ha c karakter 65-90 között van akkor nagy (ABC) és a
						// megelõzõ char 97-122 között van (abc), akkor az külön szó
						// és kell egy whitespace
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
		} else {
			words = bow.getBug().getBugShortDesc() + bow.getBug().getBugLongDesc();
		}

		// elõállítjuk a szózsákmodellt:
		bow.setBagOfWords(this.lemmatizingWords(this.removeStopWords(this.getTokenizedText(words))));

	}

	private void initBuildBagOfWords() {

		// inicializálunk a removeStopWords metódusnak:
		//
		try { // Elõállítjuk a stopszó szótárat.

			fileR = new FileReader(workingDir + "\\OuterFiles\\nlp_en_stop_words.txt");
			fileR2 = new FileReader(workingDir + "\\OuterFiles\\java_stop_words.txt");
			String sCurrentLine;
			stopwords = new ArrayList<String>();

			BufferedReader br = new BufferedReader(fileR);
			while ((sCurrentLine = br.readLine()) != null)
				stopwords.add(sCurrentLine);

			br = new BufferedReader(fileR2);
			while ((sCurrentLine = br.readLine()) != null)
				stopwords.add(sCurrentLine);

			stopwords.replaceAll(String::toLowerCase);
			Collections.sort(stopwords);

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(0);
		}

		// inicializálunk a lemmatizingWords metódusnak:
		//

		try (InputStream modelIn = new FileInputStream(workingDir + "\\OuterFiles\\en-pos-maxent.bin")) {
			model = new POSModel(modelIn);

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(0);
		}

		try (InputStream modelIn = new FileInputStream(workingDir + "\\OuterFiles\\en-lemmatizer.dict")) {
			BuildBagOfWords.lemmatizer = new DictionaryLemmatizer(modelIn);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(0);
		}

	}

	/*
	 * Az OpenNLP SimpleTokenizer osztálya, ami az adott nyers szöveget a tokenize
	 * metódussal karakter osztályokra bontja és egy szavakból álló tömböt ad vissza
	 */

	private String[] getTokenizedText(String words) { // 1. lépés: visszaad egy tokenizált azaz szétbontott
														// szövegtömböt.
		@SuppressWarnings("deprecation")
		SimpleTokenizer tokenizer = new SimpleTokenizer();
		String wordsToken[] = tokenizer.tokenize(words);
		return wordsToken;
	}

	private String[] removeStopWords(String[] sourceString) { // 2. lépés
		// stopszó szûrés a szótárat a következõ helyrõl töltöttem le:
		// https://gist.github.com/carloschavez9/63414d83f68b09b4ef2926cc20ad641c

		List<String> wordsList = new ArrayList<String>(Arrays.asList(sourceString));
		// Ha a szólista tartalmaz olyan karaktereket ami nem abc karakter el kell
		// távolítani

		for (int jj = 0; jj < wordsList.size(); ++jj) {
			if (!wordsList.get(jj).matches("[a-z]+"))
				wordsList.remove(jj--);
		}

		/* Read StopWord filesWithRankList */
		Collections.sort(wordsList);
		wordsList.replaceAll(String::toLowerCase);

		// Összehasonlítjuk a szólistát a sztopszó listával, és ha van egyezés kivesszük
		// a szólistából.
		for (int ii = 0; ii < wordsList.size(); ii++) {
			for (int jj = 0; jj < stopwords.size(); jj++) {
				if (stopwords.get(jj).contains(wordsList.get(ii).toLowerCase())) {
					wordsList.remove(ii--);
					break;
				}
			}
		}
		return wordsList.toArray(new String[wordsList.size()]); // visszatérünk a szûrt szólista tömmbel
	}

	private String[] lemmatizingWords(String[] sourceString) { // 3. lépés lemmatizálás

		String postags[] = tagger.tag(sourceString);

		String[] resultTextArray = lemmatizer.lemmatize(sourceString, postags);

		// We get the lemma for every token. “O” indicates that the lemma could not be
		// determined as the word is a proper noun.
		// Must fill the original word these records:

		for (int i = 0; i < resultTextArray.length; i++) {
			if (resultTextArray[i] == "O")
				resultTextArray[i] = sourceString[i];
		}
		return resultTextArray; // visszatérünk a lemmatizált szólista tömmbel
	}

}
