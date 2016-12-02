package com.example.aodyra.madavakasms;

/**
 * Created by aodyra on 12/2/16.
 */

public class SHA1 {
    private int[] H = {0x67452301, 0xEFCDAB89, 0x98BADCFE, 0x10325476, 0xC3D2E1F0};
    private int A, B, C, D, E;
    private String message;
    private byte[] dataMessage;
    private int totalBytesMessage; // total character
    private long totalBitMessage;
    private long byteHash;
    private String hash = "";

    public SHA1(){}

    public SHA1(String message){
        this.message = message;
        dataMessage = message.getBytes();
        totalBytesMessage = dataMessage.length;
        totalBitMessage = totalBytesMessage*8;
        generateHash(dataMessage);
    }

    public byte[] paddingBits(byte[] data){
        int moduloLength = (int)totalBitMessage % 512;
        int totalBitPadding = 448 - moduloLength;
        if(totalBitPadding <= 0) totalBitPadding = 512 - totalBitPadding;
        int totalBytePadding = totalBitPadding / 8;
        int paddingLength = totalBytePadding + 8;
        byte[] padding = new byte[paddingLength];
        padding[0] = (byte) 0x80;

        long moduloTotalBitMessage = totalBitMessage % (long)(Math.pow(2, 64));
        for(int i = 0; i < 8; ++i){
            padding[paddingLength - 1 - i] = (byte) ((moduloTotalBitMessage >> (8 * i)) & 0xFF);
        }
        byte[] output = new  byte[data.length + paddingLength];
        System.arraycopy(data, 0, output, 0, data.length);
        System.arraycopy(padding, 0, output, data.length, paddingLength);
        return output;
    }

    public void generateHash(byte[] data){
        int[] K = {0x5A827999, 0x6ED9EBA1, 0x8F1BBCDC, 0xCA62C1D6};
        byte[] paddedData = paddingBits(data);

        int iterasi = paddedData.length / 64;
        byte[] chunk = new byte[64];
        for(int i = 0; i < iterasi; ++i){
            System.arraycopy(paddedData, 64 * i, chunk, 0, 64);
            processChunk(chunk, H, K);
        }
        byteHash = (H[0] << 128) | (H[1] << 96) | (H[2] << 64) | (H[3] << 32) | H[4];
        for(int i = 0; i < H.length; ++i){
            hash += Integer.toHexString(H[i]);
        }
    }

    public int leftRotate(int value, int bits){
        return ((value << bits) | (value >>> (32 - bits)));
    }

    public void processChunk(byte[] chunk, int H[], int K[]){

        int[] W = new int[80];
        for (int i = 0; i < 16; ++i){
            int startIndex = i * 4;
            W[i] = (chunk[startIndex + 0] & 0xFF) << 24 | (chunk[startIndex + 1] & 0xFF) << 16 |
                    (chunk[startIndex + 2] & 0xFF) << 8 | chunk[startIndex + 3] & 0xFF;
        }
        for(int i = 16; i < 80; ++i){
            W[i] = (W[i-3] ^ W[i-8] ^ W[i-14] ^ W[i-16]);
            W[i] = leftRotate(W[i], 1);
        }

        A = H[0];
        B = H[1];
        C = H[2];
        D = H[3];
        E = H[4];
        int F;
        int Kcurrent;

        for(int i = 0; i < 80; ++i){
            if(i < 20){
                F = (B & C) | ((~B) & D);
                Kcurrent = K[0];
            } else if(i < 40) {
                F = B ^ C ^ D;
                Kcurrent = K[1];
            } else if(i < 60) {
                F = (B & C) | (B & D) | (C & D);
                Kcurrent = K[2];
            } else {
                F = B ^ C ^ D;
                Kcurrent = K[3];
            }
            int temp = leftRotate(A, 5) + F + E + Kcurrent + W[i];
            E = D;
            D = C;
            C = leftRotate(B, 30);
            B = A;
            A = temp;
        }
        H[0] += A;
        H[1] += B;
        H[2] += C;
        H[3] += D;
        H[4] += E;
    }

    public String getMessage() {
        return message;
    }

    public long getByteHash() {
        return byteHash;
    }

    public String getHash() {
        return hash;
    }
}
