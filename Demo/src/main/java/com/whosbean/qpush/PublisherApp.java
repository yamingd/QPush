package com.whosbean.qpush;

/**
 * Hello world!
 *
 */
public class PublisherApp
{

    public static void startPublisher(final String[] args){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                com.whosbean.qpush.publisher.ServerMain.main(args);
            }
        });
        thread.start();
    }

    public static void main( String[] args )
    {
        startPublisher(args);
    }
}
