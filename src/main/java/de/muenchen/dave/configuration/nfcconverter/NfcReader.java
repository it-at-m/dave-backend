/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.dave.configuration.nfcconverter;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

/**
 * Wrapper für Reader der eine NFC-Konvertierung durchführt.
 * Achtung:
 * Bei Java-Readern und -Writern kann gefahrlos eine NFC-Konvertierung
 * durchgeführt werden, da dort Zeichen verarbeitet werden.
 * Dieser Reader liest bei vor dem Lesen des ersten Zeichens denn vollständig Text des
 * gewrappten Readers in einern internen Buffer und führt darauf die NFC-Normalisierung
 * durch. Grund ist, dass NFC-Konvertierung kann nicht auf Basis von einzelnen Zeichen
 * durchgeführt werden kann. Dies kann zu erhöhter Latenz führen.
 */
@Slf4j
public class NfcReader extends Reader {

    private final Reader original;

    private CharArrayReader converted;

    public NfcReader(final Reader original) {
        this.original = original;
        this.converted = null;
    }

    private void convert() {

        if (this.converted != null) {
            return;
        }

        log.debug("Converting Reader data to NFC.");
        try {
            final String nfdContent = IOUtils.toString(this.original);
            final String nfcConvertedContent = NfcHelper.nfcConverter(nfdContent);
            this.converted = new CharArrayReader(nfcConvertedContent.toCharArray());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read() throws IOException {
        this.convert();
        return this.converted.read();
    }

    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        this.convert();
        return this.converted.read(cbuf, off, len);
    }

    @Override
    public void close() {
        // Nothing to do
    }

    @Override
    public long skip(final long n) throws IOException {
        this.convert();
        return this.converted.skip(n);
    }

    @Override
    public boolean ready() throws IOException {
        this.convert();
        return this.converted.ready();
    }

    @Override
    public boolean markSupported() {
        this.convert();
        return this.converted.markSupported();
    }

    @Override
    public void mark(final int readAheadLimit) throws IOException {
        this.convert();
        this.converted.mark(readAheadLimit);
    }

    @Override
    public void reset() throws IOException {
        this.convert();
        this.converted.reset();
    }

}
