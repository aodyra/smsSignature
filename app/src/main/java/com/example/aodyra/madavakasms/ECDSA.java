package com.example.aodyra.madavakasms;

import java.math.BigInteger;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Visat on 12/2/16.
 */

public class ECDSA {
    private final BigInteger p = new BigInteger("01FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
    private final BigInteger a = new BigInteger("01FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC", 16);
    private final BigInteger b = new BigInteger("0051953EB9618E1C9A1F929A21A0B68540EEA2DA725B99B315F3B8B489918EF109E156193951EC7E937B1652C0BD3BB1BF073573DF883D2C34F1EF451FD46B503F00", 16);
    private final BigInteger n = new BigInteger("01FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFA51868783BF2F966B7FCC0148F709A5D03BB5C9B8899C47AEBB6FB71E91386409", 16);

    private final BigInteger Ga = new BigInteger("00C6858E06B70404E9CD9E3ECB662395B4429C648139053FB521F828AF606B4D3DBAA14B5E77EFE75928FE1DC127A2FFA8DE3348B3C1856A429BF97E7E31C2E5BD66", 16);
    private final BigInteger Gb = new BigInteger("011839296A789A3BC0045C8A5FB42C7D1BD998F54449579B446817AFBD17273E662C97EE72995EF42640C550B9013FAD0761353C7086A272C24088BE94769FD16650", 16);
    private final Point G = new Point(Ga, Gb);

    private final ECC curve;    
    private final Random random;

    public ECDSA() {
        this.curve = new ECC(a, b, p);                
        this.random = new Random();
    }
    
    public Pair<BigInteger, Point> generateKey() {        
        while (true) {
            BigInteger privateKey = new BigInteger(n.bitLength(), random);
            privateKey = privateKey.mod(n);
            if (privateKey.equals(BigInteger.ZERO))
                continue;
            
            Point publicKey = curve.multiply(privateKey, G);
            if (publicKey.infinite)
                continue;
            return new Pair<>(privateKey, publicKey);
        }
    }
    
    public Point generate(BigInteger privateKey) {
        return curve.multiply(privateKey, G);
    }
    
    public Pair<BigInteger, BigInteger> sign(String message, BigInteger privateKey) {
        String digest = new SHA1(message).getHash();
        BigInteger e = new BigInteger(digest, 16);
        while (true) {
            BigInteger k = new BigInteger(n.bitLength(), random);
            k = k.mod(this.n);
            if (k.equals(BigInteger.ZERO))
                continue;
            
            Point kG = curve.multiply(k, G);            
            BigInteger r = kG.x.mod(this.n);
            if (r.equals(BigInteger.ZERO))
                continue;
            
            BigInteger k_inv = k.modPow(this.n.subtract(BigInteger.valueOf(2)), this.n);
            BigInteger s = r.multiply(privateKey).add(e).multiply(k_inv).mod(this.n);
            if (s.equals(BigInteger.ZERO))
                continue;
            
            return new Pair<>(r, s);
        }
    }
    
    public boolean verify(String message, Pair<BigInteger, BigInteger> signature, Point publicKey) {
        if (publicKey.infinite)
            return false;
        if (!curve.checkOnCurve(publicKey))
            return false;
        if (!curve.multiply(this.n, publicKey).infinite)
            return false;        
        BigInteger r = signature.left, s = signature.right;
        if (r.compareTo(BigInteger.ZERO) <= 0 || s.compareTo(BigInteger.ZERO) <= 0
                || r.compareTo(this.n) >= 0 || s.compareTo(this.n) >= 0)
            return false;
        BigInteger w = s.modPow(this.n.subtract(BigInteger.valueOf(2)), this.n);
        String digest = new SHA1(message).getHash();
        BigInteger e = new BigInteger(digest, 16);
        BigInteger u = w.multiply(e).mod(this.n);
        BigInteger v = w.multiply(r).mod(this.n);
        Point kG = curve.add(curve.multiply(u, G), curve.multiply(v, publicKey));
        return kG.x.equals(r);
    }
    
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        ECDSA ecdsa = new ECDSA();
        String message = in.nextLine();
        Pair<BigInteger, Point> key = ecdsa.generateKey();
        Pair<BigInteger, BigInteger> signature = ecdsa.sign(message, key.left);
        System.out.println("Messsage: "+ message);        
        System.out.println("Private Key: " + key.left.toString(16));
        System.out.println("Public Key:" + key.right);
        System.out.println("\nSignature: " + signature);
        System.out.println("Verification: " + (ecdsa.verify(message, signature, key.right) ? "OK" : "WRONG"));
    }
}