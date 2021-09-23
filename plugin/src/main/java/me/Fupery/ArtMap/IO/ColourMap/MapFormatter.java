package me.Fupery.ArtMap.IO.ColourMap;

import java.io.IOException;

public interface MapFormatter {

    byte[] generateBLOB(byte[] mapData, int resolution) throws IOException;

    byte[] readBLOB(byte[] blobData, int resolution) throws IOException;
}
