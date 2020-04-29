package org.ovgu.de.fiction;

import org.apache.log4j.Logger;
import org.ovgu.de.fiction.feature.extraction.ChunkDetailsGenerator;
import org.ovgu.de.fiction.model.BookDetails;
import org.ovgu.de.fiction.model.TopKResults;
import org.ovgu.de.fiction.preprocess.ContentExtractor;
import org.ovgu.de.fiction.search.FictionRetrievalSearch;
import org.ovgu.de.fiction.search.InterpretSearchResults;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRGeneralUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author Suhita, Sayantan
 */
public class FictionRetrievalDriver {

    final static Logger LOG = Logger.getLogger(FictionRetrievalDriver.class);

    public static void main(String[] args) throws Exception {

        if (args.length != 0) {
            long start = System.currentTimeMillis();

            // setup FRGeneralUtils to load files from dynamic location
            FRGeneralUtils frGeneralUtils = new FRGeneralUtils(args[0]);

            /* 1> Extract content from Gutenberg corpus - one time */
            ContentExtractor.generateContentFromAllEpubs();
            System.out.println("Time taken for generating content (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));

            start = System.currentTimeMillis();
            /* 2> Generate features from the extracted content - one time */
            List<BookDetails> features = generateOtherFeatureForAll();
            System.out.println("Time taken for feature extraction and chunk generation (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));
            start = System.currentTimeMillis();

            /* 3> Write features to CSV - one time */
            //FeatureExtractorUtility.writeFeaturesToCsv(features);
            start = System.currentTimeMillis();
            System.out.println("Time taken for writing to CSV (min)-" + (System.currentTimeMillis() - start) / (1000 * 60));

            /* 4> Query */
            String qryBookNum = "pg1400DickensGreatExp"; //pg11CarolAlice,  pg1400DickensGreatExp,pg766DickensDavidCopfld
            // pg2701HermanMobyDick,pg537DoyleTerrorTales
            // pg13720HermanVoyage1, pg2911Galsw2, pg1155Agatha2,pg2852DoyleHound, pg2097DoyleSignFour

            // read from csv features and prints ranked relevant books, run after CSV is written
            String FEATURE_CSV_FILE = FRGeneralUtils.getPropertyVal(FRConstants.FEATURE_FILE);
            //Config 1: three possible setting similarity objective penalization: divide chunks by (1) OR (number_of_chunks) OR sqr_root(number_of_chunks)
            //Config 2: two possible settings for similarity roll up : add_chunks (default) OR multipl_chunks
            //Config 3: Include or exclude TTR and Numbr of Chars


            TopKResults topKResults = FictionRetrievalSearch.findRelevantBooks(qryBookNum, FEATURE_CSV_FILE,
                    FRConstants.SIMI_PENALISE_BY_CHUNK_NUMS, FRConstants.SIMI_ROLLUP_BY_ADDTN,
                    FRConstants.SIMI_EXCLUDE_TTR_NUMCHARS, FRConstants.TOP_K_RESULTS, FRConstants.SIMILARITY_L2);

            /* * 5> Perform some machine learning over the results

             */
            InterpretSearchResults interp = new InterpretSearchResults();
            interp.performStatiscalAnalysis(topKResults);

            //findLuceneRelevantBooks(qryBookNum);
        } else {
            LOG.error("Please provide you base folder location as run args");
        }
    }

    public static List<BookDetails> generateOtherFeatureForAll() throws IOException {
        ChunkDetailsGenerator chunkImpl = new ChunkDetailsGenerator();
        return chunkImpl.getChunksFromAllFiles();
    }


}
