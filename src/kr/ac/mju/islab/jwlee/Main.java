package kr.ac.mju.islab.jwlee;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        HashChain hc = new HashChain();

        List<Integer> ns = Arrays.asList(100, 200, 300, 1000, 3000, 10000);
        List<Integer> cs = Arrays.asList(30, 50, 20, 100, 1000, 3000);
        Iterator<Integer> ni = ns.iterator();
        Iterator<Integer> ci = cs.iterator();

        long startTime = System.currentTimeMillis();
        while (ni.hasNext() && ci.hasNext())
        {
            int n = ni.next();
            int c = ci.next();

            // Generate keypair with n and c
            System.out.println(String.format("Generating keypair for n=%s, c=%s", n, c));
            KeyPair keypair = hc.keyGen(n, c);

            // Generate v_0 to v_n with hashValGen and keypair
            System.out.println("Generating all vs with keypair and hashValGen...");
            List<String> vList = new ArrayList<>();
            for (int i=0; i<=n; i++)
                vList.add(hc.hashValGen(n, c, i, keypair.vsMap));

            System.out.println("Verifying all vs with hashValVeri...");
            for (int i=Math.max(n-10, 1); i<=n; i++)   // test only v_i of i from max(n-10, 1) to n
            {
                    // Correct Verification Check
                    if (!hc.hashValVeri(keypair.v0, i, vList.get(i)))
                        System.out.println(String.format("CV: Problem occurred with n=%s, c=%s, and v_%s", n, c, i));
                    // Incorrect Verification Check
                    if (hc.hashValVeri(keypair.v0, i-1, vList.get(i)))
                        System.out.println(String.format("IV: Problem occurred with n=%s, c=%s, and v_%s", n, c, i));
            }
            System.out.println("Done");
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        long endTime = System.currentTimeMillis();
        System.out.println(String.format("Tasks took %s millis to run", endTime-startTime));
    }
}
