

import java.math.BigInteger;
import java.security.*;

public class Main {

    public static void print(String s, boolean newline){
        if (newline)
            System.out.println(s);
        else
            System.out.print(s);
    }

    public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {


        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);

        // Generating two key pairs, one for Scrooge and one for Alice
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey private_key_scrooge = pair.getPrivate();
        PublicKey public_key_scrooge = pair.getPublic();

        pair = keyGen.generateKeyPair();
        PrivateKey private_key_alice = pair.getPrivate();
        PublicKey public_key_alice = pair.getPublic();


        Transaction tx = new Transaction();
        tx.addOutput(10, public_key_scrooge);

        // that value has no meaning, but tx.getRawDataToSign(0) will access in.prevTxHash;
        byte[] initialHash = BigInteger.valueOf(1695609641).toByteArray();
        tx.addInput(initialHash, 0);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(private_key_scrooge);
        signature.update(tx.getRawDataToSign(0));
        byte[] sig = signature.sign();

        tx.addSignature(sig, 0);
        tx.finalize();



        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(tx.getHash(),0);
        utxoPool.addUTXO(utxo, tx.getOutput(0));


        Transaction tx2 = new Transaction();


        tx2.addInput(tx.getHash(), 0);


        tx2.addOutput(5, public_key_alice);
        tx2.addOutput(3, public_key_alice);
        tx2.addOutput(2, public_key_alice);


        signature.initSign(private_key_scrooge);
        signature.update(tx2.getRawDataToSign(0));
        sig = signature.sign();
        tx2.addSignature(sig, 0);
        tx2.finalize();


        TxHandler txHandler = new TxHandler(utxoPool);
        boolean valid = txHandler.isValidTx(tx2);
        int length = txHandler.handleTxs(new Transaction[]{tx2}).length;

        System.out.println("Validity: (should be true) "+valid);
        System.out.println("Length: (should be 1) "+length);




        Transaction tx3 = new Transaction();


        tx3.addInput(tx.getHash(), 0);


        tx3.addOutput(5, public_key_alice);
        tx3.addOutput(3, public_key_alice);
        tx3.addOutput(5, public_key_alice);


        signature.initSign(private_key_scrooge);
        signature.update(tx3.getRawDataToSign(0));
        sig = signature.sign();
        tx3.addSignature(sig, 0);
        tx3.finalize();


        TxHandler txHandler2 = new TxHandler(utxoPool);
        boolean valid2 = txHandler.isValidTx(tx3);
        int length2 = txHandler.handleTxs(new Transaction[]{tx3}).length;

        System.out.println("Validity: (should be false) "+valid2);
        System.out.println("Length: (should be 0) "+length2);



        /*MaxFeeTxHandler mtxHandler = new MaxFeeTxHandler(utxoPool);
        print("mtxhandler:", true);
        boolean valid3 = mtxHandler.isValidTx(tx3);
        valid2 = txHandler.isValidTx(tx3);
        System.out.println(mtxHandler);
        System.out.println(valid3);
        System.out.println(valid2);*/


    }
}