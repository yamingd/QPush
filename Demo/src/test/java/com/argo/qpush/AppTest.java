package com.argo.qpush;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }


    private static byte[] intToBytes(int n){
//        byte[] b = new byte[4];
//        for(int i = 0;i < 4;i++){
//            b[i] = (byte)(n >> (24 - i * 8));
//        }
//        return b;

        byte[] result = new byte[4];
        result[0] = (byte)((n >> 24) & 0xFF);
        result[1] = (byte)((n >> 16) & 0xFF);
        result[2] = (byte)((n >> 8) & 0xFF);
        result[3] = (byte)(n & 0xFF);
        return result;
    }

    public static int toInt(byte[] bytes) {
        int ret = 0;
        for (int i=0; i<4 && i<bytes.length; i++) {
            ret <<= 8;
            ret |= (int)bytes[i] & 0xFF;
        }
        return ret;
    }

    public void print(byte[] bytes){
        for (int i = 0; i < bytes.length; i++) {
            System.out.print(bytes[i]);
            System.out.print(",");
        }
        System.out.println("");
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        print(intToBytes(50));
        System.out.println(toInt(intToBytes(50)));
        print(intToBytes(127));
        System.out.println(toInt(intToBytes(127)));
        print(intToBytes(128));
        System.out.println(toInt(intToBytes(128)));
        print(intToBytes(200));
        System.out.println(toInt(intToBytes(200)));
    }
}
