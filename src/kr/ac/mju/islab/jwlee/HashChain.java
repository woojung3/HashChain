package kr.ac.mju.islab.jwlee;

public class HashChain {

    // Singleton BEGIN
    private HashChain() {}
    private static class Singleton {
        private static final HashChain instance = new HashChain();
    }

    public static HashChain getInstance() {
        return Singleton.instance;
    }

    /* High Thread Cost Singleton
    private static HashChain instance;
    private HashChain () {}

    public static synchronized HashChain getInstance() {
        if (instance == null)
            instance = new HashChain();
        return instance;
    }
    */
    // END
}
