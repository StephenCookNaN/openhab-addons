/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.tapocontrol.internal.api.protocol.klap;

import static org.openhab.binding.tapocontrol.internal.constants.TapoErrorCode.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoEncoder.*;
import static org.openhab.binding.tapocontrol.internal.helpers.utils.ByteUtils.*;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;

/**
 * Cipher for KLAP-Protocol
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class KlapCipher {
    protected static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    protected static final String CIPHER_ALGORITHM = "AES";
    protected static final String CIPHER_CHARSET = "UTF-8";

    private int ivSeq = 0;
    private byte[] iv;
    private byte[] key;
    private byte[] sig;

    /************************
     * INIT CLASS
     ************************/

    /**
     * Init KlapCipher with seeds generated by session and got from device
     * 
     * @param localSeed local random 16 byte seedhash sent to device
     * @param remoteSeed 16 byte seed received as awnser from device
     * @param userHash sha256 hash of user-credentials
     */
    public KlapCipher(byte[] localSeed, byte[] remoteSeed, byte[] userHash)
            throws TapoErrorHandler, NoSuchAlgorithmException {
        iv = initIvDerive(localSeed, remoteSeed, userHash);
        key = initKeyDerive(localSeed, remoteSeed, userHash);
        sig = initSigDerive(localSeed, remoteSeed, userHash);
    }

    /**
     * reset all data (logout)
     */
    public void reset() {
        ivSeq = 0;
        iv = new byte[0];
        key = new byte[0];
        sig = new byte[0];
    }

    /************************
     * GET VALUES
     ************************/

    /**
     * return true if cipher is fully initialized (handshakes and cookies completed)
     */
    public boolean isInitialized() {
        return iv.length > 0 && key.length > 0 && sig.length > 0;
    }

    /**
     * get iv-sequence number which increments every request
     */
    public int getIvSeq() {
        return ivSeq;
    }

    /************************
     * ENCODING / DECODING
     ************************/

    /**
     * ENCRYPT STRING TO BYTEARRAY
     */
    public byte[] encrypt(String str) throws TapoErrorHandler {
        try {
            ivSeq += 1; /* increment sequence */

            Cipher encodeCipher = getCipher(Cipher.ENCRYPT_MODE, ivSeq);
            byte[] msg = str.getBytes(CIPHER_CHARSET);
            byte[] cipherText = encodeCipher.doFinal(msg);
            byte[] signature = sha256Encode(concatBytes(sig, BigInteger.valueOf(ivSeq).toByteArray(), cipherText));
            return concatBytes(signature, cipherText);
        } catch (Exception e) {
            throw new TapoErrorHandler(ERR_DATA_ENCRYPTING, e.getMessage());
        }
    }

    /**
     * DECRYPT BYTEARRAY INTO STRING
     */
    public String decrypt(byte[] byteArr, int ivSeq) throws TapoErrorHandler {
        try {
            Cipher decodeCipher = getCipher(Cipher.DECRYPT_MODE, ivSeq);
            byte[] bytesToDecode = truncateByteArray(byteArr, 32, byteArr.length - 32);
            byte[] doFinal = decodeCipher.doFinal(bytesToDecode);
            return new String(doFinal);
        } catch (Exception e) {
            throw new TapoErrorHandler(ERR_DATA_DECRYPTING, e.getMessage());
        }
    }

    /**
     * get iv-buffer
     * 
     * @param ivSeq ivSequence-Number
     */
    private byte[] getIv(int ivSeq) throws TapoErrorHandler {
        byte[] seq = BigInteger.valueOf(ivSeq).toByteArray();
        return concatBytes(iv, seq);
    }

    /************************
     * INIT CIPHER-SPECS
     ************************/

    /**
     * INIT IV-DERIVE FROM SEEDS
     */
    private byte[] initIvDerive(byte[] localSeed, byte[] remoteSeed, byte[] userHash)
            throws TapoErrorHandler, NoSuchAlgorithmException {
        /* iv is first 16 bytes of sha256, where the last 4 bytes forms the */
        /* sequence number used in requests and is incremented on each request */
        byte[] fullBytes = concatBytes("iv".getBytes(), localSeed, remoteSeed, userHash);
        byte[] byteHash = sha256Encode(fullBytes);
        byte[] seqBytes = truncateByteArray(byteHash, byteHash.length - 4, 4);

        ivSeq = new BigInteger(seqBytes).intValue(); /* get sequence number */
        return truncateByteArray(byteHash, 0, 12);
    }

    /**
     * INIT KEY-DERIVE FROM SEEDS
     */
    private byte[] initKeyDerive(byte[] localSeed, byte[] remoteSeed, byte[] userHash)
            throws TapoErrorHandler, NoSuchAlgorithmException {
        byte[] fullBytes = concatBytes("lsk".getBytes(), localSeed, remoteSeed, userHash);
        byte[] byteHash = sha256Encode(fullBytes);
        byte[] keyBytes = truncateByteArray(byteHash, 0, 16);
        return keyBytes;
    }

    /**
     * INIT SIGNATURE-DERIVE FROM SEEDS
     */
    private byte[] initSigDerive(byte[] localSeed, byte[] remoteSeed, byte[] userHash)
            throws TapoErrorHandler, NoSuchAlgorithmException {
        /* used to create a hash with which to prefix each request */
        byte[] fullBytes = concatBytes("ldk".getBytes(), localSeed, remoteSeed, userHash);
        byte[] byteHash = sha256Encode(fullBytes);
        byte[] sigBytes = truncateByteArray(byteHash, 0, 28);
        return sigBytes;
    }

    /************************
     * HELPERS
     ************************/

    /**
     * Return initialized Cipher
     * 
     * @param opMode op-mode (encrypt/decrypt)
     * @param ivSeq iv-sequence number
     */
    private Cipher getCipher(int opMode, int ivSeq) throws TapoErrorHandler {
        try {
            byte[] ivBuffer = getIv(ivSeq);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, CIPHER_ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBuffer);

            Cipher myCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            myCipher.init(opMode, secretKeySpec, ivParameterSpec);

            return myCipher;
        } catch (Exception e) {
            throw new TapoErrorHandler(e);
        }
    }
}
