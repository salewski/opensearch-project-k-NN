/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.knn.index.codec;

import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class KNNCodecUtil {

    public static final String HNSW_EXTENSION = ".hnsw";
    public static final String HNSW_COMPOUND_EXTENSION = ".hnswc";

    public static final class Pair {
        public Pair(int[] docs, float[][] vectors) {
            this.docs = docs;
            this.vectors = vectors;
        }

        public int[] docs;
        public float[][] vectors;
    }

    public static KNNCodecUtil.Pair getFloats(BinaryDocValues values) throws IOException {
        ArrayList<float[]> vectorList = new ArrayList<>();
        ArrayList<Integer> docIdList = new ArrayList<>();
        for (int doc = values.nextDoc(); doc != DocIdSetIterator.NO_MORE_DOCS; doc = values.nextDoc()) {
            BytesRef bytesref = values.binaryValue();
            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytesref.bytes, bytesref.offset, bytesref.length);
                ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
                float[] vector = (float[]) objectStream.readObject();
                vectorList.add(vector);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            docIdList.add(doc);
        }
        return new KNNCodecUtil.Pair(docIdList.stream().mapToInt(Integer::intValue).toArray(), vectorList.toArray(new float[][]{}));
    }

    public static String buildEngineFileName(String segmentName, String latestBuildVersion, String fieldName,
                                             String extension) {
        return String.format("%s_%s_%s%s", segmentName, latestBuildVersion, fieldName, extension);
    }
}
