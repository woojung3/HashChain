package kr.ac.mju.islab.jwlee;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        List<Integer> ns = Arrays.asList(100, 200, 300);
        List<Integer> cs = Arrays.asList(30, 50, 20);
        Iterator<Integer> ni = ns.iterator();
        Iterator<Integer> ci = cs.iterator();

        while (ni.hasNext() && ci.hasNext())
        {
            int n = ni.next();
            int c = ci.next();

            // Generate keypair with n and c
            KeyPair keypair = keyGen(n, c);

            // Generate v_0 to v_n with hashValGen and keypair
            List<String> vList = new ArrayList<>();
            for (int i=0; i<=n; i++)
                vList.add(hashValGen(n, c, i, keypair.vsMap));

            // Correct Verification Check
            for (int i=1; i<=n; i++)
            {
                if (!hashValVeri(keypair.v0, i, vList.get(i)))
                    System.out.println(String.format("CV: Problem occurred with n=%s, c=%s, and v_%s", n, c, i));
            }

            // Incorrect Verification Check
            for (int i=1; i<=n; i++)
            {
                if (hashValVeri(keypair.v0, i-1, vList.get(i)))
                    System.out.println(String.format("IV: Problem occurred with n=%s, c=%s, and v_%s", n, c, i));
            }
        }
    }

    /**
     * Key Generation Function keyGen
     * With input n (hash chain length) and c (# of stored hash values)
     * Outputs v_0 (public value) and v_s (secret values)
     *
     * @param n Hash chain length
     * @param c Number(#) of stored hash values
     * @return keypair that contains v_0 and v_s
     */
    private static KeyPair keyGen(int n, int c)
    {
        KeyPair rtn = new KeyPair();
        RandomString rs = new RandomString(64);
        String vn = hash(rs.nextString());
        rtn.v0 = hashN(vn, n);

        List<Integer> range = IntStream.rangeClosed(0, c-1).boxed().collect(Collectors.toList());

        // Codes below are not cost-optimized. These are written like this because it is simple and helps understanding. BEGIN
        List<Integer> indexes = range.stream().map(j -> n - (int)Math.ceil(j*(double)n/c)).collect(Collectors.toList());
        indexes.add(0);
        List<String> vsList = range.stream().map(j -> hashN(vn, (int)Math.ceil(j*(double)n/c))).collect(Collectors.toList());
        vsList.add(rtn.v0);

        Iterator<Integer> i1 = indexes.iterator();
        Iterator<String> i2 = vsList.iterator();

        Map<Integer, String> map = new LinkedHashMap<>();  // ordered
        while (i1.hasNext() && i2.hasNext())
            map.put(i1.next(), i2.next());

        rtn.vsMap = map;
        // END

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
    private static String hashValGen(int n, int c,int i, Map<Integer, String> vs)
    {
        int j = (int)Math.floor((double)(c*(n-i))/n);
        int s = n - (int)Math.ceil(j*(double)n/c);

        // Can find index s via codes below, too
        // This takes approximately O(n/c). BEGIN
        /*
        int s = i;
        while (!vs.containsKey(s))
            s++;
        */
        // END

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
    private static boolean hashValVeri(String v0, int i, String vi)
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
    private static String hash(String str){
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
    private static String hashN(String str, int n)
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