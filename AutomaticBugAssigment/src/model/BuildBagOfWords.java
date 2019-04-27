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
	 * Ez az oszt�ly objektumai felel�sek az�rt, hogy az Apache Opennlp-t haszn�lva
	 * a bagOfWords objektumoknak l�trehozza a sz�zs�k modellj�ket. 1. l�p�sk�nt
	 * tokenzi�l az OpenNLP tokenizerrel 2. l�p�sk�nt Lemmatiz�l 3. l�p�sk�nt stop
	 * sz�t sz�r
	 */

	private static String workingDir;
	// stop sz� sz�t�rak:
	private static FileReader fileR;
	private static FileReader fileR2;
	private static List<String> stopwords;

	// lemmatizing met�dushoz:
	// a tagger nem lehet statikus mert akkor a sz�lkezel�sn�l hib�t okoz
	private POSTaggerME tagger;
	private static POSModel model;

	private static DictionaryLemmatizer lemmatizer;

	BagOfWordsV2 bow;
	private String words = null;

	public BuildBagOfWords(BagOfWordsV2 bow, String workingDir) {
		if (BuildBagOfWords.workingDir == null) {
			// ha m�g nincs inicializ�lva
			BuildBagOfWords.workingDir = workingDir;
			initBuildBagOfWords();
		}
		this.bow = bow;
		tagger = new POSTaggerME(model);
	}

	@Override
	public void run() {

		/*
		 * A BagOfWords objektum t�pus�t�l f�gg�en feldolgozza a tartalmukat.
		 */
		if (bow.isItSourceCode()) {
			StringBuilder s = new StringBuilder();
			int c;
			int cPrev = 0; // // Megel�z� karakter ahhoz hogy defini�lni tudjuk az �res �s a java
							// elnevez�si konnveci�k szerinti karaktereket

			FileInputStream fin = null;

			/* Read Input file */
			try {
				fin = new FileInputStream(bow.getFile());
			} catch (FileNotFoundException fex) {
				fex.printStackTrace();
				System.err.println(0);
			}

			/*
			 * Beolvassuk a karaktereket, �gyelve a java szerinti elnevez�si konvennci�kra.
			 * Teh�t ha kis bet�t kapunk de az el�z� nagy bet�, akkor az egy egybe�rt
			 * v�ltoz�n�v, ami k�l�n szavakat jelent.
			 */
			try {
				while ((c = fin.read()) != -1) {

					if (('A' <= c && c <= 'Z') && ('a' <= cPrev && cPrev <= 'z'))
						// ha c karakter 65-90 k�z�tt van akkor nagy (ABC) �s a
						// megel�z� char 97-122 k�z�tt van (abc), akkor az k�l�n sz�
						// �s kell egy whitespace
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

		// el��ll�tjuk a sz�zs�kmodellt:
		bow.setBagOfWords(this.lemmatizingWords(this.removeStopWords(this.getTokenizedText(words))));

	}

	private void initBuildBagOfWords() {

		// inicializ�lunk a removeStopWords met�dusnak:
		//
		try { // El��ll�tjuk a stopsz� sz�t�rat.

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

		// inicializ�lunk a lemmatizingWords met�dusnak:
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
	 * Az OpenNLP SimpleTokenizer oszt�lya, ami az adott nyers sz�veget a tokenize
	 * met�dussal karakter oszt�lyokra bontja �s egy szavakb�l �ll� t�mb�t ad vissza
	 */

	private String[] getTokenizedText(String words) { // 1. l�p�s: visszaad egy tokeniz�lt azaz sz�tbontott
														// sz�vegt�mb�t.
		@SuppressWarnings("deprecation")
		SimpleTokenizer tokenizer = new SimpleTokenizer();
		String wordsToken[] = tokenizer.tokenize(words);
		return wordsToken;
	}

	private String[] removeStopWords(String[] sourceString) { // 2. l�p�s
		// stopsz� sz�r�s a sz�t�rat a k�vetkez� helyr�l t�lt�ttem le:
		// https://gist.github.com/carloschavez9/63414d83f68b09b4ef2926cc20ad641c

		List<String> wordsList = new ArrayList<String>(Arrays.asList(sourceString));
		// Ha a sz�lista tartalmaz olyan karaktereket ami nem abc karakter el kell
		// t�vol�tani

		for (int jj = 0; jj < wordsList.size(); ++jj) {
			if (!wordsList.get(jj).matches("[a-z]+"))
				wordsList.remove(jj--);
		}

		/* Read StopWord filesWithRankList */
		Collections.sort(wordsList);
		wordsList.replaceAll(String::toLowerCase);

		// �sszehasonl�tjuk a sz�list�t a sztopsz� list�val, �s ha van egyez�s kivessz�k
		// a sz�list�b�l.
		for (int ii = 0; ii < wordsList.size(); ii++) {
			for (int jj = 0; jj < stopwords.size(); jj++) {
				if (stopwords.get(jj).contains(wordsList.get(ii).toLowerCase())) {
					wordsList.remove(ii--);
					break;
				}
			}
		}
		return wordsList.toArray(new String[wordsList.size()]); // visszat�r�nk a sz�rt sz�lista t�mmbel
	}

	private String[] lemmatizingWords(String[] sourceString) { // 3. l�p�s lemmatiz�l�s

		String postags[] = tagger.tag(sourceString);

		String[] resultTextArray = lemmatizer.lemmatize(sourceString, postags);

		// We get the lemma for every token. �O� indicates that the lemma could not be
		// determined as the word is a proper noun.
		// Must fill the original word these records:

		for (int i = 0; i < resultTextArray.length; i++) {
			if (resultTextArray[i] == "O")
				resultTextArray[i] = sourceString[i];
		}
		return resultTextArray; // visszat�r�nk a lemmatiz�lt sz�lista t�mmbel
	}

}
