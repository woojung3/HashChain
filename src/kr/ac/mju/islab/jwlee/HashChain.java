package kr.ac.mju.islab.jwlee;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class HashChain {

    /**
     * Key Generation Function keyGen
     * With input n (hash chain length) and c (# of stored hash values)
     * Outputs v_0 (public value) and v_s (secret values)
     *
     * @param n Hash chain length
     * @param c Number(#) of stored hash values
     * @return keypair that contains v_0 and v_s
     */
    KeyPair keyGen(int n, int c)
    {
        KeyPair rtn = new KeyPair();
        RandomString rs = new RandomString(64);
        String vn = hash(rs.nextString());
        rtn.v0 = hashN(vn, n);

        List<Integer> range = IntStream.rangeClosed(0, c-1).boxed().collect(Collectors.toList());

        Map<Integer, String> map = new HashMap<>();
        map.put(n, vn);
        int prevIdx = n;
        String prevHash = vn;
        for (Integer j : range)
        {
            int s = n - (int)Math.ceil(j*(double)n/c);
            String vs = hashN(prevHash, prevIdx - s);
            map.put(s, vs);
            prevIdx = s;
            prevHash = vs;
        }
        map.put(0, rtn.v0);
        rtn.vsMap = map;

        return rtn;
    }

    /**
     * Hash Value Generation Function hashValGen
     * With input n, c, i, and v_s
     * Outputs v_i
     *
     * @param n Hash chain length
     * @param c Number(#) of stored hash values
     * @param i Index of v_i
     * @param vs Part of Keypair
     * @return v_i
     */
    String hashValGen(int n, int c, int i, Map<Integer, String> vs)
    {
        int j = (int)Math.floor((double)(c*(n-i))/n);
        int s = n - (int)Math.ceil(j*(double)n/c);

        return hashN(vs.get(s), s - i);
    }

    /**
     * Hash Value Verification Function
     *
     * @param v0 Public key
     * @param i Index of v_i
     * @param vi i_th String of the hash chain
     * @return True or False
     */
    boolean hashValVeri(String v0, int i, String vi)
    {
        return v0.equals(hashN(vi, i));
    }

    /**
     * Hash function (dedicated to SHA256)
     * With input str (message)
     * Outputs digest (digest of message)
     *
     * @param str message
     * @return digest
     */
    private String hash(String str){
        String digest;
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes());
            byte byteData[] = sh.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aByteData : byteData) {
                sb.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
            }
            digest = sb.toString();
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            digest = null;
        }
        return digest;
    }

    /**
     * Hash n times
     *
     * @param str message
     * @param n number
     * @return h^n(m)
     */
    private String hashN(String str, int n)
    {
        if (n < 0)
            throw new IllegalArgumentException("n < 1: " + n);

        int i = 0;
        String rtn = str;
        while(i < n)
        {
            rtn = hash(rtn);
            i++;
        }

        return rtn;
    }
}

/**
 * Class that contains secret/public keys
 */
class KeyPair {
    String v0;
    Map<Integer, String> vsMap;
}

/**
 * Class that generates random string. This is used for generating v_n
 */
final class RandomString
{
    /* Assign a string that contains the set of characters you allow. */
    private static final String symbols = "ABCDEFGJKLMNPRSTUVWXYZ0123456789";
    private final Random random = new SecureRandom();
    private final char[] buf;

    RandomString(int length)
    {
        if (length < 1)
            throw new IllegalArgumentException("length < 1: " + length);
        buf = new char[length];
    }

    String nextString()
    {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols.charAt(random.nextInt(symbols.length()));
        return new String(buf);
    }
}
